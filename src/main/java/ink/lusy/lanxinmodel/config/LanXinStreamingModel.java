package ink.lusy.lanxinmodel.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import ink.lusy.lanxinmodel.properties.LanXinProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>
 * 蓝心模型流式输出的LangChain4j集成
 * </p>
 *
 * @author lusy
 * @date 2025-06-07 23:38
 */
@Slf4j
public class LanXinStreamingModel implements StreamingChatLanguageModel {

    private final LanXinProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ThreadPoolExecutor executorService;

    public LanXinStreamingModel(LanXinProperties props) {
        this.props = props;
        this.executorService = LanXinThreadPoolFactory.createThreadPool(props.getThreadPool());

        // 定期打印线程池状态（可选，用于监控）
        scheduleThreadPoolMonitoring();
    }

    @PreDestroy
    public void shutdown() {
        log.info("开始关闭LanXin流式模型...");
        LanXinThreadPoolFactory.shutdownGracefully(executorService, 10);
    }

    /**
     * 定期监控线程池状态（可选）
     */
    private void scheduleThreadPoolMonitoring() {
        if (log.isDebugEnabled()) {
            ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "lanxin-pool-monitor");
                t.setDaemon(true);
                return t;
            });

            monitor.scheduleAtFixedRate(() -> {
                if (!executorService.isShutdown()) {
                    log.debug(LanXinThreadPoolFactory.getThreadPoolStatus(executorService));
                }
            }, 60, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        CompletableFuture.runAsync(() -> {
            try {
                processStreamingChat(chatRequest, handler);
            } catch (Exception e) {
                log.error("流式聊天处理异常", e);
                handler.onError(e);
            }
        }, executorService);
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return ChatRequestParameters.builder()
                .temperature(props.getModel().getTemperature())
                .maxOutputTokens(props.getModel().getMaxOutputTokens())
                .build();
    }

    @Override
    public ModelProvider provider() {
        // 或者定义自己的provider
        return ModelProvider.OTHER;
    }

    private void processStreamingChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) throws Exception {
        // requestId 和 sessionId 如果需要可以存储起来
        UUID requestId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        // 构建请求参数
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("requestId", requestId.toString());
        String queryStr = mapToQueryString(queryParams);

        // 构建 messages 列表
        List<Map<String, Object>> messages = chatRequest.messages().stream()
                .flatMap(msg -> extractVivoMessages(msg).stream())
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("model", props.getModelName());
        data.put("sessionId", sessionId.toString());
        data.put("requestId", requestId.toString());
        data.put("messages", messages);

        // 生成认证头
        HttpHeaders headers = VivoAuth.generateAuthHeaders(
                props.getAppId(),
                props.getAppKey(),
                props.getMethod(),
                // 使用流式接口URI
                props.getStreamUri(),
                queryStr);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = String.format("https://%s%s?%s", props.getDomain(), props.getStreamUri(), queryStr);
        String requestBody = objectMapper.writeValueAsString(data);

        // 建立流式连接
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder fullContent = new StringBuilder();

        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // 设置超时
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setReadTimeout(60000);    // 60秒读取超时

            // 设置认证头
            HttpURLConnection finalConnection = connection;
            headers.forEach((key, values) -> {
                if (!values.isEmpty()) {
                    finalConnection.setRequestProperty(key, values.get(0));
                }
            });

            // 发送请求体
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                boolean isFirstChunk = true;
                TokenUsage finalTokenUsage = null;
                FinishReason finishReason = FinishReason.OTHER;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    // 处理Server-Sent Events格式
                    if (line.startsWith("data:")) {
                        String jsonData = line.substring(5).trim();

                        // 检查是否为结束标记
                        if ("[DONE]".equals(jsonData)) {
                            break;
                        }

                        try {
                            JsonNode jsonNode = objectMapper.readTree(jsonData);
                            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";
                            String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "";

                            if ("text".equals(type) && !message.isEmpty()) {
                                fullContent.append(message);

                                // 发送流式内容
                                handler.onPartialResponse(message);

                                if (isFirstChunk) {
                                    isFirstChunk = false;
                                }
                            }

                            // 处理结束信息
                            if (jsonNode.has("finish_reason")) {
                                String finishReasonStr = jsonNode.get("finish_reason").asText();
                                if ("stop".equals(finishReasonStr)) {
                                    finishReason = FinishReason.STOP;
                                }
                            }

                            // 处理统计信息
                            if (jsonNode.has("statistics")) {
                                JsonNode stats = jsonNode.get("statistics");
                                if (stats.has("totalWords")) {
                                    int totalTokens = stats.get("totalWords").asInt();
                                    finalTokenUsage = new TokenUsage(0, totalTokens, totalTokens);
                                }
                            }

                        } catch (Exception e) {
                            log.warn("解析流式响应数据失败: {}", jsonData, e);
                        }
                    } else if (line.startsWith("event:close")) {
                        // 连接关闭事件
                        break;
                    }
                }

                // 构建最终响应
                AiMessage aiMessage = new AiMessage(fullContent.toString());

                ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                        .id(requestId.toString())
                        .modelName(props.getModelName())
                        .tokenUsage(finalTokenUsage)
                        .finishReason(finishReason)
                        .build();

                ChatResponse finalResponse = ChatResponse.builder()
                        .aiMessage(aiMessage)
                        .metadata(metadata)
                        .build();

                handler.onCompleteResponse(finalResponse);

            } else {
                String errorResponse = "HTTP错误，状态码: " + responseCode;
                ChatResponse errorChatResponse = ChatResponse.builder()
                        .aiMessage(new AiMessage("【LanXinStreamingModel 错误】" + errorResponse))
                        .build();
                handler.onCompleteResponse(errorChatResponse);
            }

        } catch (Exception e) {
            log.error("流式请求处理异常", e);
            ChatResponse errorResponse = ChatResponse.builder()
                    .aiMessage(new AiMessage("【异常】" + e.getMessage()))
                    .build();
            handler.onCompleteResponse(errorResponse);
        } finally {
            // 关闭资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    log.warn("关闭reader失败", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }



    private List<Map<String, Object>> extractVivoMessages(ChatMessage message) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (message instanceof dev.langchain4j.data.message.UserMessage user) {
            for (Content content : user.contents()) {
                Map<String, Object> map = new HashMap<>();
                map.put("role", "user");

                if (content instanceof TextContent textContent) {
                    map.put("content", textContent.text());
                    map.put("contentType", "text");
                } else if (content instanceof ImageContent imageContent) {
                    map.put("content", "data:image/"+ imageContent.image().mimeType() +";base64," + imageContent.image().base64Data());
                    map.put("contentType", "image");
                } else {
                    map.put("content", "[UNSUPPORTED CONTENT TYPE]");
                    map.put("contentType", "text");
                }

                result.add(map);
            }
        } else if (message instanceof dev.langchain4j.data.message.SystemMessage sys) {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "system");
            map.put("content", sys.text());
            map.put("contentType", "text");
            result.add(map);
        } else if (message instanceof dev.langchain4j.data.message.AiMessage ai) {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "assistant");
            map.put("content", ai.text());
            map.put("contentType", "text");
            result.add(map);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "user");
            map.put("content", "[UNSUPPORTED MESSAGE TYPE]");
            map.put("contentType", "text");
            result.add(map);
        }

        return result;
    }

    private String mapToQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
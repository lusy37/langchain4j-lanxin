package ink.lusy.lanxinmodel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import ink.lusy.lanxinmodel.properties.LanXinProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 蓝心大模型集成LangChain4j
 * </p>
 *
 *@author lusy
 *@date 2025-06-07 23:38
 */
@RequiredArgsConstructor
@Slf4j
public class LanXinModel implements ChatLanguageModel {


    private final LanXinProperties props;

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return ChatRequestParameters.builder()
                .temperature(props.getModel().getTemperature())
                .maxOutputTokens(props.getModel().getMaxOutputTokens())
                .build();
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        try {
            // 如果需要，可以把 requestId 和 sessionId 存储起来
            UUID requestId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("requestId", requestId.toString());
            String queryStr = mapToQueryString(queryParams);
            // 构建 messages 列表
            List<Map<String, Object>> messages = chatRequest.messages().stream()
                    .flatMap(msg -> extractVivoMessages(msg).stream())
                    .collect(Collectors.toList());


            Map<String, Object> data = new HashMap<>();
            data.put("messages", messages);
            data.put("model", props.getModelName());
            data.put("sessionId", sessionId.toString());

            HttpHeaders headers = VivoAuth.generateAuthHeaders(
                                                                props.getAppId(),
                                                                props.getAppKey(),
                                                                props.getMethod(),
                                                                props.getUri(),
                                                                queryStr);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = String.format("https://%s%s?%s", props.getDomain(), props.getUri(), queryStr);
            String requestBody = new ObjectMapper().writeValueAsString(data);

            log.info(">>> 请求地址: {}", url);
            log.info(">>> 请求体: {}", requestBody);

            // 配置超时的RestTemplate
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(30000); // 30秒连接超时
            factory.setReadTimeout(60000);    // 60秒读取超时
            RestTemplate restTemplate = new RestTemplate(factory);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info(">>> 响应状态码: {}", response.getStatusCode());
            log.info(">>> 响应体: {}", response.getBody());


            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                Map<String, Object> result = new ObjectMapper().readValue(body, Map.class);

                if ((Integer) result.get("code") == 0 && result.containsKey("data")) {
                    Map<String, Object> dataMap = (Map<String, Object>) result.get("data");
                    String content = (String) dataMap.get("content");

                    AiMessage aiMessage = new AiMessage(content);

                    TokenUsage tokenUsage = null;
                    if (Objects.nonNull(dataMap.get("usage"))) {
                        Map<String, Object> tokens = (Map<String, Object>) dataMap.get("usage");
                        tokenUsage = new TokenUsage(
                                (Integer) tokens.get("promptTokens"),
                                (Integer) tokens.get("completionTokens"),
                                (Integer) tokens.get("totalTokens"));
                    }

                    ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                            .id(requestId.toString())
                            .modelName(dataMap.get("model").toString())
                            .tokenUsage(tokenUsage)
                            .finishReason(FinishReason.valueOf(dataMap.get("finishReason").toString().toUpperCase()))
                            .build();

                    return ChatResponse.builder()
                            .aiMessage(aiMessage)
                            .metadata(metadata)
                            .build();
                } else {
                    return ChatResponse.builder()
                            .aiMessage(new AiMessage("【LanXinModel 错误】" + body))
                            .build();
                }
            } else {
                return ChatResponse.builder()
                        .aiMessage(new AiMessage("【HTTP错误】状态码: " + response.getStatusCode()))
                        .build();
            }

        } catch (Exception e) {
            return ChatResponse.builder()
                    .aiMessage(new AiMessage("【异常】" + e.getMessage()))
                    .build();
        }
    }

    private static String mapToQueryString(Map<String, Object> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
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
        }

        // 处理系统/AI消息仍然是 1:1 的映射
        else if (message instanceof dev.langchain4j.data.message.SystemMessage sys) {
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


}

package ink.lusy.lanxinmodel.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import ink.lusy.lanxinmodel.config.LanXinModel;
import ink.lusy.lanxinmodel.config.LanXinStreamingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


/**
 * <p>
 * LanXin聊天服务 [仅供测试]
 * </p>
 *
 * @author lusy
 * @date 2025-06-08 13:32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanXinChatService {

    private final LanXinModel lanXinModel;
    private final LanXinStreamingModel lanXinStreamingModel;

    /**
     * 普通聊天
     */
    public ChatResponse chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 带系统提示的聊天
     */
    public ChatResponse chat(String userMessage, String systemPrompt) {
        try {
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            
            // 添加系统消息
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(new SystemMessage(systemPrompt));
            }
            
            // 添加用户消息
            messages.add(new UserMessage(userMessage));

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .build();

            return lanXinModel.doChat(request);
        } catch (Exception e) {
            log.error("聊天请求失败", e);
            return ChatResponse.builder()
                    .aiMessage(new AiMessage("聊天服务异常: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * 流式聊天
     */
    public CompletableFuture<String> streamChat(String userMessage) {
        return streamChat(userMessage, null);
    }

    /**
     * 带系统提示的流式聊天
     */
    public CompletableFuture<String> streamChat(String userMessage, String systemPrompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        AtomicReference<StringBuilder> contentBuilder = new AtomicReference<>(new StringBuilder());

        try {
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            
            // 添加系统消息
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(new SystemMessage(systemPrompt));
            }
            
            // 添加用户消息
            messages.add(new UserMessage(userMessage));

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .build();

            StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    contentBuilder.get().append(partialResponse);
                    log.debug("收到流式响应片段: {}", partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    String finalContent = response.aiMessage().text();
                    log.info("流式聊天完成，最终内容: {}", finalContent);
                    future.complete(finalContent);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式聊天出错", error);
                    future.completeExceptionally(error);
                }
            };

            lanXinStreamingModel.doChat(request, handler);

        } catch (Exception e) {
            log.error("流式聊天请求失败", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * 多轮对话
     */
    public ChatResponse multiTurnChat(List<String> conversation) {
        return multiTurnChat(conversation, null);
    }

    /**
     * 带系统提示的多轮对话
     */
    public ChatResponse multiTurnChat(List<String> conversation, String systemPrompt) {
        try {
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            
            // 添加系统消息
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(new SystemMessage(systemPrompt));
            }
            
            // 添加对话历史（假设奇数索引是用户消息，偶数索引是AI回复）
            for (int i = 0; i < conversation.size(); i++) {
                if (i % 2 == 0) {
                    // 用户消息
                    messages.add(new UserMessage(conversation.get(i)));
                } else {
                    // AI回复
                    messages.add(new AiMessage(conversation.get(i)));
                }
            }

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .build();

            return lanXinModel.doChat(request);
        } catch (Exception e) {
            log.error("多轮对话请求失败", e);
            return ChatResponse.builder()
                    .aiMessage(new AiMessage("多轮对话服务异常: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            ChatResponse response = chat("你好", "你是一个AI助手，请简短回复。");
            return response != null && response.aiMessage() != null && 
                   !response.aiMessage().text().contains("异常") && 
                   !response.aiMessage().text().contains("错误");
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return false;
        }
    }
}

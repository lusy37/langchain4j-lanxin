package ink.lusy.lanxinmodel.controller;

import dev.langchain4j.model.chat.response.ChatResponse;
import ink.lusy.lanxinmodel.properties.LanXinProperties;
import ink.lusy.lanxinmodel.service.LanXinChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * <p>
 * LanXin聊天控制器 [仅供测试]
 * </p>
 *
 * @author lusy
 * @date 2025-06-08 13:32
 */
@Slf4j
@RestController
@RequestMapping("/api/lanxin")
@RequiredArgsConstructor
public class LanXinChatController {

    private final LanXinChatService chatService;
    private final LanXinProperties properties;

    /**
     * 简单聊天接口
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String systemPrompt = request.get("systemPrompt");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "消息内容不能为空"));
            }

            ChatResponse response = chatService.chat(userMessage, systemPrompt);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.aiMessage().text(),
                    "metadata", Map.of(
                            "id", response.metadata() != null ? response.metadata().id() : "unknown",
                            "modelName", response.metadata() != null ? response.metadata().modelName() : "unknown",
                            "finishReason", response.metadata() != null ? response.metadata().finishReason() : "unknown",
                            "tokenUsage", response.metadata() != null && response.metadata().tokenUsage() != null ? 
                                    Map.of(
                                            "inputTokens", response.metadata().tokenUsage().inputTokenCount(),
                                            "outputTokens", response.metadata().tokenUsage().outputTokenCount(),
                                            "totalTokens", response.metadata().tokenUsage().totalTokenCount()
                                    ) : Map.of()
                    )
            ));
        } catch (Exception e) {
            log.error("聊天接口异常", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "聊天服务异常: " + e.getMessage()));
        }
    }

    /**
     * 流式聊天接口
     */
    @PostMapping("/stream-chat")
    public ResponseEntity<Map<String, Object>> streamChat(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String systemPrompt = request.get("systemPrompt");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "消息内容不能为空"));
            }

            CompletableFuture<String> future = chatService.streamChat(userMessage, systemPrompt);
            String result = future.get(); // 等待完成
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", result
            ));
        } catch (Exception e) {
            log.error("流式聊天接口异常", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "流式聊天服务异常: " + e.getMessage()));
        }
    }

    /**
     * SSE流式聊天接口
     */
    @PostMapping(value = "/sse-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseChat(@RequestBody Map<String, String> request) {
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        try {
            String userMessage = request.get("message");
            String systemPrompt = request.get("systemPrompt");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("消息内容不能为空"));
                emitter.complete();
                return emitter;
            }

            CompletableFuture<String> future = chatService.streamChat(userMessage, systemPrompt);
            
            future.whenComplete((result, throwable) -> {
                try {
                    if (throwable != null) {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data("流式聊天异常: " + throwable.getMessage()));
                    } else {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(result));
                    }
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE发送数据失败", e);
                    emitter.completeWithError(e);
                }
            });
            
        } catch (Exception e) {
            log.error("SSE聊天接口异常", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("SSE聊天服务异常: " + e.getMessage()));
                emitter.complete();
            } catch (IOException ioException) {
                emitter.completeWithError(ioException);
            }
        }
        
        return emitter;
    }

    /**
     * 多轮对话接口
     */
    @PostMapping("/multi-turn")
    public ResponseEntity<Map<String, Object>> multiTurnChat(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> conversation = (List<String>) request.get("conversation");
            String systemPrompt = (String) request.get("systemPrompt");
            
            if (conversation == null || conversation.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "对话历史不能为空"));
            }

            ChatResponse response = chatService.multiTurnChat(conversation, systemPrompt);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.aiMessage().text(),
                    "metadata", Map.of(
                            "id", response.metadata() != null ? response.metadata().id() : "unknown",
                            "modelName", response.metadata() != null ? response.metadata().modelName() : "unknown"
                    )
            ));
        } catch (Exception e) {
            log.error("多轮对话接口异常", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "多轮对话服务异常: " + e.getMessage()));
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            boolean isHealthy = chatService.healthCheck();
            
            return ResponseEntity.ok(Map.of(
                    "status", isHealthy ? "healthy" : "unhealthy",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("健康检查异常", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "error", e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * 获取模型信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        return ResponseEntity.ok(Map.of(
                "provider", properties.getInfo().getProvider(),
                "model", properties.getInfo().getDisplayName(),
                "modelName", properties.getModelName(),
                "version", properties.getInfo().getVersion(),
                "features", properties.getInfo().getFeatures(),
                "domain", properties.getDomain(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}

package com.example.lanxin.controller;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @PostMapping("/message")
    public String sendMessage(@RequestBody MessageRequest request) {
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(new UserMessage(request.getMessage())))
                    .build();
            ChatResponse response = chatLanguageModel.doChat(chatRequest);
            return response.aiMessage().text();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }

    @GetMapping("/test")
    public String test() {
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(new UserMessage("你好，请介绍一下你自己")))
                    .build();
            ChatResponse response = chatLanguageModel.doChat(chatRequest);
            return "测试成功！模型回复: " + response.aiMessage().text();
        } catch (Exception e) {
            return "测试失败: " + e.getMessage();
        }
    }

    public static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

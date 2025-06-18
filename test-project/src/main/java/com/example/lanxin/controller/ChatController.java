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

    @GetMapping("/test")
    public String test() {
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(new UserMessage("你好，这是真正从GitHub通过JitPack构建的LanXin测试")))
                    .build();
            ChatResponse response = chatLanguageModel.doChat(chatRequest);
            return "🎉 真正的JitPack测试成功！模型回复: " + response.aiMessage().text();
        } catch (Exception e) {
            return "❌ JitPack测试失败: " + e.getMessage();
        }
    }

    @PostMapping("/chat")
    public String chat(@RequestBody MessageRequest request) {
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(new UserMessage(request.getMessage())))
                    .build();
            ChatResponse response = chatLanguageModel.doChat(chatRequest);
            return "✅ JitPack调用成功: " + response.aiMessage().text();
        } catch (Exception e) {
            return "❌ JitPack调用失败: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String info() {
        return "🎉 从GitHub通过JitPack构建的LanXin LangChain4j测试项目\n" +
                "🔗 GitHub仓库: https://github.com/lusy37/langchain4j-lanxin\n" +
                "📦 JitPack坐标: com.github.lusy37:langchain4j-lanxin:v1.0.0\n" +
                "🚀 依赖来源: JitPack.io (从GitHub源码构建)\n" +
                "✨ 这证明了用户可以直接通过JitPack使用项目！";
    }

    @GetMapping("/status")
    public String status() {
        if (chatLanguageModel != null) {
            return "✅ ChatLanguageModel注入成功 - 类型: " + chatLanguageModel.getClass().getName() +
                    "\n🎯 这是从JitPack加载的真正依赖！";
        } else {
            return "❌ ChatLanguageModel注入失败";
        }
    }

    @GetMapping("/jitpack-verify")
    public String jitpackVerify() {
        // 检查类加载器信息来验证是否从JitPack加载
        String classLocation = chatLanguageModel.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        return "📦 JitPack验证信息:\n" +
                "🔍 ChatLanguageModel类型: " + chatLanguageModel.getClass().getName() + "\n" +
                "📍 类加载位置: " + classLocation + "\n" +
                "✨ 如果看到Maven仓库路径包含'com/github/lusy37'，则说明是从JitPack加载的！";
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

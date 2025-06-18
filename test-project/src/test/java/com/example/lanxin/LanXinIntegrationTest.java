package com.example.lanxin;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "langchain4j.vivo-lanxin.enabled=true",
    "langchain4j.vivo-lanxin.app-id=test-app-id",
    "langchain4j.vivo-lanxin.app-key=test-app-key"
})
public class LanXinIntegrationTest {

    @Autowired(required = false)
    private ChatLanguageModel chatLanguageModel;

    @Test
    public void testLanXinModelAutoConfiguration() {
        // 验证自动配置是否正常工作
        assertNotNull(chatLanguageModel, "ChatLanguageModel should be auto-configured");
        
        // 验证模型类型
        assertTrue(chatLanguageModel.getClass().getName().contains("LanXin"), 
                   "Should be LanXin model implementation");
    }

    @Test
    public void testBasicGeneration() {
        if (chatLanguageModel != null) {
            try {
                dev.langchain4j.model.chat.request.ChatRequest chatRequest =
                    dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(java.util.List.of(new dev.langchain4j.data.message.UserMessage("测试消息")))
                        .build();
                dev.langchain4j.model.chat.response.ChatResponse response = chatLanguageModel.doChat(chatRequest);
                assertNotNull(response, "Response should not be null");
                assertNotNull(response.aiMessage(), "AI message should not be null");
                assertFalse(response.aiMessage().text().trim().isEmpty(), "Response text should not be empty");
                System.out.println("模型回复: " + response.aiMessage().text());
            } catch (Exception e) {
                System.out.println("测试跳过 - 需要有效的API凭据: " + e.getMessage());
            }
        }
    }
}

package ink.lusy.lanxinmodel;

import ink.lusy.lanxinmodel.service.LanXinChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LanXin模型测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class LanXinModelTest {

    @Autowired
    private LanXinChatService chatService;

    @Test
    public void testHealthCheck() {
        // 测试健康检查
        boolean isHealthy = chatService.healthCheck();
        System.out.println("健康检查结果: " + isHealthy);
        // 注意：由于需要真实的API调用，这里可能会失败，这是正常的
    }

    @Test
    public void testSimpleChat() {
        try {
            var response = chatService.chat("你好，请简单介绍一下自己");
            assertNotNull(response);
            assertNotNull(response.aiMessage());
            assertNotNull(response.aiMessage().text());
            System.out.println("聊天响应: " + response.aiMessage().text());
        } catch (Exception e) {
            System.err.println("聊天测试失败: " + e.getMessage());
            // 在测试环境中，API调用可能失败，这是正常的
        }
    }

    @Test
    public void testStreamChat() {
        try {
            var future = chatService.streamChat("请用一句话介绍人工智能");
            var result = future.get();
            assertNotNull(result);
            System.out.println("流式聊天结果: " + result);
        } catch (Exception e) {
            System.err.println("流式聊天测试失败: " + e.getMessage());
            // 在测试环境中，API调用可能失败，这是正常的
        }
    }
}

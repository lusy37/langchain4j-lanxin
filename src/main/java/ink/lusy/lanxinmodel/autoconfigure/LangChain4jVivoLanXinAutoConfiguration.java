package ink.lusy.lanxinmodel.autoconfigure;

import ink.lusy.lanxinmodel.config.LanXinModel;
import ink.lusy.lanxinmodel.config.LanXinStreamingModel;
import ink.lusy.lanxinmodel.properties.LanXinProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * LangChain4j vivo LanXin 自动配置类
 *
 * <p>当检测到相关依赖和配置时，自动创建 vivo 蓝心大模型的 LangChain4j 集成 Bean。</p>
 *
 * <p>启用条件：</p>
 * <ul>
 *   <li>类路径中存在 LangChain4j 相关类</li>
 *   <li>配置了 {@code langchain4j.vivo-lanxin.enabled=true}（默认为 true）</li>
 *   <li>配置了必要的 API 密钥信息</li>
 * </ul>
 *
 * <p>配置示例：</p>
 * <pre>{@code
 * langchain4j:
 *   vivo-lanxin:
 *     enabled: true
 *     app-id: your-app-id
 *     app-key: your-app-key
 *     model-name: vivo-BlueLM-V-2.0
 * }</pre>
 *
 * @author lusy
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({
    dev.langchain4j.model.chat.ChatLanguageModel.class,
    dev.langchain4j.model.chat.StreamingChatLanguageModel.class
})
@ConditionalOnProperty(
    prefix = "langchain4j.vivo-lanxin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(LanXinProperties.class)
public class LangChain4jVivoLanXinAutoConfiguration {

    /**
     * 创建 vivo 蓝心大模型同步聊天模型 Bean
     */
    @Bean
    @ConditionalOnMissingBean(LanXinModel.class)
    @ConditionalOnProperty(prefix = "langchain4j.vivo-lanxin", name = "app-id")
    public LanXinModel lanXinModel(LanXinProperties properties) {
        log.info("正在创建 vivo LanXin ChatLanguageModel，模型: {}", properties.getModelName());
        return new LanXinModel(properties);
    }

    /**
     * 创建 vivo 蓝心大模型流式聊天模型 Bean
     */
    @Bean
    @ConditionalOnMissingBean(LanXinStreamingModel.class)
    @ConditionalOnProperty(prefix = "langchain4j.vivo-lanxin", name = "app-id")
    public LanXinStreamingModel lanXinStreamingModel(LanXinProperties properties) {
        log.info("正在创建 vivo LanXin StreamingChatLanguageModel，模型: {}", properties.getModelName());
        return new LanXinStreamingModel(properties);
    }
}

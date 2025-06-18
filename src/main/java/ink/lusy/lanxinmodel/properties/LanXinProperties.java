package ink.lusy.lanxinmodel.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;


@ConfigurationProperties(prefix = "langchain4j.vivo-lanxin")
@Data
public class LanXinProperties {

    private String appId;
    private String appKey;
    private String modelName;
    private String uri = "/vivogpt/completions";
    private String domain = "api-ai.vivo.com.cn";
    private String method = "POST";
    private String streamUri = "/vivogpt/completions/stream";

    // 模型参数配置
    private ModelConfig model = new ModelConfig();

    // 线程池配置
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    // 模型信息配置
    private ModelInfo info = new ModelInfo();

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(appId)) {
            throw new IllegalArgumentException("LanXin appId 不能为空");
        }
        if (!StringUtils.hasText(appKey)) {
            throw new IllegalArgumentException("LanXin appKey 不能为空");
        }
        if (!StringUtils.hasText(modelName)) {
            throw new IllegalArgumentException("LanXin modelName 不能为空");
        }
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("LanXin domain 不能为空");
        }
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("LanXin uri 不能为空");
        }
        if (!StringUtils.hasText(streamUri)) {
            throw new IllegalArgumentException("LanXin streamUri 不能为空");
        }
    }

    @Data
    public static class ModelConfig {
        /**
         * 模型温度参数，控制输出的随机性 (0.0-1.0)
         */
        private double temperature = 0.7;

        /**
         * 最大输出token数
         */
        private int maxOutputTokens = 4096;

        /**
         * 最大输入token数
         */
        private int maxInputTokens = 8192;
    }

    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private int corePoolSize = 2;

        /**
         * 最大线程数
         */
        private int maximumPoolSize = 10;

        /**
         * 线程空闲时间（秒）
         */
        private long keepAliveTime = 60L;

        /**
         * 队列容量
         */
        private int queueCapacity = 100;

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix = "vivo-lanxin-streaming-";

        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeOut = false;

        /**
         * 拒绝策略：ABORT, CALLER_RUNS, DISCARD, DISCARD_OLDEST
         */
        private String rejectedExecutionHandler = "CALLER_RUNS";
    }

    @Data
    public static class ModelInfo {
        /**
         * 模型提供商
         */
        private String provider = "vivo";

        /**
         * 模型显示名称
         */
        private String displayName = "vivo 蓝心大模型";

        /**
         * 模型版本
         */
        private String version = "1.0.0";

        /**
         * 支持的功能列表
         */
        private java.util.List<String> features = java.util.List.of("chat", "streaming", "multi-turn");
    }
}

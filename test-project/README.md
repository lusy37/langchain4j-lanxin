# LanXin LangChain4j 集成测试项目

这个项目演示了如何使用Maven依赖的方式集成LanXin（vivo BlueLM）到LangChain4j生态系统中。

## 快速开始

### 1. 添加Maven依赖

在你的 `pom.xml` 文件中添加以下依赖：

```xml
<!-- 在pom.xml中添加JitPack仓库 -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

        <!-- 添加您的依赖 -->
<dependency>
<groupId>com.github.lusy37</groupId>
<artifactId>langchain4j-lanxin</artifactId>
<version>v1.0.0</version>
</dependency>
```

### 2. 配置应用

在 `application.yml` 中添加LanXin配置：

```yaml
langchain4j:
  vivo-lanxin:
    enabled: true
    app-id: your-app-id
    app-key: your-app-key
    base-url: https://api.vivo.com.cn/vivogpt/completions
    temperature: 0.7
    max-output-tokens: 2048
    top-p: 0.9
    top-k: 50
    model-name: vivo-BlueLM-TB
    model-version: "1.0"
    provider: vivo
    features:
      - text-generation
      - conversation
    timeout: 30s
    max-retries: 3
    log-requests: true
    log-responses: false
```

### 3. 使用ChatLanguageModel

```java
@RestController
public class ChatController {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatLanguageModel.generate(message);
    }
}
```

## 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | boolean | false | 是否启用LanXin集成 |
| `app-id` | string | - | vivo开发者平台应用ID |
| `app-key` | string | - | vivo开发者平台应用密钥 |
| `base-url` | string | https://api.vivo.com.cn/vivogpt/completions | API基础URL |
| `temperature` | double | 0.7 | 生成温度 (0.0-1.0) |
| `max-output-tokens` | int | 2048 | 最大输出token数 |
| `top-p` | double | 0.9 | Top-p采样参数 |
| `top-k` | int | 50 | Top-k采样参数 |
| `model-name` | string | vivo-BlueLM-TB | 模型名称 |
| `model-version` | string | 1.0 | 模型版本 |
| `provider` | string | vivo | 提供商名称 |
| `features` | list | [text-generation] | 支持的功能列表 |
| `timeout` | duration | 30s | 请求超时时间 |
| `max-retries` | int | 3 | 最大重试次数 |
| `log-requests` | boolean | false | 是否记录请求日志 |
| `log-responses` | boolean | false | 是否记录响应日志 |

## 运行测试

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run

# 测试API
curl http://localhost:8081/api/chat/test
```

## 自动化测试

运行PowerShell脚本进行完整测试：

```powershell
.\test-maven-dependency.ps1
```

## 注意事项

1. 确保你有有效的vivo开发者平台账号和API凭据
2. 替换配置文件中的 `your-app-id` 和 `your-app-key` 为实际值
3. 根据需要调整模型参数和配置项

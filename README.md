# Lanxin for LangChain4j

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.0.0--beta2-blue.svg)](https://github.com/langchain4j/langchain4j)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 🚀 vivo 蓝心大模型(BlueLM)的LangChain4j集成封装，让您轻松在Java应用中使用蓝心大模型

## 📋 项目简介

**Lanxin for LangChain4j** 是一个专门为 **vivo 蓝心大模型(BlueLM)** 开发的 **LangChain4j 集成封装库**。该项目的核心目标是将 vivo 蓝心大模型无缝集成到 LangChain4j 生态系统中，让Java开发者能够通过标准的LangChain4j接口使用蓝心大模型的强大能力。

### 🎯 核心价值

- 🔌 **标准化集成** - 完整实现LangChain4j的`ChatLanguageModel`和`StreamingChatLanguageModel`接口
- 🛠️ **开箱即用** - 封装了vivo蓝心大模型的认证、请求处理等复杂逻辑
- 🔄 **无缝切换** - 可与其他LangChain4j支持的模型无缝切换使用
- 🏗️ **生产就绪** - 具备生产级的健壮性、错误处理和资源管理
- 📦 **轻量级** - 最小化依赖，易于集成到现有项目

### ✨ 主要特性

- 🔌 **完整的LangChain4j接口实现** - 支持同步和异步聊天模式
- 🌊 **流式输出支持** - 实现真正的流式响应处理
- 🛡️ **企业级健壮性** - 完善的异常处理、超时控制和资源管理
- 🔐 **安全认证** - 内置vivo API认证机制
- 🎛️ **灵活配置** - 支持多种配置方式和参数调优
- 🧪 **测试友好** - 提供完整的测试示例和API接口（仅用于测试验证）

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    LanXin Model Application                 │
├─────────────────────────────────────────────────────────────┤
│                      LangChain4j                            │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │ ChatLanguage    │    │ StreamingChatLanguageModel      │ │
│  │ Model Interface │    │ Interface                       │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                   LanXin Integration                        │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   LanXinModel   │    │   LanXinStreamingModel          │ │
│  │   同步实现       │    │   异步流式实现                   │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
│                           │                                 │
│                           ▼                                 │
│                    ┌─────────────────┐                      │
│                    │   VivoAuth      │                      │
│                    │   认证组件       │                      │
│                    └─────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌─────────────────────────┐
                    │    vivo BlueLM API      │
                    │    蓝心大模型服务)       │
                    └─────────────────────────┘
```

### 🔧 核心组件

#### LangChain4j集成层
- **LanXinModel** - 实现`ChatLanguageModel`接口，提供同步聊天功能
- **LanXinStreamingModel** - 实现`StreamingChatLanguageModel`接口，提供异步流式聊天
- **VivoAuth** - vivo API认证工具类，处理签名和请求头生成

#### 测试验证层（可选）
- **LanXinChatService** - 业务服务层，演示如何使用集成的模型
- **LanXinChatController** - REST API控制器，提供HTTP接口用于测试验证

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- vivo开发者账号和API密钥
- LangChain4j >=1.0.0-beta2，请确保您的项目中已引入兼容版本，或通过 BOM 管理版本。

### 方式一：作为依赖使用（推荐）

1. **添加依赖**
```xml
<!-- 在pom.xml中添加JitPack仓库 -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.lusy37</groupId>
    <artifactId>langchain4j-lanxin</artifactId>
    <version>v1.0.1</version>
</dependency>
```

2. **配置和使用**
```yaml
# application.yml
langchain4j:
  vivo-lanxin:
    enabled: true
    app-id: your-app-id
    app-key: your-app-key
    model-name: vivo-BlueLM-V-2.0
```

```java
// 自动注入使用
@Autowired
private ChatLanguageModel chatModel;

@Autowired
private StreamingChatLanguageModel streamingChatModel;

// 使用LangChain4j标准接口
ChatResponse response = chatModel.doChat(ChatRequest.builder()
    .messages(List.of(new UserMessage("你好，请介绍一下自己")))
    .build());

System.out.println(response.aiMessage().text());
```

### 方式二：克隆源码使用

1. **克隆项目**
```bash
git clone https://github.com/lusy37/langchain4j-lanxin.git
cd langchain4j-lanxin
```

2. **配置应用**

编辑 `src/main/resources/application.yml`：

```yaml
langchain4j:
  vivo-lanxin:
    enabled: true              # 启用 vivo LanXin 集成
    app-id: your-app-id        # vivo开发者平台应用ID
    app-key: your-app-key      # vivo开发者平台应用密钥
    model-name: vivo-BlueLM-V-2.0
    domain: api-ai.vivo.com.cn
    uri: /vivogpt/completions
    stream-uri: /vivogpt/completions/stream

    # 模型参数配置
    model:
      temperature: 0.7         # 温度参数，控制输出随机性 (0.0-1.0)
      max-output-tokens: 4096  # 最大输出token数
      max-input-tokens: 8192   # 最大输入token数

    # 模型信息配置
    info:
      provider: vivo           # 模型提供商
      display-name: vivo 蓝心大模型  # 模型显示名称
      version: 2.0             # 模型版本
      features:                # 支持的功能
        - chat
        - streaming
        - multi-turn
        - image

    # 线程池配置（生产环境推荐）
    thread-pool:
      core-pool-size: 2        # 核心线程数
      maximum-pool-size: 10    # 最大线程数
      keep-alive-time: 60      # 线程空闲时间（秒）
      queue-capacity: 100      # 队列容量
      thread-name-prefix: "vivo-lanxin-streaming-"
      rejected-execution-handler: "CALLER_RUNS"  # 拒绝策略
```

3. **编译和测试**
```bash
# 编译项目
mvn clean compile

# 运行单元测试
mvn test

# 启动测试应用（可选）
mvn spring-boot:run
```

## 💻 使用示例

### 基本用法

```java
import ink.lusy.lanxinmodel.config.LanXinModel;
import ink.lusy.lanxinmodel.properties.LanXinProperties;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;

// 1. 配置模型, 也可以在配置文件中配置
LanXinProperties properties = new LanXinProperties();
properties.setAppId("your-app-id");
properties.setAppKey("your-app-key");
properties.setModelName("vivo-BlueLM-V-2.0");

// 2. 创建模型实例
LanXinModel model = new LanXinModel(properties);

// 3. 发送聊天请求
ChatRequest request = ChatRequest.builder()
    .messages(List.of(new UserMessage("解释一下什么是人工智能")))
    .build();

ChatResponse response = model.doChat(request);
System.out.println("AI回复: " + response.aiMessage().text());
```

### 流式聊天

```java
import ink.lusy.lanxinmodel.config.LanXinStreamingModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

// 1. 创建流式模型
LanXinStreamingModel streamingModel = new LanXinStreamingModel(properties);

// 2. 定义响应处理器
StreamingChatResponseHandler handler = new StreamingChatResponseHandler() {
    @Override
    public void onPartialResponse(String partialResponse) {
        System.out.print(partialResponse); // 实时输出
    }

    @Override
    public void onCompleteResponse(ChatResponse response) {
        System.out.println("\n对话完成!");
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("错误: " + error.getMessage());
    }
};

// 3. 发送流式请求
streamingModel.doChat(request, handler);
```

### 与其他LangChain4j组件集成

```java
import dev.langchain4j.service.AiServices;

// 定义AI服务接口
interface Assistant {
    String chat(String userMessage);
}

// 使用蓝心模型创建AI助手
Assistant assistant = AiServices.builder(Assistant.class)
    .chatLanguageModel(new LanXinModel(properties))
    .build();

// 直接使用
String response = assistant.chat("请介绍一下Java编程语言");
```

## 🧪 测试验证

项目提供了完整的测试API接口，用于验证集成是否正常工作：

### 测试接口列表

| 方法 | 路径 | 描述 | 用途 |
|------|------|------|------|
| GET | `/api/lanxin/health` | 健康检查 | 验证服务状态 |
| GET | `/api/lanxin/info` | 模型信息 | 获取模型详情 |
| POST | `/api/lanxin/chat` | 普通聊天 | 测试同步聊天 |
| POST | `/api/lanxin/stream-chat` | 流式聊天 | 测试异步流式 |
| POST | `/api/lanxin/sse-chat` | SSE流式 | 测试SSE流式 |
| POST | `/api/lanxin/multi-turn` | 多轮对话 | 测试对话历史 |

### 快速测试

```bash
# 启动测试应用
mvn spring-boot:run

# 运行测试脚本
powershell -ExecutionPolicy Bypass -File test_simple.ps1
```

## 📦 集成到现有项目

### Spring Boot项目集成

1. **在 `application.yml` 中配置**
```yaml
langchain4j:
  vivo-lanxin:
    enabled: true                        # 启用 vivo LanXin 集成
    app-id: your-app-id                  # vivo开发者平台应用ID
    app-key: your-app-key                # vivo开发者平台应用密钥
    model-name: vivo-BlueLM-V-2.0        # 模型名称

    # 模型参数配置（可选）
    model:
      temperature: 0.7                   # 温度参数 (0.0-1.0)
      max-output-tokens: 4096            # 最大输出token数
      max-input-tokens: 8192             # 最大输入token数

    # 线程池配置（可选，用于流式聊天）
    thread-pool:
      core-pool-size: 2                  # 核心线程数
      maximum-pool-size: 10              # 最大线程数
      queue-capacity: 100                # 队列容量
```

2. **在业务代码中直接使用**
```java
@Service
public class MyAiService {

    @Autowired
    private ChatLanguageModel chatModel;  // 自动注入，无需手动配置

    @Autowired
    private StreamingChatLanguageModel streamingChatModel;  // 流式模型

    public String processUserQuery(String query) {
        ChatRequest request = ChatRequest.builder()
            .messages(List.of(new UserMessage(query)))
            .build();

        return chatModel.doChat(request).aiMessage().text();
    }

    public void processStreamingQuery(String query) {
        ChatRequest request = ChatRequest.builder()
            .messages(List.of(new UserMessage(query)))
            .build();

        streamingChatModel.doChat(request)
            .onNext(response -> System.out.print(response.aiMessage().text()))
            .onComplete(() -> System.out.println("\n对话完成"))
            .onError(error -> System.err.println("错误: " + error.getMessage()))
            .start();
    }
}
```

### 普通Java项目集成

对于非Spring Boot项目，需要手动创建配置和模型实例：

```java
import ink.lusy.lanxinmodel.config.LanXinModel;
import ink.lusy.lanxinmodel.config.LanXinStreamingModel;
import ink.lusy.lanxinmodel.properties.LanXinProperties;

public class LanXinExample {
    public static void main(String[] args) {
        // 1. 创建配置
        LanXinProperties props = new LanXinProperties();
        props.setAppId("your-app-id");
        props.setAppKey("your-app-key");
        props.setModelName("vivo-BlueLM-V-2.0");

        // 可选：配置模型参数
        props.getModel().setTemperature(0.7);
        props.getModel().setMaxOutputTokens(4096);

        // 2. 创建模型实例
        LanXinModel chatModel = new LanXinModel(props);
        LanXinStreamingModel streamingModel = new LanXinStreamingModel(props);

        // 3. 使用同步模型
        ChatResponse response = chatModel.doChat(
            ChatRequest.builder()
                .messages(List.of(new UserMessage("你好，请介绍一下自己")))
                .build()
        );
        System.out.println(response.aiMessage().text());

        // 4. 使用流式模型
        streamingModel.doChat(
            ChatRequest.builder()
                .messages(List.of(new UserMessage("请详细介绍一下人工智能")))
                .build()
        ).onNext(chunk -> System.out.print(chunk.aiMessage().text()))
         .onComplete(() -> System.out.println("\n对话完成"))
         .start();
    }
}
```

## 📁 项目结构

```
src/
├── main/
│   ├── java/ink/lusy/lanxinmodel/
│   │   ├── config/                        # 🔧 核心集成组件
│   │   │   ├── LanXinModel.java           #   ├─ LangChain4j同步模型实现
│   │   │   ├── LanXinStreamingModel.java  #   ├─ LangChain4j流式模型实现
│   │   │   ├── LanXinThreadPoolFactory.java #   ├─ 自定义线程池工厂
│   │   │   └── VivoAuth.java              #   └─ vivo API认证工具
│   │   ├── properties/                    # ⚙️ 配置管理
│   │   │   └── LanXinProperties.java      #   └─ 配置属性定义（含模型参数、线程池配置）
│   │   ├── controller/                    # 🧪 测试验证层（可选）
│   │   │   ├── LanXinChatController.java  #   ├─ REST API控制器
│   │   │   └── ThreadPoolMonitorController.java #   ├─ 线程池监控接口
│   │   ├── service/                       #   │
│   │   │   └── LanXinChatService.java     #   └─ 业务服务示例
│   │   └── LanXinModelApplication.java    # 🚀 测试应用启动类
│   └── resources/
│       └── application.yml                # 📝 应用配置（含完整配置示例）
├── test/                                  # ✅ 测试代码
│   ├── java/
│   │   └── LanXinModelTest.java          #   └─ 单元测试
│   └── resources/
│       └── application-test.yml          #   └─ 测试配置
└── docs/                                  # 📚 文档和脚本
    ├── test_simple.ps1                   #   ├─ PowerShell测试脚本
    └── API_TEST.md                       #   └─ API测试文档
```

### 核心文件说明

| 文件 | 类型 | 说明 |
|------|------|------|
| `LanXinModel.java` | **核心** | LangChain4j同步聊天模型实现 |
| `LanXinStreamingModel.java` | **核心** | LangChain4j流式聊天模型实现 |
| `LanXinThreadPoolFactory.java` | **核心** | 自定义线程池工厂，提供企业级线程池管理 |
| `VivoAuth.java` | **核心** | vivo API认证和签名工具 |
| `LanXinProperties.java` | **核心** | 配置属性管理（含模型参数、线程池配置） |
| `LanXinChatController.java` | 测试 | HTTP API接口（仅用于测试） |
| `ThreadPoolMonitorController.java` | 测试 | 线程池监控接口（仅用于测试） |
| `LanXinChatService.java` | 测试 | 业务服务示例（仅用于测试） |

## 🔧 配置说明

### 完整YAML配置示例

```yaml
langchain4j:
  vivo-lanxin:
    # 基础配置（必需）
    enabled: true                        # 是否启用 vivo LanXin 集成
    app-id: your-app-id                  # vivo开发者平台应用ID
    app-key: your-app-key                # vivo开发者平台应用密钥
    model-name: vivo-BlueLM-V-2.0        # 模型名称

    # API配置（可选）
    domain: api-ai.vivo.com.cn           # API域名
    uri: /vivogpt/completions            # API路径
    stream-uri: /vivogpt/completions/stream  # 流式API路径
    method: POST                         # HTTP方法

    # 模型参数配置（可选）
    model:
      temperature: 0.7                   # 温度参数 (0.0-1.0)
      max-output-tokens: 4096            # 最大输出token数
      max-input-tokens: 8192             # 最大输入token数

    # 模型信息配置（可选）
    info:
      provider: vivo                     # 模型提供商
      display-name: vivo 蓝心大模型       # 模型显示名称
      version: "2.0"                     # 模型版本
      features:                          # 支持的功能
        - chat
        - streaming
        - multi-turn

    # 线程池配置（可选，用于流式聊天）
    thread-pool:
      core-pool-size: 2                  # 核心线程数
      maximum-pool-size: 10              # 最大线程数
      keep-alive-time: 60                # 线程空闲时间（秒）
      queue-capacity: 100                # 队列容量
      thread-name-prefix: "vivo-lanxin-streaming-"  # 线程名称前缀
      allow-core-thread-time-out: false  # 是否允许核心线程超时
      rejected-execution-handler: "CALLER_RUNS"  # 拒绝策略
```

### 必需配置

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `langchain4j.vivo-lanxin.enabled` | 启用集成 | `true` |
| `langchain4j.vivo-lanxin.app-id` | vivo开发者平台应用ID | `your-app-id` |
| `langchain4j.vivo-lanxin.app-key` | vivo开发者平台应用密钥 | `your-secret-key` |
| `langchain4j.vivo-lanxin.model-name` | 模型名称 | `vivo-BlueLM-V-2.0` |

### 模型参数配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `langchain4j.vivo-lanxin.model.temperature` | `0.7` | 温度参数，控制输出随机性 (0.0-1.0) |
| `langchain4j.vivo-lanxin.model.max-output-tokens` | `4096` | 最大输出token数 |
| `langchain4j.vivo-lanxin.model.max-input-tokens` | `8192` | 最大输入token数 |

### 模型信息配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `langchain4j.vivo-lanxin.info.provider` | `vivo` | 模型提供商 |
| `langchain4j.vivo-lanxin.info.display-name` | `vivo 蓝心大模型` | 模型显示名称 |
| `langchain4j.vivo-lanxin.info.version` | `1.0.0` | 模型版本 |
| `langchain4j.vivo-lanxin.info.features` | `["chat", "streaming", "multi-turn"]` | 支持的功能列表 |
| `lanxin.model.max-input-tokens` | `8192` | 最大输入token数 |

### 线程池配置（生产环境推荐）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `lanxin.thread-pool.core-pool-size` | `2` | 核心线程数 |
| `lanxin.thread-pool.maximum-pool-size` | `10` | 最大线程数 |
| `lanxin.thread-pool.keep-alive-time` | `60` | 线程空闲时间（秒） |
| `lanxin.thread-pool.queue-capacity` | `100` | 队列容量 |
| `lanxin.thread-pool.rejected-execution-handler` | `CALLER_RUNS` | 拒绝策略 |


### 其他配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `lanxin.domain` | `api-ai.vivo.com.cn` | API域名 |
| `lanxin.uri` | `/vivogpt/completions` | 普通聊天接口路径 |
| `lanxin.stream-uri` | `/vivogpt/completions/stream` | 流式聊天接口路径 |

## 🛡️ 企业级特性

### 健壮性保障
- ✅ **配置验证** - 启动时自动验证必要配置参数
- ✅ **资源管理** - 自动管理线程池和HTTP连接资源
- ✅ **超时控制** - 30秒连接超时，60秒读取超时
- ✅ **异常处理** - 完善的错误处理和日志记录
- ✅ **并发安全** - 线程安全的实现设计
- ✅ **内存优化** - 流式处理避免大内容阻塞

### LangChain4j兼容性
- ✅ **标准接口** - 完全兼容LangChain4j接口规范
- ✅ **无缝切换** - 可与OpenAI、Claude等其他模型无缝切换
- ✅ **生态集成** - 支持LangChain4j的所有高级功能
- ✅ **版本兼容** - 支持LangChain4j 1.0.0-beta2及以上版本

### 生产就绪
- ✅ **监控友好** - 提供健康检查和状态监控接口
- ✅ **日志完善** - 详细的请求和错误日志
- ✅ **配置灵活** - 支持多种配置方式和环境
- ✅ **测试覆盖** - 完整的单元测试和集成测试


## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情


## 🙏 致谢

- [LangChain4j](https://github.com/langchain4j/langchain4j) - 优秀的Java AI应用开发框架
- [vivo BlueLM](https://developers.vivo.com/) - 强大的大语言模型服务
- [Spring Boot](https://spring.io/projects/spring-boot) - 企业级应用开发框架

---

⭐ **如果这个项目对您有帮助，请给个Star支持一下！**

🔗 **相关项目推荐**
- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI应用开发框架
- [Spring AI](https://github.com/spring-projects/spring-ai) - Spring生态AI集成

# AGENTS.md

> 本文件供所有 AI 工具（Claude Code、Cursor、Copilot、Windsurf、Aider 等）读取，描述本项目的硬性约束、约定与工作流，避免每次新对话都重复解释。

---

## 1. 项目速览

- **类型**：Spring Boot Starter 集合（脚手架）
- **Java 版本**：25（`fancy-boot-parent/pom.xml` 定义 `java.version`）
- **Spring Boot 版本**：4.1.x
- **构建工具**：Maven（`./mvnw`，**不用** `gradlew`）
- **Lombok**：项目级按需使用，业务方依赖 `provided`
- **关键依赖**：Jackson 3（databind 包名 `tools.jackson.databind.*`）、Log4j2（**不是** Logback）、MyBatis-Plus 3.5.x

---

## 2. 模块布局

```
fancy-boot/
├── fancy-boot-parent/         # 父 POM，统管依赖与插件版本
├── fancy-boot-bom/            # 业务方引入的 BOM
├── fancy-boot-core/           # 核心工具
└── fancy-boot-starters/       # 所有 starter 聚合（9 个）
```

包名规则：

- `fancy.boot.core.*` —— core 模块
- `fancy.starter.<feature>.*` —— starter 模块（例：`fancy.starter.validation.advice.*`）

---

## 3. 编码硬性约定（务必遵守）

### 3.1 命名与结构

| 约定            | 说明                                                                                                                                                                                                                             |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 包结构           | `<starter>/<layer>/`，常见 layer：`advice`、`autoconfigure`、`service`、`component`、`model`、`properties`、`annotation`、`aspect`、`filter`、`handler`、`util`、`bootstrap`、`authentication`、`context`、`provider`、`mapper`、`core`、`executor` |
| 类命名           | Service / Component / Handler / Aspect / Filter / Repository 等后缀与所在包语义一致                                                                                                                                                       |
| Bean 方法名      | **小驼峰 + 类名**（如 `validationExceptionAdvice()`）                                                                                                                                                                                  |
| starter 类全限定名 | `fancy.starter.<feature>.autoconfigure.<Feature>AutoConfiguration`                                                                                                                                                             |

### 3.2 Lombok

- `@Slf4j` 替代手写 `LoggerFactory.getLogger(...)`
- `@RequiredArgsConstructor` / `@AllArgsConstructor` 替代手写构造器
- `@Getter` / `@Setter` 仅在需要时使用，不要每字段都加
- 不允许混用 `@Builder` + `@AllArgsConstructor` + `@NoArgsConstructor` 同一字段集（如 `@Builder.Default` 失效）
- 工具类统一使用 `@UtilityClass`（Lombok 实验特性）

### 3.3 异常处理

- `@RestControllerAdvice` 已包含 `@ControllerAdvice`，**不要**重复标注
- 业务异常走 `WARN` 级别，堆栈仅 DEBUG 输出
- 不可预期的系统异常（兜底）走 `ERROR` 级别，打印完整堆栈
- 校验失败返回 `400 BAD_REQUEST`（**不是** 500）
- 校验错误返回全部字段集合，**不要**只取第一个错误

### 3.4 自动配置

所有 starter 自动配置类必须：

- 标注 `@AutoConfiguration`
- 业务可覆盖的 Bean 加 `@ConditionalOnMissingBean`
- 注册到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

`EnvironmentPostProcessor` 走 `META-INF/spring.factories` 的 `EnvironmentPostProcessor` key。

### 3.5 Javadoc

- 公开类与公开方法必须有 Javadoc
- 用 `{@link}` 引用类型，不要硬编码类名
- 中文标点使用半角 + 空格（如 `"方法: 返回结果"`），与现有代码风格一致

### 3.6 强制使用 Record 而非 DTO

- 仅含数据的载体类（DTO / VO / 参数接收）使用 `record`
- 需要可变状态的类用 class + Lombok
- Spring 响应 `Response<T>` 是 record

---

## 4. 文档约定

### 4.1 README 集中管理

**根目录 `README.md` 是项目的唯一文档入口。** 所有 starter 的使用说明放在 `docs/` 子目录里。

**不要**给每个 starter 模块单独写 README.md。理由：

- 模块数量多（9 个），重复内容多
- starter 之间有依赖关系，单文档无法体现整体
- 用户认知：在一个地方能查全

### 4.2 docs 命名

`docs/<starter-name>.md`，与 starter artifactId 去前缀 kebab-case 一致：

- `fancy-web-spring-boot-starter` → `docs/web.md`
- `fancy-validation-spring-boot-starter` → 合并在 `web.md`

### 4.3 文档更新触发条件

以下情形必须更新文档：

- 新增/删除/重命名 starter
- starter 引入方式或配置项变更
- starter API（公开类/方法）变更

---

## 5. Response / HttpStatus 使用规则

`fancy.boot.core.http.Response` 是项目 Web 统一响应，**业务项目直接使用**，starter 的异常处理也走它：

```java
public record Response<T>(int code, String message, T data) {
}
```

**API 速查**（不要凭记忆使用，先到 `fancy-boot-core/.../http/Response.java` 核对）：

| 调用                                              | 用途                                 |
|-------------------------------------------------|------------------------------------|
| `Response.success()`                            | 成功无数据（默认消息 `OK`）                   |
| `Response.success(T data)`                      | 成功带数据（默认消息 `OK`）                   |
| `Response.success(String message, T data)`      | 成功 + 自定义消息 + 数据                    |
| `Response.of(int code, String message)`         | 无数据，状态码自选                          |
| `Response.of(int code, String message, T data)` | 显式三参                               |
| `Response.fail()`                               | 失败（默认 500，`Internal Server Error`） |
| `Response.fail(String message)`                 | 失败（默认 500）                         |
| `Response.unauthorized(String message)`         | 401 响应                             |
| `Response.forbidden(String message)`            | 403 响应                             |

**HttpStatus 枚举**（`value` 数值 + `reasonPhrase` 原因短语）：

- `OK(200, "OK")`
- `UNAUTHORIZED(401, "Unauthorized")`
- `FORBIDDEN(403, "Forbidden")`
- `INTERNAL_SERVER_ERROR(500, "Internal Server Error")`

**易踩坑**：

- HttpStatus 枚举仅承载项目必须的 4 个值，业务层需要其他 HTTP 码用 `int` 字面量直接传给 `Response.of(...)`。
- `Response` 的 `data` 字段 `null` 时不会序列化（`@JsonInclude(NON_NULL)`）。
- `message` 不允许为 `null`（构造器 `requireNonNull`）。

---

## 6. 工作流规则

### 6.1 接到任务时

1. **先探索**：用 `Glob` / `Grep` / `Read` 摸清现状，不要直接动代码
2. **不要凭印象**：API 调用、字段名、配置项先 `Read` 文件确认
3. **大改动进 Plan 模式**（Claude Code 的 `EnterPlanMode` 或 AskUserQuestion 先对齐）

### 6.2 修改代码前

1. 确认修改不破坏其他模块（启动器之间可能有依赖）
2. 修改后跑 `mvn clean compile -DskipTests` 验证编译通过
3. 修改 starter 自动配置类后，确认对应 `AutoConfiguration.imports` 文件已注册

### 6.3 不要做的事

- ❌ 不要给每个 starter 单独写 README
- ❌ 不要随意新增 `*Properties` 等配置类（除非用户明确要求或者确实有需要）
- ❌ 不要在 advice/handler 里把不可预期的系统异常打 `WARN` 级别
- ❌ 不要在 catch 里用 `_` 占位捕获 `InterruptedException` 后不重新设置中断标志
- ❌ 不要把方法返回值类型设为 `Response<String>` 然后塞 Object 数据
- ❌ 不要用 `private static final Logger LOGGER` 写法（用 `@Slf4j`）
- ❌ 不要新增项目内无人调用的"预留扩展"类，需要时再加

---

## 7. 关键 API 索引（快速跳转）

| 关注点             | 路径                                                                                                           |
|-----------------|--------------------------------------------------------------------------------------------------------------|
| 统一响应            | `fancy-boot-core/src/main/java/fancy/boot/core/http/Response.java`                                           |
| HTTP 状态码        | `fancy-boot-core/src/main/java/fancy/boot/core/http/HttpStatus.java`                                         |
| ID 生成           | `fancy-boot-core/src/main/java/fancy/boot/core/id/IdUtils.java`                                              |
| 字符串工具           | `fancy-boot-core/src/main/java/fancy/boot/core/lang/StringUtils.java`                                        |
| 注解解析            | `fancy-boot-core/src/main/java/fancy/boot/core/annotation/AnnotationResolver.java`                           |
| 网络工具            | `fancy-boot-core/src/main/java/fancy/boot/core/net/NetUtils.java`                                            |
| 进程工具            | `fancy-boot-core/src/main/java/fancy/boot/core/system/ProcessUtils.java`                                     |
| Web 自动配置        | `fancy-boot-starters/fancy-web-spring-boot-starter/.../autoconfigure/WebAutoConfiguration.java`              |
| 全局异常兜底          | `fancy-boot-starters/fancy-web-spring-boot-starter/.../advice/GlobalExceptionAdvice.java`                    |
| Web 工具类         | `fancy-boot-starters/fancy-web-spring-boot-starter/.../util/WebUtils.java`                                   |
| 校验异常处理          | `fancy-boot-starters/fancy-validation-spring-boot-starter/.../advice/ValidationExceptionAdvice.java`         |
| 编程式校验           | `fancy-boot-starters/fancy-validation-spring-boot-starter/.../service/ValidatorService.java`                 |
| MyBatis-Plus 配置 | `fancy-boot-starters/fancy-mybatis-plus-spring-boot-starter/.../properties/MyBatisPlusProperties.java`       |
| 元实体             | `fancy-boot-starters/fancy-mybatis-plus-spring-boot-starter/.../entity/MetaDO.java`                          |
| Redis 服务        | `fancy-boot-starters/fancy-redis-spring-boot-starter/.../service/RedisService.java`                          |
| 动态数据源管理         | `fancy-boot-starters/fancy-datasource-spring-boot-starter/.../core/DynamicDataSourceManager.java`            |
| 资源服务器配置         | `fancy-boot-starters/fancy-resource-server-spring-boot-starter/.../properties/ResourceServerProperties.java` |

---

## 8. 与 AI 协作时的提示

### 用户偏好（已确认）

- 用户**重视代码质量与一致性**，倾向于让 AI 主动指出"踩坑点"
- 用户**会亲手调整 AI 输出的代码**，最终代码以项目里最新的版本为准
- 用户**不要求 AI 输出每个文件的修改历史总结**，精简输出即可
- 用户**习惯中文回答**

### 沟通模式

- **探索性任务**（"看看有什么可优化"）→ 给方案 + 关键文件，不直接动手
- **明确任务**（"把 X 改成 Y"）→ 直接改，跑编译验证
- **新功能/重构** → 默认进 Plan 模式让用户确认后再实施

---

## 9. 验证流程

每次代码改动完成，按顺序执行：

```bash
# 1. 编译验证
./mvnw clean compile -DskipTests

# 2. 修改 starter 时全模块编译（防止破坏依赖）
./mvnw clean install -DskipTests -pl fancy-boot-core,fancy-boot-starters/<name> -am
```

UI / 前端改动：本项目无前端模块（纯 Java 后端），跳过。

---

## 10. 版本管理

- 当前版本：`4.1.0`
- 版本号遵循 `<major>.<minor>.<patch>`，跟随 Spring Boot 主版本
- 修改后**不要**自行 bump 版本号，等用户决策

---

**最后提醒**：如果你（AI）发现新的项目约定与本文冲突，**以本文为准**；如果你认为本文有错误或不完整，告诉用户并建议修订。
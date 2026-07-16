# 日志

基于 Log4j2，提供接口出入参日志、TraceId 串联。

## 引入

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-log-spring-boot-starter</artifactId>
</dependency>
```

> starter 强制使用 **Log4j2**（排除 Spring Boot 默认的 Logback）。业务项目无需额外配置日志框架。

---

## 功能概览

| 功能             | 描述                                          | 默认开关 |
|----------------|---------------------------------------------|------|
| `@Log` 切面      | 方法或类上加 `@Log` 主动开启日志                        | ✅    |
| Controller 切面  | 自动为所有 Controller 方法记录日志（除非方法/类上有 `@Log`）    | ✅    |
| TraceId Filter | 从 Header 读 / 自动生成 TraceId，写入 MDC 并回传 Header | ✅    |

两个切面的关系：`@Log` 注解**优先**于 Controller 切面；同一方法只会触发一个。

---

## 配置

```yaml
fancy:
  log:
    enabled: true                       # 全局总开关
    service-name: order-service         # 自定义服务名；不配则取 spring.application.name
    print-args: true                    # 是否打印入参
    max-args-length: 2048               # 入参最大长度，超出截断
    print-result: true                  # 是否打印返回结果
    max-result-length: 2048             # 返回结果最大长度，超出截断
    annotation:                         # @Log 切面配置
      enabled: true
    controller:                         # Controller 切面配置
      enabled: true
```

`fancy.log.print-args`、`fancy.log.max-args-length`、`fancy.log.print-result`、`fancy.log.max-result-length` 是 `@Log`
注解缺省时的兜底值；当方法上有 `@Log` 时，使用注解上的值。

---

## 使用

### 1. `@Log` 注解

在方法或类上添加 `@Log`：

```java
@Log("创建用户")
@PostMapping
public Response<UserVO> create(@RequestBody @Valid UserCreateRequest request) {
    return Response.of(HttpStatus.CREATED.getValue(), "创建成功", userService.create(request));
}
```

`@Log` 参数：

| 参数                | 默认   | 说明                         |
|-------------------|------|----------------------------|
| `value`           | —    | 描述（必填），作为日志前缀 `[value]` 打印 |
| `printArgs`       | true | 是否打印入参                     |
| `maxArgsLength`   | 2048 | 入参最大长度                     |
| `printResult`     | true | 是否打印返回结果                   |
| `maxResultLength` | 2048 | 返回结果最大长度                   |

`@Log` 可标注在类上，作为整个 Controller 的默认值；方法上的 `@Log` 会覆盖类上的。

### 2. Controller 自动日志

所有 `@RestController` / `@Controller` 的方法会自动记录日志，无需加注解。

若某个 Controller 方法已有 `@Log`，Controller 切面**不会**重复打印，由 `@Log` 切面处理。

### 3. 日志输出格式

```
[创建用户] 服务: order-service | 接口: UserController#create | 耗时: 23ms | 入参: {"username":"admin"} | 
	返回结果: {"code":201,"message":"创建成功","data":{"id":1}}
```

异常情况以 `ERROR` 级别打印，自动附带完整堆栈。

---

## TraceId 串联

`TraceIdFilter` 会在请求入口：

1. 从请求头 `X-Trace-Id` 读取 TraceId，没有则用 Snowflake ID 生成
2. 写入 Log4j2 的 `ThreadContext`（MDC key = `traceId`）
3. 在响应头 `X-Trace-Id` 中回写给客户端

业务日志格式需要包含 `%X{traceId}` 占位符，例如：

```properties
# log4j2.xml
<PatternLayout=pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level [%X{traceId}] %logger{36} - %msg%n"/>
```

---

## 关闭某个切面

```yaml
fancy:
  log:
    annotation:
      enabled: false   # 关闭 @Log 切面
    controller:
      enabled: false   # 关闭 Controller 自动切面
```

也可以全局关闭 `fancy.log.enabled=false`，所有日志功能停用。

---

## 自定义日志打印

提供自己的 `LogAdvice` / `LogAspect` / `ControllerLogAspect` / `LogPrinter` Bean 即可替换默认实现（所有 Bean 都标注
`@ConditionalOnMissingBean`）。

---

## 常见问题

**Q：业务项目已经有 log4j2 配置怎么办？**
A：正常生效，starter 不强制覆盖日志配置。

**Q：想看 SQL 但日志太多怎么办？**
A：参考 [mybatis-plus.md](./mybatis-plus.md) 的 P6Spy 章节，或单独调高 SQL logger 级别。

**Q：TraceId 没出现在日志里？**
A：检查 log4j2.xml 的 PatternLayout 是否包含 `%X{traceId}`。

**Q：日志里入参很长怎么办？**
A：调小 `fancy.log.max-args-length` 或在 `@Log` 上单独设置 `maxArgsLength`。超出部分会被截断为 `text + "..."`。
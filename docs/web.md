# Web 与统一响应

本文档覆盖 `fancy-web-spring-boot-starter`、`fancy-validation-spring-boot-starter`，以及 `fancy-boot-core` 中的
`Response`、`HttpStatus`、工具类。

---

## 1. 统一响应 Response

`fancy-boot-core` 提供的统一响应包装。业务项目的 Controller 与 starter 的异常处理均使用此响应，保证项目间**响应格式一致**。

### 定义

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(int code, String message, T data) {
    public Response {
        Objects.requireNonNull(message, "message must not be null.");
    }
}
```

`null` 字段不会出现在序列化结果里（`@JsonInclude(NON_NULL)`）。`message` 不允许为 `null`。

### 静态工厂方法

| 方法                                              | 用途                                 |
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

> `success` 系列默认状态码 `HttpStatus.OK(200)`，消息默认为 `HttpStatus.OK.getReasonPhrase()`（即 `"OK"`）。需要其他状态码（201/202
> 等）仍可走 `Response.of(int code, String message, T data)`。

---

## 2. HttpStatus 枚举

`fancy.boot.core.http.HttpStatus` 是项目用到的 HTTP 状态码枚举，每项含两个字段：`value`（数值）与 `reasonPhrase`（标准原因短语）。

```java
public enum HttpStatus {
    // 2xx, Success
    OK(200, "OK"),

    // 4xx, Client Error
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),

    // 5xx, Server Error
    INTERNAL_SERVER_ERROR(500, "Internal Server Error")
}
```

调用示例：

- `HttpStatus.OK.getValue()` → `200`
- `HttpStatus.OK.getReasonPhrase()` → `"OK"`
- `HttpStatus.UNAUTHORIZED.getValue()` → `401`

> 当前仅承载项目必须的 4 个枚举值，业务层如需其他 HTTP 码用 `int` 字面量直接传入 `Response.of(...)`。

---

## 3. 全局异常处理

`fancy-web-spring-boot-starter` 提供兜底的全局异常处理器，捕获未处理的 `Exception`：

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public Response<String> handleException(Exception exception) {
        log.error("系统异常: {}", exception.getMessage(), exception);
        return Response.fail("系统异常: " + exception.getMessage());
    }
}
```

- 走 `ERROR` 级别并打印完整堆栈——这是不可预期异常的兜底
- 业务方应在自己的 starter 或服务中定义更具体的 `@ExceptionHandler`，顺序在兜底之前
- Bean 已标注 `@ConditionalOnMissingBean`，提供自己的全局 Advice 即可覆盖

---

## 4. 参数校验

引入 [`fancy-validation-spring-boot-starter`](../../fancy-boot-starters/fancy-validation-spring-boot-starter) 后，会自动注册
`ValidationExceptionAdvice`，统一处理以下 4 种校验异常：

| 异常                                 | 触发场景                                                 |
|------------------------------------|------------------------------------------------------|
| `ConstraintViolationException`     | `@RequestParam` / `@PathVariable` / 单参数 `@Validated` |
| `MethodArgumentNotValidException`  | `@RequestBody @Valid`                                |
| `BindException`                    | 表单绑定（`application/x-www-form-urlencoded`）            |
| `HandlerMethodValidationException` | Spring 6.1+ 控制器方法参数校验                                |

### 响应结构

校验失败统一返回 `400`，`data` 为 `List<FieldErrorDetail>`：

```json
{
  "code": 400,
  "message": "实体类参数校验失败",
  "data": [
    { "field": "username", "message": "用户名不能为空", "rejectedValue": "" },
    { "field": "age", "message": "年龄必须大于 0", "rejectedValue": -1 }
  ]
}
```

`FieldErrorDetail`：

```java
public record FieldErrorDetail(String field, String message, Object rejectedValue) {
}
```

注意 `HandlerMethodValidationException` 的 `field` 字段为 `null`（框架不提供字段名）。

### 使用示例

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public Response<UserVO> getById(@PathVariable @Min(1) Long id) {
        // id <= 0 时返回 400 + 上述 data 结构
        return Response.success("查询成功", userService.getById(id));
    }

    @PostMapping
    public Response<UserVO> create(@RequestBody @Valid UserCreateRequest request) {
        // 创建成功常用 201 Created, 此时可用 Response.of(HttpStatus.CREATED, ...) 或 Response.of(201, ...)
        return Response.of(HttpStatus.OK.getValue(), "创建成功", userService.create(request));
    }
}
```

```java

@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Min(value = 0, message = "年龄必须大于等于 0")
    private Integer age;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

> `@RequestBody` 校验需要在 DTO 上标注 `@Valid`（或 `@Validated`）；简单参数校验需要在 Controller 类上标注 `@Validated`。

### 编程式校验

通过 `ValidatorService` 手动校验：

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final ValidatorService validatorService;

    public void save(UserDTO dto) {
        validatorService.validate(dto, SaveGroup.class);
        // ...
    }
}
```

校验失败会抛出 `ConstraintViolationException`，由 `ValidationExceptionAdvice` 统一处理。注意：`ValidatorService.validate`
当前仅抛出**第一个**错误（`violations.iterator().next()`），需要返回全部错误请自行迭代。

### 日志行为

- 校验失败默认 `WARN` 级别，避免污染 `ERROR` 日志
- 异常堆栈仅在 `DEBUG` 级别输出，避免噪音

### 覆盖默认行为

`ValidationExceptionAdvice` 与 `ValidatorService` 均标注 `@ConditionalOnMissingBean`，可通过自定义 Bean 覆盖。

---

## 5. 客户端请求

`fancy-web-spring-boot-starter` 默认注册了 `RestClient` 与 `WebClient.Builder` / `WebClient`：

```java
@Service
@RequiredArgsConstructor
public class ExternalService {

    private final RestClient restClient;

    public UserDTO getUser(Long id) {
        return restClient.get()
                .uri("https://api.example.com/users/{id}", id)
                .retrieve()
                .body(UserDTO.class);
    }
}
```

`RestClient` 默认带 `Content-Type: application/json` 请求头。

`WebClient.Builder` 默认取消内存缓冲区大小限制（`maxInMemorySize = -1`），适合大报文场景。

---

## 6. Web 工具类

`fancy.starter.web.util.WebUtils` 提供：

### 路径规范化

```java
File safe = WebUtils.normalize("/var/data/../etc/passwd");
```

按 `normalize().toRealPath()` 解析，防止路径穿越攻击。文件不存在时抛 `NoSuchFileException`。

### 文件下载

```java
File file = new File("/var/data/report.pdf");
WebUtils.download(file, response);
```

- `file` 为 null 或文件不存在 → 返回 `{"code":500,"message":"文件不存在"}` JSON 响应
- 自动设置 `Content-Type`（按文件 mime 探测，无法探测则降级为 `application/octet-stream`）
- 文件名走 `URLEncoder` + `filename*=UTF-8''...` 兼容所有浏览器

---

## 7. 核心模块工具

`fancy-boot-core` 在 `fancy.boot.core.*` 下提供：

| 工具类                                             | 说明                                        |
|-------------------------------------------------|-------------------------------------------|
| `fancy.boot.core.id.IdUtils`                    | Snowflake ID 生成（`generateSnowflakeId()`）  |
| `fancy.boot.core.id.Snowflake`                  | 雪花算法 record（dataCenterId + workerId）      |
| `fancy.boot.core.lang.StringUtils`              | `isBlank` / `isNotBlank` / `lowerFirst` 等 |
| `fancy.boot.core.net.NetUtils`                  | 本地主机、MAC 地址等                              |
| `fancy.boot.core.system.ProcessUtils`           | 当前进程 PID                                  |
| `fancy.boot.core.net.NetException`              | 网络异常 RuntimeException                     |
| `fancy.boot.core.annotation.AnnotationResolver` | 从 AOP JoinPoint 解析注解（方法优先，类兜底）            |

---

## 8. 常见问题

**Q：校验失败返回 400 而不是 200？**
A：遵循 HTTP 语义。客户端传错参数属于 `4xx`，业务系统必须正确表达。

**Q：可以全局关闭校验异常处理吗？**
A：提供自己的 `ValidationExceptionAdvice` Bean，原 `@ConditionalOnMissingBean` 生效时不会注册默认的。

**Q：自定义错误码和 HttpStatus 怎么配合？**
A：`Response(code, message, data)` 中 `code` 是业务码或 HTTP 码均可。建议业务码单独定义枚举，避免与 HTTP 码混用。
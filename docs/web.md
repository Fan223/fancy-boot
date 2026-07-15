# Web 与统一响应

本文档覆盖 `fancy-web-spring-boot-starter`、`fancy-validation-spring-boot-starter`，以及 `fancy-boot-core` 中的
`Response` / `HttpStatus`。

---

## 1. 统一响应 Response

### 数据结构

```java
public record Response<T>(int code, String message, T data) { }
```

序列化时 `null` 字段会自动忽略（`@JsonInclude(NON_NULL)`）。

### 构造方式

```java
// 成功（推荐使用 HttpStatus 枚举）
return Response.of(HttpStatus.OK, "查询成功", user);

// 无数据成功
return Response.of(HttpStatus.NO_CONTENT, "操作成功");

// 失败（默认 500）
return Response.fail("系统繁忙");

// 失败（指定状态码）
return Response.fail(HttpStatus.NOT_FOUND, "用户不存在");
return Response.of(HttpStatus.BAD_REQUEST, "参数错误", fieldErrors);
```

> `HttpStatus` 是项目自实现的枚举，完整覆盖 HTTP 1xx ~ 5xx，参见 [
`fancy.boot.core.http.HttpStatus`](fancy-boot-core/src/main/java/fancy/boot/core/http/HttpStatus.java)。

### 自定义状态码

`Response` 只负责包装，业务层不强制使用 `HttpStatus`：

```java
return Response.of(90001, "业务自定义错误", null);
```

---

## 2. 全局异常处理

`fancy-web-spring-boot-starter` 提供兜底的全局异常处理器，捕获未处理的 `Exception` 并返回 `Response.fail`。

```java
@ExceptionHandler(Exception.class)
public Response<String> handleOtherException(Exception exception) {
    log.error("系统异常: {}", exception.getMessage(), exception);
    return Response.fail("系统异常: " + exception.getMessage());
}
```

业务方应在自己的 starter 或服务中定义更具体的异常处理器（顺序在兜底之前）。

---

## 3. 参数校验

引入 [`fancy-validation-spring-boot-starter`](../fancy-boot-starters/fancy-validation-spring-boot-starter) 后，会自动注册
`ValidationExceptionAdvice`，统一处理以下 4 种校验异常：

| 异常                                 | 触发场景                                                 |
|------------------------------------|------------------------------------------------------|
| `ConstraintViolationException`     | `@RequestParam` / `@PathVariable` / 单参数 `@Validated` |
| `MethodArgumentNotValidException`  | `@RequestBody @Valid`                                |
| `BindException`                    | 表单绑定（`application/x-www-form-urlencoded`）            |
| `HandlerMethodValidationException` | Spring 6.1+ 控制器方法参数校验                                |

### 响应结构

校验失败时返回 `400`，`data` 字段为 `List<FieldErrorDetail>`：

```json
{
  "code": 400,
  "message": "实体类参数校验失败",
  "data": [
    { "field": "username", "message": "用户名不能为空", "rejectedValue": "" },
    { "field": "age",      "message": "年龄必须大于 0",  "rejectedValue": -1 }
  ]
}
```

`FieldErrorDetail` 定义：

```java
public record FieldErrorDetail(String field, String message, Object rejectedValue) { }
```

### 使用方式

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public Response<UserVO> getById(@PathVariable @Min(1) Long id) {
        // id <= 0 时返回 400 + 上述 data 结构
        return Response.of(HttpStatus.OK, "查询成功", userService.getById(id));
    }

    @PostMapping
    public Response<UserVO> create(@RequestBody @Valid UserCreateRequest request) {
        return Response.of(HttpStatus.CREATED, "创建成功", userService.create(request));
    }
}

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

> **注意**：`@RequestBody` 触发的校验，需要在 DTO 上标注 `@Valid`（或 `@Validated`）；简单参数校验需要在 Controller 类上标注
`@Validated`。

### 编程式校验

除了注解方式，还可以通过 `ValidatorService` 手动校验：

```java
@RequiredArgsConstructor
@Service
public class UserService {

    private final ValidatorService validatorService;

    public void save(UserDTO dto) {
        validatorService.validate(dto, SaveGroup.class);
        // ...
    }
}
```

校验失败时会抛出 `ConstraintViolationException`，由 `ValidationExceptionAdvice` 统一处理。

### 日志行为

- 校验失败默认以 `WARN` 级别记录，避免污染 `ERROR` 日志
- 异常堆栈仅在 `DEBUG` 级别输出，避免噪音

### 覆盖默认行为

`ValidationExceptionAdvice` 与 `ValidatorService` 都标注了 `@ConditionalOnMissingBean`，可通过自定义 Bean 覆盖：

```java
@Bean
public ValidationExceptionAdvice customValidationAdvice() {
    return new ValidationExceptionAdvice() {
        // 自定义处理逻辑
    };
}
```

---

## 4. 客户端请求

`fancy-web-spring-boot-starter` 默认注册了 `RestClient` 与 `WebClient.Builder`：

```java
@RequiredArgsConstructor
@Service
public class ExternalService {

    private final RestClient restClient;
    private final WebClient.Builder webClientBuilder;

    public UserDTO getUser(Long id) {
        return restClient.get()
                .uri("https://api.example.com/users/{id}", id)
                .retrieve()
                .body(UserDTO.class);
    }
}
```

`WebClient.Builder` 默认取消了内存缓冲区大小限制（`maxInMemorySize = -1`），适合大报文场景。

---

## 5. 工具类

`fancy-boot-core` 提供了一些常用工具，统一在 `fancy.boot.core.*` 包下：

- `fancy.boot.core.id.IdUtils` —— Snowflake ID 生成
- `fancy.boot.core.lang.StringUtils` —— 字符串工具
- `fancy.boot.core.lang.CharUtils` —— 字符工具
- `fancy.boot.core.net.NetUtils` —— 网络工具
- `fancy.boot.core.system.ProcessUtils` —— 进程工具
- `fancy.boot.core.net.NetException` —— 网络异常

---

## 6. 常见问题

**Q：为什么校验失败返回 400 而不是 200？**
A：遵循 HTTP 语义。客户端传错参数属于 `4xx`，业务系统必须正确表达。

**Q：可以全局关闭校验异常处理吗？**
A：提供自己的 `ValidationExceptionAdvice` Bean 即可，原 @ConditionalOnMissingBean 生效时不会注册默认的。

**Q：自定义错误码和 HttpStatus 怎么配合？**
A：`Response(code, message, data)` 中 `code` 是业务码或 HTTP 码均可。建议业务码单独定义枚举，避免与 HTTP 码混用。
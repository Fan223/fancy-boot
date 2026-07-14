# 资源服务器（OAuth2 JWT）

封装 OAuth2 资源服务器配置：JWT 解析、权限映射、CORS、内部 Token 透传。  
基于 Spring Security 7，同时支持 **Servlet** 与 **Reactive** 两套自动配置。

## 引入

```xml

<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-resource-server-spring-boot-starter</artifactId>
</dependency>
```

> starter 已包含 `spring-boot-starter-oauth2-resource-server`，只需在 application.yml 指定 issuer / jwk-set-uri 等。

---

## 自动配置

starter 启动时会根据 Web 栈自动装配：

| Web 栈    | 启用配置类                                     | 引入依赖                          |
|----------|-------------------------------------------|-------------------------------|
| Servlet  | `ServletResourceServerAutoConfiguration`  | `spring-boot-starter-web`     |
| Reactive | `ReactiveResourceServerAutoConfiguration` | `spring-boot-starter-webflux` |

两者通过 `@ConditionalOnWebApplication(type = SERVLET/REACTIVE)` 自动二选一。

---

## 配置

```yaml
fancy:
  security:
    jwt:
      claim-name: authorities              # 从 JWT Claims 中读取权限列表的 Key
      authority-prefix: ""                 # 权限前缀, 为空则不去前缀
    internal-token:
      token: "internal-shared-secret"      # 内部服务调用共享 Token, 为空则不启用内部认证过滤器
    cors:
      allowed-origins: [ "*" ]
      allowed-methods: [ "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD" ]
      allowed-headers: [ "*" ]
      allow-credentials: true
      max-age: 3600                        # 预检请求缓存时间(秒)

# Spring Security 官方配置(必须)
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server
          # 或使用 jwk-set-uri
          # jwk-set-uri: https://your-auth-server/.well-known/jwks.json
```

---

## 默认行为

| 项              | 默认值              |
|----------------|------------------|
| `/api/**`      | 放行               |
| `/actuator/**` | 检测到 actuator 时放行 |
| 其它路径           | 全部需要认证           |
| JWT 权限 Key     | `authorities`    |
| 权限前缀           | 空（不处理）           |
| CORS           | 全开放              |

---

## 自定义授权规则

实现 `ServletAuthorizeCustomizer` / `ReactiveAuthorizeCustomizer`，加入 Spring 容器即可：

```java

@Component
public class CustomAuthorizeCustomizer implements ServletAuthorizeCustomizer {

    @Override
    public void customize(ExpressionInterceptUrlRegistry registry) {
        registry.requestMatchers("/admin/**").hasRole("ADMIN");
        registry.requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("SCOPE_delete");
    }
}
```

多个 `Customizer` 会按 `@Order` 顺序依次执行。

---

## 内部服务调用

设置 `fancy.security.internal-token.token` 后，框架会自动注册内部认证过滤器。  
下游服务可通过请求头 `X-Internal-Token` 传递共享 Token，绕过 JWT 校验。

```bash
curl -H "X-Internal-Token: internal-shared-secret" http://service/internal-api
```

> **生产环境务必使用 HTTPS + 短 Token**，避免泄漏。

---

## 自定义未授权响应

默认 `AuthenticationEntryPoint` 与 `AccessDeniedHandler` 返回统一格式：

```json
{
  "code": 401,
  "message": "未认证"
}
```

可自定义：

```java

@Bean
public ServletAuthenticationEntryPoint servletAuthenticationEntryPoint(JsonMapper jsonMapper) {
    return new ServletAuthenticationEntryPoint(jsonMapper) {
        @Override
        protected Response<Void> buildResponse(WebExchangeExchange exchange, AuthenticationException ex) {
            return Response.of(901, "请先登录", null);
        }
    };
}
```

---

## `@EnableMethodSecurity`

两个自动配置类都已启用：

- Servlet：`@EnableMethodSecurity` → 支持 `@PreAuthorize` / `@PostAuthorize` 等注解
- Reactive：`@EnableReactiveMethodSecurity`

```java

@PreAuthorize("hasAuthority('user:read')")
@GetMapping("/users/{id}")
public UserVO getUser(@PathVariable Long id) { ...}
```

---

## 常见问题

**Q：JWT 权限前缀是什么场景？**
A：当 JWT 中权限是 `["ROLE_admin"]` 这类带前缀的字符串，前端又期望不显示前缀时，设 `authority-prefix=""` 会去除。  
若 JWT 中存储的是 `["admin"]` 而你想要 `ROLE_admin` 的 GrantedAuthority，设 `authority-prefix="ROLE_"`。

**Q：同时引入 web 和 webflux？**
A：Spring Boot 会依据 classpath 优先级自动选择。建议业务方只引入一套，避免冲突。

**Q：怎么关闭内部 Token 功能？**
A：不配置 `fancy.security.internal-token.token`（留空），过滤器不会被注册（`@ConditionalOnProperty` 控制）。
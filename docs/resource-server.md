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

> starter 已包含 `spring-boot-starter-oauth2-resource-server`。

---

## 自动配置

starter 启动时会根据 Web 栈自动装配：

| Web 栈    | 启用配置类                                     | 引入依赖                          |
|----------|-------------------------------------------|-------------------------------|
| Servlet  | `ServletResourceServerAutoConfiguration`  | `spring-boot-starter-web`     |
| Reactive | `ReactiveResourceServerAutoConfiguration` | `spring-boot-starter-webflux` |

两者通过 `@ConditionalOnWebApplication(type = SERVLET/REACTIVE)` 自动二选一。两个配置类已分别启用 `@EnableWebSecurity` +
`@EnableMethodSecurity`、`@EnableWebFluxSecurity` + `@EnableReactiveMethodSecurity`。

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

`/api/**` 与 `/actuator/**` 由 starter 内的两个默认 `Customizer` 实现，业务方实现自己的 `*AuthorizeCustomizer` 可覆盖或追加规则。

---

## 自定义授权规则

实现 `ServletAuthorizeCustomizer` / `ReactiveAuthorizeCustomizer`，加入 Spring 容器即可：

```java
@Component
public class CustomAuthorizeCustomizer implements ServletAuthorizeCustomizer {

    @Override
    public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry.requestMatchers("/admin/**").hasRole("ADMIN");
        registry.requestMatchers(HttpMethod.DELETE, "/**").hasAuthority("SCOPE_delete");
    }
}
```

多个 `Customizer` 按 `@Order` 顺序依次执行（`ObjectProvider.orderedStream()`）。

---

## 内部服务调用

设置 `fancy.security.internal-token.token` 后，starter 自动注册内部认证过滤器。  
下游服务可通过请求头 `X-Internal-Token` 传递共享 Token，绕过 JWT 校验。

```bash
curl -H "X-Internal-Token: internal-shared-secret" http://service/internal-api
```

实现要点：

- Token 比较使用 `MessageDigest.isEqual`，防时序攻击
- 内部认证主体标识 `internal-service`，写入 `SecurityContext`
- `Servlet` 版通过 `addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)` 注入到过滤器链
- `Reactive` 版通过 `addFilterBefore(filter, SecurityWebFiltersOrder.AUTHENTICATION)` 注入

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

响应状态码：`401 Unauthorized` / `403 Forbidden`。

可通过提供自己的 Bean 覆盖（`@ConditionalOnMissingBean`）。

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

## 自动注册 Bean

| Bean                                                          | Servlet / Reactive            |
|---------------------------------------------------------------|-------------------------------|
| `SecurityFilterChain` / `SecurityWebFilterChain`              | 默认链                           |
| `AuthenticationEntryPoint` / `ServerAuthenticationEntryPoint` | 401 处理                        |
| `AccessDeniedHandler` / `ServerAccessDeniedHandler`           | 403 处理                        |
| `JwtAuthenticationConverter` / Reactive 适配器                   | JWT 权限解析                      |
| `CorsConfigurationSource` / `CorsWebFilter`                   | CORS 配置                       |
| `*InternalAuthenticationFilter`（条件注册）                         | 内部 Token 透传                   |
| `*AuthorizeCustomizer`（默认 2 个）                                | `/api/**` + `/actuator/**` 放行 |

---

## 常见问题

**Q：JWT 权限前缀是什么场景？**
A：当 JWT 中权限是 `["ROLE_admin"]` 这类带前缀的字符串，前端又期望不显示前缀时，设 `authority-prefix=""` 会去除。  
若 JWT 中存储的是 `["admin"]` 而你想要 `ROLE_admin` 的 GrantedAuthority，设 `authority-prefix="ROLE_"`。

**Q：同时引入 web 和 webflux？**
A：Spring Boot 会依据 classpath 优先级自动选择。建议业务方只引入一套，避免冲突。

**Q：怎么关闭内部 Token 功能？**
A：不配置 `fancy.security.internal-token.token`（留空），过滤器不会被注册（
`@ConditionalOnProperty(prefix = "fancy.security.internal-token", name = "token")` 控制）。

**Q：内部认证过滤器与 JWT 校验顺序？**
A：内部过滤器在 JWT 之前执行（Servlet 通过 `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`，Reactive 通过
`addFilterBefore(..., SecurityWebFiltersOrder.AUTHENTICATION)`），保证内部 Token 优先于 JWT。
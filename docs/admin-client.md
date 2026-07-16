# Spring Boot Admin Client

将业务服务注册到 Spring Boot Admin Server，被动上报健康/指标/JVM/日志等。  
通过 `EnvironmentPostProcessor` 在 Spring Boot 启动最早期注入默认配置，业务方**零配置**即可被监控。

## 引入

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-admin-client-spring-boot-starter</artifactId>
</dependency>
```

> starter 已包含 `spring-boot-admin-starter-client` 与 `spring-boot-starter-actuator`。

---

## 默认注入的启动配置

启动时（任何业务配置加载之前）会自动注入以下默认值，**业务方显式配置则不覆盖**：

| 配置项                                                   | 默认值                      | 说明              |
|-------------------------------------------------------|--------------------------|-----------------|
| `spring.boot.admin.client.url`                        | `http://localhost:12000` | Admin Server 地址 |
| `spring.boot.admin.client.instance.service-host-type` | `IP`                     | 客户端上报的地址类型      |
| `management.endpoints.web.exposure.include`           | `*`                      | Actuator 端点全部暴露 |

> `*` 仅用于**开发/调试**。生产环境应显式指定要暴露的端点，避免泄漏敏感信息（如 `/heapdump`、`/env`、`/configprops`）。

### 业务方覆盖

```yaml
spring:
  application:
    name: order-service
  boot:
    admin:
      client:
        url: http://admin.internal:12000
        instance:
          service-host-type: HOST_NAME
          metadata:
            environment: production

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus  # 显式列出, 不要用 *
```

---

## 行为说明

- **不会自动启动 Admin Server**，仅注册客户端
- 默认 port 由 `spring-boot-admin-starter-client` 决定（无独立端口，与业务服务共用）
- 启动后会在日志看到类似：`registered application as application ... with admin server`
- 通过 `spring.boot.admin.client.period`（默认 10s）周期性上报

---

## 完整 application.yml 示例

```yaml
spring:
  application:
    name: order-service
  boot:
    admin:
      client:
        url: http://admin.internal:12000
        username: ${ADMIN_USERNAME:admin}     # 若 Server 端启用了安全认证
        password: ${ADMIN_PASSWORD:admin}
        instance:
          service-host-type: IP
          metadata:
            environment: production
            version: @project.version@

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus, loggers
  endpoint:
    health:
      show-details: when_authorized
```

---

## 配合 Admin Server 使用

```yaml
# Admin Server 端
spring:
  boot:
    admin:
      discovery:
        services-host-type: IP
```

更多 Admin Server 配置请参考 [Spring Boot Admin 官方文档](https://codecentric.github.io/spring-boot-admin/current/)。

---

## 实现原理

```java
public class AdminClientEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        properties.putIfAbsent("spring.boot.admin.client.url", "http://localhost:12000");
        properties.putIfAbsent("spring.boot.admin.client.instance.service-host-type", "IP");
        properties.putIfAbsent("management.endpoints.web.exposure.include", "*");

        environment.getPropertySources().addLast(new MapPropertySource("adminClient", properties));
    }
}
```

- 在 `EnvironmentPostProcessor` 阶段注入，比 `@AutoConfiguration` 更早
- `putIfAbsent` 保证**不覆盖**业务方在 `application.yml` 中的显式配置
- `addLast` 保证优先级最低，让业务配置优先

通过 `META-INF/spring.factories` 的 `EnvironmentPostProcessor` key 注册到 Spring Boot：

```
org.springframework.boot.EnvironmentPostProcessor=\
    fancy.starter.client.admin.bootstrap.AdminClientEnvironmentPostProcessor
```

---

## 常见问题

**Q：客户端连不上 Admin Server？**
A：检查三项：

1. `spring.boot.admin.client.url` 配置正确
2. Admin Server 端口可达（防火墙 / 安全组）
3. 若 Server 启用了 basic auth，客户端需配 `username` / `password`

**Q：生产环境 `*` 暴露 actuator 端点安全吗？**
A：不安全。Actuator 默认端口和应用端口共享，会暴露：

- `/heapdump` — JVM 堆
- `/env` — 环境变量（含密码）
- `/configprops` — 全部 `@ConfigurationProperties`
- `/shutdown` — 远程关闭应用

生产环境应只暴露必要端点，并加 `spring.security` 鉴权。

**Q：客户端会主动向 Server 推数据吗？**
A：是的。`spring-boot-admin-client` 通过周期性 HTTP 请求（默认 10s）向 Server 上报实例信息。客户端**不会**等 Server 拉取。

**Q：客户端心跳失败会怎样？**
A：实例状态显示为 `OFFLINE`，但应用本身继续运行。Server 端可配置告警。

**Q：可以注册多个 Admin Server 吗？**
A：可以，`url` 配置支持逗号分隔：`http://admin1:12000,http://admin2:12000`。
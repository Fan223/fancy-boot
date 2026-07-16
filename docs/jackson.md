# Jackson

基于 Jackson 3 的全局 `JsonMapper` 配置：忽略 `null` 字段、忽略 `isGetter` 方法。

## 引入

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-jackson-spring-boot-starter</artifactId>
</dependency>
```

> starter 已包含 `spring-boot-starter-jackson`。Spring Boot 4 默认使用 Jackson 3，databind 包名
`tools.jackson.databind.*`。

> 关于注解包：Jackson 3 把 databind / core 迁到了 `tools.jackson.*`，但 Jackson 注解（`@JsonInclude`、`@JsonAutoDetect` 等）*
*仍位于** `com.fasterxml.jackson.annotation.*`。业务方继续用 Jackson 2 注解即可，Jackson 3 兼容 2.x 注解，无需切换 import。

---

## 默认行为

| 项                 | 默认   | 说明                                 |
|-------------------|------|------------------------------------|
| 序列化忽略 `null` 字段   | ✅    | `JsonInclude.Include.NON_NULL`     |
| 序列化忽略 `null` 集合元素 | ✅    | `withContentInclusion(NON_NULL)`   |
| `isGetter` 序列化方法  | ❌ 忽略 | 如 `isActive()` 不会被序列化为 `active` 字段 |
| 字段重命名             | 默认   | 字段名直接序列化，需重命名时用 `@JsonProperty`    |

完整配置：

```java
JsonMapper.builder()
        .changeDefaultVisibility(vc -> vc
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
        .changeDefaultPropertyInclusion(incl -> incl
                .withValueInclusion(JsonInclude.Include.NON_NULL)
                .withContentInclusion(JsonInclude.Include.NON_NULL))
        .build();
```

---

## 自动配置

starter 注册一个 `tools.jackson.databind.json.JsonMapper` Bean，业务项目直接注入使用：

```java

@Service
@RequiredArgsConstructor
public class ExportService {

    private final JsonMapper jsonMapper;

    public String export(Object data) {
        return jsonMapper.writeValueAsString(data);
    }

    public <T> T parse(String json, Class<T> clazz) {
        return jsonMapper.readValue(json, clazz);
    }

    public <T> T convert(Object source, Class<T> target) {
        return jsonMapper.convertValue(source, target);
    }
}
```

> 也可调用 `jsonMapper.convertValue(obj, TargetClass.class)` 在对象之间转换（参考 [Redis 服务](./redis.md) 中的用法）。

---

## 自定义 JsonMapper

需要扩展默认行为时，提供自己的 Bean 即可（`@ConditionalOnMissingBean` 兜底）：

```java

@Bean
public JsonMapper customJsonMapper() {
    return JsonMapper.builder()
            // 保留 starter 的全部默认
            .changeDefaultVisibility(vc -> vc
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
            // 增加自定义配置
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_ABSENT))
            .build();
}
```

---

## 与统一响应 Response 配合

[fancy-boot-core 中的 Response](../../fancy-boot-core/src/main/java/fancy/boot/core/http/Response.java) 已标注
`@JsonInclude(NON_NULL)`：

```java

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(int code, String message, T data) {
}
```

当 `data` 为 `null` 时，序列化结果不会包含 `data` 字段，与 starter 的全局配置保持一致。

---

## 常见问题

**Q：项目用了 Jackson 2 的 `@JsonInclude` 等注解，能继续用吗？**
A：可以。Spring Boot 4 默认同时引入 Jackson 3 databind（`tools.jackson.core:jackson-databind`）和 Jackson 2 注解包（
`com.fasterxml.jackson.core:jackson-annotations`），Jackson 3 兼容 2.x 注解，新旧代码可以共存。

**Q：业务实体类上的注解应该用哪个包？**
A：用 `com.fasterxml.jackson.annotation.*`。Jackson 3 在可预见未来都会保持这一注解包的兼容。

**Q：怎么关闭某个默认值？**
A：提供自己的 `JsonMapper` Bean 完整覆盖。starter 不提供细粒度配置开关（保持轻量）。

**Q：怎么调试序列化输出？**
A：在 log4j2.xml 中调整 `tools.jackson.databind` 包的 logger 级别到 `DEBUG`。
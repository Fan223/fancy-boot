# MyBatis-Plus

封装 MyBatis-Plus 常用插件、字段自动填充和代码生成器，集成 P6Spy SQL 日志。

## 引入

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-mybatis-plus-spring-boot-starter</artifactId>
</dependency>
```

> **前置依赖**：业务项目需要自行引入 JDBC 驱动（如 `mysql-connector-j`）。

---

## 自动配置

### 内置插件

starter 默认注册 `MybatisPlusInterceptor`，按以下顺序添加 inner interceptor：

| 插件       | 默认 | 配置项                                   | 说明                                        |
|----------|----|---------------------------------------|-------------------------------------------|
| 乐观锁      | ✅  | `fancy.mybatis-plus.optimisticLocker` | `OptimisticLockerInnerInterceptor`        |
| 防全表更新/删除 | ✅  | `fancy.mybatis-plus.blockAttack`      | `BlockAttackInnerInterceptor`             |
| 分页       | ✅  | `fancy.mybatis-plus.pagination`       | `PaginationInnerInterceptor`，需指定 `dbType` |

> 拦截器链顺序由注册顺序决定：`乐观锁 → 防全表 → 分页`。分页插件放在最后，与 MyBatis-Plus 官方建议一致。

### 数据库类型

```yaml
fancy:
  mybatis-plus:
    db-type: MySQL  # MySQL / PostgreSQL / Oracle / SQLServer 等，参见 DbType 枚举
```

启动时由 `DbType.getDbType(...)` 转为 `com.baomidou.mybatisplus.annotation.DbType`；配置错误抛 `IllegalArgumentException`
并提示原始字符串。

### 关闭某个插件

```yaml
fancy:
  mybatis-plus:
    optimistic-locker: false  # 关闭乐观锁
    pagination: false         # 关闭分页
```

---

## 字段自动填充

继承 `fancy.starter.mybatis.plus.entity.MetaDO`，自动获得字段填充：

| 字段           | 类型              | 插入 | 更新 | 说明           |
|--------------|-----------------|----|----|--------------|
| `id`         | `Long`          | ✅  | —  | Snowflake ID |
| `deleteTime` | `LocalDateTime` | —  | —  | 逻辑删除字段       |
| `createTime` | `LocalDateTime` | ✅  | —  | 插入时填充        |
| `updateTime` | `LocalDateTime` | ✅  | ✅  | 插入或更新时填充     |

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends MetaDO {
    private String username;
}
```

> 原理：`FancyMetaObjectHandler` 通过 `LambdaUtils.getFieldName(MetaDO::getId)` 等方法拿到字段名（避开硬编码）。
`@TableId(type = INPUT)` 不走 MyBatis-Plus 的 `IdentifierGenerator`，由 `MetaObjectHandler` 直接 `setFieldValByName` 写入。

---

## 分页查询参数

`PageQuery` 提供默认值兜底的分页参数：

```java
@Getter
@Setter
public class PageQuery {
    private Integer currentPage;  // < 1 时返回 1
    private Integer pageSize;     // < 1 时返回 10
}
```

```java
@GetMapping
public Response<Page<UserVO>> list(PageQuery query) {
    Page<User> page = PageUtil.of(query);  // 业务方自行组装 MyBatis-Plus 的 Page
    userService.page(page);
    return Response.of(HttpStatus.OK.getValue(), "查询成功", page);
}
```

`PageQuery` 仅提供参数承载与默认值兜底，不负责转换为 MyBatis-Plus 的 `Page` 对象。

---

## 代码生成器

基于 MyBatis-Plus 的 `FastAutoGenerator` + Freemarker 模板。

### 引入依赖

代码生成器依赖默认是 `optional`，需要在业务项目中显式引入：

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-generator</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
```

### 使用方式

```java
public class CodeGen {

    static void main(String[] args) {
        CodeProperties props = CodeProperties.builder()
                .url("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf8")
                .username("root")
                .password("root")
                .outputDir("D:/projects/demo/src/main/java")
                .parent("com.demo")
                .moduleName("user")
                .tablePrefix("t_")
                .build();

        // 按表名生成
        CodeGenerator.generate(props, "t_user", "t_role");

        // 按前缀后缀批量生成
        // CodeGenerator.generateAll(props);
    }
}
```

### 输出结构

代码生成器按以下目录输出（以 `parent="com.demo"`、`moduleName="user"` 为例）：

```
com.demo.user
├── entity
├── mapper
│   └── xml
├── service
│   └── impl
└── ...
```

### 内置约定

- 实体统一启用 Lombok
- 类型映射：`SMALLINT` → `INTEGER`
- 模板引擎：Freemarker（默认 Velocity）
- 注释日期格式：`yyyy-MM-dd HH:mm:ss`
- 启用 Swagger 注解

---

## SQL 日志（P6Spy）

`P6SpyEnvironmentPostProcessor` 在 Spring Boot 启动早期注入 P6Spy 默认日志格式：

```
分类: %(category) | 耗时: %(executionTime)ms | %(newline)\t%(sql)
```

注意换行使用 P6Spy 占位符 `%(newline)`，不是字面 `\n`。

业务方引入 `p6spy-spring-boot-starter`（spring.factories 中通过 `EnvironmentPostProcessor` key 注册）后即可生效；如需关闭或自定义格式，请覆盖
`decorator.datasource.p6spy.log-format`。

---

## 常见问题

**Q：`@TableId` 自增 ID 与 Snowflake 冲突怎么办？**
A：`MetaDO` 默认走 Snowflake（`@TableId(type = INPUT)` + handler 填充）。若实体使用自增 ID，不要继承 `MetaDO` 的 `id`
字段，或在实体上自定义 `@TableId(type = IdType.AUTO)`。注意 Snowflake 与 `@TableId` 是两条独立路径，不会冲突。

**Q：分页插件的顺序？**
A：分页插件必须放在拦截器链的最末端。`MyBatisPlusAutoConfiguration` 已按"乐观锁 → 防全表 → 分页"顺序注册，保持默认即可。

**Q：想加自定义 `MetaObjectHandler`？**
A：starter 注册 MetaObjectHandler 时使用了 `@ConditionalOnMissingBean(MetaObjectHandler.class)`，提供自己的 Bean 即可。
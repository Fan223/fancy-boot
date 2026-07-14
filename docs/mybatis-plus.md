# MyBatis-Plus

封装 MyBatis-Plus 常用插件、字段自动填充和代码生成器，集成 P6Spy SQL 日志。

## 引入

```xml

<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-mybatis-plus-spring-boot-starter</artifactId>
</dependency>
```

> **前置依赖**：业务项目需要自行引入 JDBC 驱动和数据库连接池（如 `mysql-connector-j`、`HikariCP` 由
`spring-boot-starter-jdbc` 间接提供）。

## 自动配置

### 内置插件

| 插件       | 默认 | 配置项                                   | 说明                                        |
|----------|----|---------------------------------------|-------------------------------------------|
| 乐观锁      | ✅  | `fancy.mybatis-plus.optimisticLocker` | `OptimisticLockerInnerInterceptor`        |
| 防全表更新/删除 | ✅  | `fancy.mybatis-plus.blockAttack`      | `BlockAttackInnerInterceptor`             |
| 分页       | ✅  | `fancy.mybatis-plus.pagination`       | `PaginationInnerInterceptor`，需指定 `dbType` |

### 数据库类型

```yaml
fancy:
  mybatis-plus:
    db-type: MySQL  # 支持 MySQL/PostgreSQL/Oracle/SQLServer 等，参见 DbType 枚举
```

启动时自动转为 `com.baomidou.mybatisplus.annotation.DbType`。  
若配置错误，抛 `IllegalArgumentException` 并提示原始字符串。

### 关闭某个插件

```yaml
fancy:
  mybatis-plus:
    optimistic-locker: false  # 关闭乐观锁
```

---

## 字段自动填充

继承 `fancy.starter.mybatis.plus.entity.MetaDO`，自动获得如下字段填充：

| 字段           | 插入 | 更新 | 说明              |
|--------------|----|----|-----------------|
| `id`         | ✅  | —  | Snowflake ID    |
| `createTime` | ✅  | —  | `LocalDateTime` |
| `updateTime` | ✅  | ✅  | `LocalDateTime` |

```java

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends MetaDO {
    private String username;
}
```

> **原理**：`FancyMetaObjectHandler` 通过 `LambdaUtils.getFieldName(MetaDO::getId)` 等方法拿到字段名，实现"以字段名引用"
> 而非硬编码。业务方自定义基类继承 `MetaDO` 后无需任何额外配置。

---

## 分页查询

`PageQuery` 提供默认值兜底的分页参数：

```java

@GetMapping
public Response<Page<UserVO>> list(PageQuery query) {
    Page<User> page = PageUtil.of(query);  // 业务方组装
    userService.page(page);
    return Response.of(HttpStatus.OK, "查询成功", PageUtil.toVO(page, this::toVO));
}
```

`PageQuery` 字段：

| 字段            | 默认 | 范围     |
|---------------|----|--------|
| `currentPage` | 1  | `>= 1` |
| `pageSize`    | 10 | `>= 1` |

---

## 代码生成器

代码生成器基于 MyBatis-Plus 的 `FastAutoGenerator` + Freemarker 模板。

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
├── controller
├── service
│   └── impl
├── mapper
│   └── xml
├── entity
└── ...
```

### 自定义约定

- 实体统一启用 Lombok
- 类型映射：`SMALLINT` → `INTEGER`
- 模板引擎：Freemarker（默认 Velocity）
- 注释日期格式：`yyyy-MM-dd HH:mm:ss`
- 启用 Swagger 注解

---

## SQL 日志（P6Spy）

引入 starter 时已自动包含 `p6spy-spring-boot-starter`，执行 SQL 与耗时会被打印到日志。

如需关闭，参考 P6Spy 官方配置：

```yaml
decorator:
  datasource:
    enabled: false
```

---

## 常见问题

**Q：`@TableId` 自增 ID 与 Snowflake 冲突怎么办？**
A：`MetaDO` 默认走 Snowflake。若实体使用自增 ID，不要继承 `MetaDO` 的 `id` 字段，或在实体上自定义
`@TableId(type = IdType.AUTO)`。注意 Snowflake 由 starter 注入，与 `@TableId` 不冲突（前者走 `MetaObjectHandler`，后者走
`IdentifierGenerator`，这是两条独立路径）。

**Q：分页插件的顺序？**
A：分页插件必须放在拦截器链的最末端。`MyBatisPlusAutoConfiguration` 已按"乐观锁 → 防全表 → 分页"顺序注册，保持默认即可。

**Q：想加自定义 `MetaObjectHandler`？**
A：starter 注册 MetaObjectHandler 时使用了 `@ConditionalOnMissingBean(MetaObjectHandler.class)`，提供自己的 Bean 即可。
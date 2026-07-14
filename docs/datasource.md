# 动态数据源

基于 `@Ds` 注解 + Spring `AbstractRoutingDataSource` 的动态数据源切换。  
业务方实现 `DataSourceProvider` 即可在**应用启动后**动态加载数据源列表。

## 引入

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-datasource-spring-boot-starter</artifactId>
</dependency>
```

> starter 已包含 `fancy-boot-core`、`spring-boot-starter-jdbc`、`spring-boot-starter-aop`。

---

## 工作原理

```
方法调用 @Ds("slave_1")
   ↓
DsAspect 切面拦截
   ↓
DataSourceContextHolder.push("slave_1")   ← ThreadLocal 栈顶
   ↓
DynamicRoutingDataSource.determineTargetDataSource()
   ↓
DynamicDataSourceManager.get("slave_1")   ← 查 Map
   ↓
返回对应的 HikariDataSource
   ↓
执行原方法
   ↓
finally: DataSourceContextHolder.poll()    ← 出栈
```

数据源在 `ApplicationReadyEvent` 阶段加载（启动完成后）。

---

## 三步使用

### 1. 定义数据源模型

```java
List<DataSourceModel> models = List.of(
        new DataSourceModel(
                "master",                       // 唯一标识码
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://master:3306/db",
                "root",
                "root",
                Map.of("charset", "UTF-8")
        ),
        new DataSourceModel(
                "slave_1",
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://slave1:3306/db",
                "readonly",
                "readonly",
                Map.of("charset", "UTF-8")
        )
);
```

`code` 必须**全局唯一**，与 `@Ds` 注解的 value 一致。

### 2. 实现 DataSourceProvider

```java
@Component
public class MyDataSourceProvider implements DataSourceProvider {

    @Override
    public List<DataSourceModel> load() {
        // 从配置中心 / 数据库 / 配置文件加载
        return models;
    }
}
```

> 没有 `DataSourceProvider` Bean 时，starter 正常启动，但**没有任何动态数据源**——所有查询走 defaultDataSource。

### 3. 使用 @Ds 注解

```java
@Ds("master")
public void save(Order order) {
    jdbcTemplate.update("insert into order ...");
}

@Ds("slave_1")
public List<Order> list() {
    return jdbcTemplate.query("select * from order");
}
```

`@Ds` 可标注在**类**或**方法**上：

- 类 + 方法：以方法为准
- 仅类：所有方法用同一数据源
- 仅方法：仅该方法切换

---

## 默认数据源

Spring Boot 的 `spring.datasource.*` 配置仍生效，starter 注册的 `dynamicRoutingDataSource` 标了 `@Primary`，业务代码注入
`DataSource` 拿到的是动态路由版本。

当 `@Ds` 注解的 code 找不到时，**回退到默认数据源**。默认数据源就是 Spring Boot 启动时按 `spring.datasource.*` 创建的
Bean。

```yaml
spring:
  datasource:
    url: jdbc:mysql://default:3306/db
    username: root
    password: root
```

---

## 字符集处理

`DataSourceModel.properties` 中可配置 `charset`：

```java
new DataSourceModel("slave_1", driver, url, user, pass, Map.of("charset", "GBK"));
```

- 留空或缺省：使用 Spring 默认 `ColumnMapRowMapper`
- 配置合法（如 `UTF-8` / `GBK`）：查询 `CHAR / VARCHAR / LONGVARCHAR` 列时**先按 bytes 读取再按指定 charset 解码**
- 配置非法：回退到默认 mapper，并打 `WARN` 日志

> **常见场景**：MySQL 默认字符集是 `latin1`，存了中文 → 配 `charset=GBK` 或 `UTF-8` 解码。

---

## 动态刷新

`DynamicDataSourceManager.refresh()` 在运行时重新加载数据源列表：

```java
@Autowired
private DynamicDataSourceManager manager;

// 当配置变更时, 业务方主动调用
manager.refresh();
```

逻辑：

- 新增数据源 → 立即生效
- 删除数据源 → 关闭连接池, 立即失效
- 修改连接参数 → 关闭旧池, 创建新池

> 注意: `refresh()` 是 `synchronized`, 调用时所有查询会等待。

---

## 编程式切换数据源

```java
DataSourceContextHolder.push("slave_1");
try {
    // 后续 JDBC 操作走 slave_1
    jdbcTemplate.query(...);
} finally {
    DataSourceContextHolder.poll();
}
```

适合拦截器、过滤器、自定义 AOP 等无法用 `@Ds` 注解的场景。

---

## 自动注册 Bean

| Bean                       | 说明                     |
|----------------------------|------------------------|
| `DsAspect`                 | 拦截 `@Ds` 注解            |
| `DynamicDataSourceManager` | 数据源管理（增删改查）            |
| `DataSource`（@Primary）     | 动态路由数据源                |
| `DynamicSqlExecutor`       | 编程式 SQL 执行器（无需关心数据源切换） |

所有 Bean 均标注 `@ConditionalOnMissingBean`，业务方可整体覆盖。

---

## 编程式 SQL 执行（DynamicSqlExecutor）

`DynamicSqlExecutor` 是封装好的"按数据源 code 执行 SQL"工具，省去手动切数据源：

```java
@Autowired
private DynamicSqlExecutor sqlExecutor;

List<Map<String, Object>> rows = sqlExecutor.query(
        "slave_1",
        "select * from order where id = :id",
        Map.of("id", 1)
);
```

API：

| 方法                                              | 说明     |
|-------------------------------------------------|--------|
| `query(code, sql, args...)`                     | 动态参数查询 |
| `query(code, sql, Map<String, Object>)`         | 命名参数查询 |
| `executeUpdate(code, sql, args...)`             | 动态参数更新 |
| `executeUpdate(code, sql, Map<String, Object>)` | 命名参数更新 |

---

## 常见问题

**Q：启动时报 "No default DataSource configured"？**
A：业务项目没有配置 `spring.datasource.*`，且没有 `DataSourceProvider`。至少配其中一个：

- 简单场景：配 `spring.datasource.url/username/password`
- 复杂场景：实现 `DataSourceProvider` 加载

**Q：`@Ds("xxx")` 切了数据源但没生效？**
A：检查：

1. 数据源 `code` 与 `@Ds` 值一致
2. `DataSourceProvider` 的 `load()` 返回列表里包含这个 code
3. 启动后查看日志确认 `init()` 被调用

**Q：业务代码注入 `DataSource` 拿到的是动态路由版本吗？**
A：是的。starter 注册的 `dynamicRoutingDataSource` 标了 `@Primary`，注入的 `DataSource` 默认就是它。

**Q：能配合 MyBatis / MyBatis-Plus 用吗？**
A：可以。`SqlSessionFactory` 注入的 `DataSource` 已经是动态路由版本，`@Ds` 在 Service 层切数据源, Mapper 透明。

**Q：数据源连接池大小怎么调？**
A：在 `DataSourceModel.properties` 里配置：

```java
new DataSourceModel("master", driver, url, user, pass, Map.of(
        "charset", "UTF-8",
        "maximumPoolSize", 20,
        "minimumIdle", 5,
        "connectionTimeout", 30000
));
```

HikariCP 全部参数都支持。

**Q：怎么关闭某个数据源？**
A：调用 `manager.remove("code")` 或在 `DataSourceProvider.load()` 返回列表中移除。

**Q：`DataSourceContextHolder.clear()` 何时使用？**
A：极少需要。`poll()` 已经在栈空时自动清理。线程池场景下，任务结束后正常会触发 `poll()`。只有当线程**异常**被复用、且想强制清空时才用。
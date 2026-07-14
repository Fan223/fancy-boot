# Fancy Boot

> 基于 Spring Boot 4 的脚手架，封装常用技术栈的自动配置

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## 特性

- **统一响应** —— 提供 `Response<T>` 统一接口返回格式，配合 starter 自动装配
- **统一异常处理** —— 内置业务异常与参数校验异常处理
- **Starter 化** —— 每个技术栈独立 starter，按需引入，不绑架依赖
- **现代 API** —— 基于 Spring Boot 4 / Java 25 / Jackson 3
- **开箱即用** —— 引入依赖即生效，零配置

---

## 模块结构

```
fancy-boot
├── fancy-boot-parent      # 统一父 POM，依赖管理 + 插件配置
├── fancy-boot-bom         # 业务方引入的 BOM，统一 starter 版本
├── fancy-boot-core        # 核心工具类（Response、HttpStatus、IdUtils、StringUtils）
└── fancy-boot-starters    # 所有 starter 聚合
    ├── fancy-web-spring-boot-starter
    ├── fancy-validation-spring-boot-starter
    ├── fancy-jackson-spring-boot-starter
    ├── fancy-log-spring-boot-starter
    ├── fancy-redis-spring-boot-starter
    ├── fancy-mybatis-plus-spring-boot-starter
    ├── fancy-datasource-spring-boot-starter
    ├── fancy-resource-server-spring-boot-starter
    └── fancy-admin-client-spring-boot-starter
```

---

## 快速开始

### 1. 引入 BOM

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>fan</groupId>
            <artifactId>fancy-boot-bom</artifactId>
            <version>4.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 按需引入 starter

下面以 Web 服务最常见的组合为例：

```xml

<dependencies>
    <!-- Web 基础（含 RestClient、WebClient、全局异常兜底） -->
    <dependency>
        <groupId>fan</groupId>
        <artifactId>fancy-web-spring-boot-starter</artifactId>
    </dependency>

    <!-- 参数校验 -->
    <dependency>
        <groupId>fan</groupId>
        <artifactId>fancy-validation-spring-boot-starter</artifactId>
    </dependency>

    <!-- Jackson 配置 -->
    <dependency>
        <groupId>fan</groupId>
        <artifactId>fancy-jackson-spring-boot-starter</artifactId>
    </dependency>

    <!-- 日志 -->
    <dependency>
        <groupId>fan</groupId>
        <artifactId>fancy-log-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. 编写 Controller

```java

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Response<UserVO> getById(@PathVariable @Min(1) Long id) {
        return Response.of(HttpStatus.OK, "查询成功", userService.getById(id));
    }

    @PostMapping
    public Response<UserVO> create(@RequestBody @Valid UserCreateRequest request) {
        return Response.of(HttpStatus.CREATED, "创建成功", userService.create(request));
    }
}
```

---

## Starter 清单

| Starter                                                                                  | 说明                                   | 适用场景              |
|------------------------------------------------------------------------------------------|--------------------------------------|-------------------|
| [`fancy-web`](fancy-boot-starters/fancy-web-spring-boot-starter)                         | Web 基础、RestClient / WebClient、全局异常兜底 | 任何 Spring Web 服务  |
| [`fancy-validation`](fancy-boot-starters/fancy-validation-spring-boot-starter)           | 参数校验异常统一处理                           | 需要 `@Valid` 校验的接口 |
| [`fancy-jackson`](fancy-boot-starters/fancy-jackson-spring-boot-starter)                 | Jackson 3 全局配置（null 忽略、isGetter 忽略）  | 所有 JSON 序列化场景     |
| [`fancy-log`](fancy-boot-starters/fancy-log-spring-boot-starter)                         | 接口出入参日志、TraceId 串联                   | 需要统一日志格式的服务       |
| [`fancy-redis`](fancy-boot-starters/fancy-redis-spring-boot-starter)                     | RedisTemplate + Redisson + 分布式锁      | 缓存、分布式锁           |
| [`fancy-mybatis-plus`](fancy-boot-starters/fancy-mybatis-plus-spring-boot-starter)       | MyBatis-Plus 集成 + 代码生成器              | MyBatis 项目        |
| [`fancy-datasource`](fancy-boot-starters/fancy-datasource-spring-boot-starter)           | 动态数据源切换                              | 多数据源读写分离          |
| [`fancy-resource-server`](fancy-boot-starters/fancy-resource-server-spring-boot-starter) | OAuth2 资源服务器（JWT 解析、CORS）            | 受保护的 API 服务       |
| [`fancy-admin-client`](fancy-boot-starters/fancy-admin-client-spring-boot-starter)       | Spring Boot Admin Client 注册          | 需要被 Admin 监控的服务   |

> 详细配置与进阶用法见 [docs/](docs/) 目录。

---

## 版本兼容

| Fancy Boot | Spring Boot | Java |
|------------|-------------|------|
| 4.1.x      | 4.1.x       | 25   |
| 4.0.x      | 4.0.x       | 25   |

---

## 文档导航

- [docs/web.md](docs/web.md) —— 统一响应、异常处理、参数校验
- [docs/log.md](docs/log.md) —— 日志组件使用
- [docs/jackson.md](docs/jackson.md) —— Jackson 配置说明
- [docs/mybatis-plus.md](docs/mybatis-plus.md) —— MyBatis-Plus 与代码生成
- [docs/resource-server.md](docs/resource-server.md) —— OAuth2 资源服务器
- [docs/redis.md](docs/redis.md) —— Redis 服务与分布式锁
- [docs/datasource.md](docs/datasource.md) —— 动态数据源
- [docs/admin-client.md](docs/admin-client.md) —— Spring Boot Admin Client

> AI 工具请同时阅读根目录 [AGENTS.md](AGENTS.md) 获取项目约定与硬性约束。

---

## 开发

```bash
# 编译全部模块
./mvnw clean install -DskipTests

# 编译单个模块
./mvnw -pl fancy-boot-starters/fancy-validation-spring-boot-starter clean compile
```

---

## 许可证

[MIT](LICENSE)
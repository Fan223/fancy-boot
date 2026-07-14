# Redis

封装 Spring Data Redis + Redisson，提供统一的 `RedisService` 操作 Redis（Key、5 种数据结构、分布式锁、原子类、布隆过滤器）。

## 引入

```xml

<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-redis-spring-boot-starter</artifactId>
</dependency>
```

> starter 已包含 `redisson-spring-boot-starter` 与 `spring-boot-starter-jackson`。

### 基础配置

使用 Spring Boot 官方 `spring.data.redis` 命名空间：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      username: default
      password: your-password
      database: 0
```

> `Redisson` 单机模式自动按上述配置装配，地址形如 `redis://host:port`。  
> 集群 / Sentinel / Master-Slave 模式需自行扩展 `RedissonClient` Bean。

---

## 自动注册 Bean

| Bean                            | 说明                               |
|---------------------------------|----------------------------------|
| `RedissonClient`                | Redisson 客户端，用于分布式锁、原子类、布隆过滤器    |
| `RedisTemplate<String, Object>` | Spring Data Redis 模板，统一 JSON 序列化 |
| `RedisService`                  | 项目封装的高阶 API                      |

序列化器约定：

- Key / HashKey：`StringRedisSerializer`
- Value / HashValue：`GenericJacksonJsonRedisSerializer`（与 [jackson starter](./jackson.md) 协同）

---

## RedisService API 速查

### Key 操作

```java
redisService.hasKey("user:1");
redisService.

delete("user:1");
redisService.

expire("user:1",60);              // 60 秒
redisService.

expire("user:1",60,TimeUnit.MINUTES);
redisService.

getExpire("user:1");
```

### String（缓存）

```java
// 写
redisService.set("user:1",user);
redisService.

set("user:1",user, 60);           // 60 秒过期
redisService.

set("user:1",user, 1,TimeUnit.HOURS);

// 读
UserVO user = redisService.get("user:1", UserVO.class);
```

### Hash

```java
redisService.hSet("user:1","name","Fan");
redisService.

hSet("user:1","age",18);

String name = redisService.hGet("user:1", "name", String.class);
Map<Object, Object> all = redisService.hEntries("user:1");

redisService.

hIncrement("user:1","age",1);
redisService.

hDel("user:1","name");
```

### List

```java
redisService.lPush("queue",task);
redisService.

rPush("queue",task);

Task task = redisService.lPop("queue", Task.class);
Task tail = redisService.rPop("queue", Task.class);
```

### Set

```java
redisService.sAdd("tags","java","spring","redis");

boolean isMember = redisService.sIsMember("tags", "java");
Set<Object> all = redisService.sMembers("tags");
Long removed = redisService.sRemove("tags", "java");
```

### Sorted Set

```java
redisService.zAdd("ranking",user, 95.5);

Set<Object> top10 = redisService.zRange("ranking", 0, 9);
Long rank = redisService.zRank("ranking", user);
```

---

## 分布式锁（Redisson）

```java
// 阻塞等待, 不会自动释放
redisService.lock("order:lock:1");
try{
        // 临界区
        }finally{
        redisService.

unlock("order:lock:1");
}

// 非阻塞, 不等待
boolean ok = redisService.tryLock("order:lock:1");

// 等待 + 持有超时自动释放
boolean ok = redisService.tryLock("order:lock:1", 3, 30, TimeUnit.SECONDS);
```

> `unlock` 内部已校验 `isHeldByCurrentThread`，误调不会抛异常。  
> `tryLock` 被中断时会恢复中断标志并返回 `false`。

### 直接获取 `RLock`（高级）

```java
RLock lock = redisService.getLock("order:lock:1");
// 自定义业务: lock.tryLockAsync, lock.forceUnlock 等
```

---

## 原子类

```java
RAtomicLong counter = redisService.getAtomicLong("pv:counter");
long n = counter.incrementAndGet();

RAtomicDouble score = redisService.getAtomicDouble("user:1:score");
double old = score.addAndGet(1.5);
```

---

## 布隆过滤器

```java
RBloomFilter<Long> filter = redisService.getBloomFilter("user:id:bf");
filter.

tryInit(1_000_000L,0.01);   // 容量 100w, 误判率 1%
filter.

add(123L);

if(filter.

contains(123L)){
        // 可能存在, 继续查 DB
        }
```

---

## 自定义 RedissonClient

需要集群 / Sentinel 模式时，提供自己的 `RedissonClient` Bean 即可（`@ConditionalOnMissingBean` 兜底）：

```java

@Bean(destroyMethod = "shutdown")
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useClusterServers()
            .addNodeAddress("redis://10.0.0.1:6379", "redis://10.0.0.2:6379")
            .setPassword("cluster-pass");
    return Redisson.create(config);
}
```

---

## 常见问题

**Q：Redisson 自带的 `RedissonClient` 与 starter 注册的会冲突吗？**
A：不会。starter 注册时用了 `@ConditionalOnMissingBean`，提供自己的 Bean 后 starter 不再注册。

**Q：缓存对象反序列化失败？**
A：检查两个一致：

- 写入 / 读取两边共用同一个 `RedisService`（保证 JsonMapper 一致）
- 实体类有公开 setter / getter（或 record / Lombok `@Data`），Jackson 才能序列化

**Q：分布式锁释放失败？**
A：`unlock` 仅释放**当前线程持有的**锁（`isHeldByCurrentThread` 校验）。锁意外过期或被其他线程持有时不会抛异常，仅忽略。生产环境配合
`tryLock(waitTime, leaseTime, unit)` 避免锁过期。

**Q：怎么调试？**
A：调高 `org.redisson` 与 `org.springframework.data.redis` logger 到 DEBUG，会输出所有 Redis 命令。
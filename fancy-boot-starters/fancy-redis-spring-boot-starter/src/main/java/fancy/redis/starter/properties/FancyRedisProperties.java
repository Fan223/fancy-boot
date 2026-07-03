package fancy.redis.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fancy-redis-spring-boot-starter 配置属性。
 *
 * <p>对应 application.yml 中的 {@code fancy.redis} 前缀。
 */
@Data
@ConfigurationProperties(prefix = "fancy.redis")
public class FancyRedisProperties {
}

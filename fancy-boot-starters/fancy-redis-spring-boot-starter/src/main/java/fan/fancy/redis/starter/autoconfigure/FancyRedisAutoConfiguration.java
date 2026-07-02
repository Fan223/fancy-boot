package fan.fancy.redis.starter.autoconfigure;

import fan.fancy.redis.starter.properties.FancyRedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * fancy-redis-spring-boot-starter 自动配置入口。
 */
@AutoConfiguration
@EnableConfigurationProperties(FancyRedisProperties.class)
public class FancyRedisAutoConfiguration {

    public FancyRedisAutoConfiguration(FancyRedisProperties properties) {
    }
}

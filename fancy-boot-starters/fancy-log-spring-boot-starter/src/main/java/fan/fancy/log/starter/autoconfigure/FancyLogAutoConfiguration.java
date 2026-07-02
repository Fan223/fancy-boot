package fan.fancy.log.starter.autoconfigure;

import fan.fancy.log.starter.properties.FancyLogProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * fancy-log-spring-boot-starter 自动配置入口。
 */
@AutoConfiguration
@EnableConfigurationProperties(FancyLogProperties.class)
public class FancyLogAutoConfiguration {

    public FancyLogAutoConfiguration(FancyLogProperties properties) {
    }
}

package fan.fancy.jackson.starter.autoconfigure;

import fan.fancy.jackson.starter.properties.FancyJacksonProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * fancy-jackson-spring-boot-starter 自动配置入口。
 */
@AutoConfiguration
@EnableConfigurationProperties(FancyJacksonProperties.class)
public class FancyJacksonAutoConfiguration {

    public FancyJacksonAutoConfiguration(FancyJacksonProperties properties) {
    }
}

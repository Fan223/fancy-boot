package fan.fancy.resource.server.starter.autoconfigure;

import fan.fancy.resource.server.starter.properties.FancyResourceServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * fancy-resource-server-spring-boot-starter 自动配置入口。
 */
@AutoConfiguration
@EnableConfigurationProperties(FancyResourceServerProperties.class)
public class FancyResourceServerAutoConfiguration {

    public FancyResourceServerAutoConfiguration(FancyResourceServerProperties properties) {
    }
}

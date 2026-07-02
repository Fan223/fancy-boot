package fan.fancy.resource.server.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fancy-resource-server-spring-boot-starter 配置属性。
 *
 * <p>对应 application.yml 中的 {@code fancy.resource-server} 前缀。
 */
@Data
@ConfigurationProperties(prefix = "fancy.resource-server")
public class FancyResourceServerProperties {
}

package fan.fancy.jackson.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fancy-jackson-spring-boot-starter 配置属性。
 *
 * <p>对应 application.yml 中的 {@code fancy.jackson} 前缀。
 */
@Data
@ConfigurationProperties(prefix = "fancy.jackson")
public class FancyJacksonProperties {
}

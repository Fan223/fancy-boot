package fan.fancy.log.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fancy-log-spring-boot-starter 配置属性。
 *
 * <p>对应 application.yml 中的 {@code fancy.log} 前缀。
 */
@Data
@ConfigurationProperties(prefix = "fancy.log")
public class FancyLogProperties {
}

package fancy.starter.server.resource.cors;

import fancy.starter.server.resource.properties.ResourceServerProperties;
import lombok.experimental.UtilityClass;
import org.springframework.web.cors.CorsConfiguration;

/**
 * CORS 配置工具.
 *
 * @author Fan
 */
@UtilityClass
public final class CorsConfigurer {

    /**
     * 通过 {@link ResourceServerProperties.Cors} 属性构建 {@link CorsConfiguration}.
     *
     * @param props {@link ResourceServerProperties.Cors}
     * @return {@link CorsConfiguration}
     */
    public static CorsConfiguration buildConfiguration(ResourceServerProperties.Cors props) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(props.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(props.getAllowedMethods());
        corsConfiguration.setAllowedHeaders(props.getAllowedHeaders());
        corsConfiguration.setAllowCredentials(props.isAllowCredentials());
        corsConfiguration.setMaxAge(props.getMaxAge());
        return corsConfiguration;
    }
}

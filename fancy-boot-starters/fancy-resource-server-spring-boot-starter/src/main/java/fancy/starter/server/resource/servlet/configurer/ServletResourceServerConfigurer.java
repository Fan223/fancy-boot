package fancy.starter.server.resource.servlet.configurer;

import fancy.starter.server.resource.servlet.handler.ServletAccessDeniedHandler;
import fancy.starter.server.resource.servlet.handler.ServletAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 资源服务器配置器.
 *
 * @author Fan
 */
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletResourceServerConfigurer extends AbstractHttpConfigurer<ServletResourceServerConfigurer, HttpSecurity> {

    private final ServletAuthenticationEntryPoint authenticationEntryPoint;

    private final ServletAccessDeniedHandler accessDeniedHandler;

    @Override
    public void init(HttpSecurity http) {
        http.oauth2ResourceServer(configurer -> configurer
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );
    }
}

package fancy.starter.server.resource.servlet.configurer;

import fancy.starter.server.resource.servlet.authentication.ServletInternalAuthenticationFilter;
import fancy.starter.server.resource.servlet.authorize.ServletAuthorizeCustomizer;
import fancy.starter.server.resource.servlet.handler.ServletAccessDeniedHandler;
import fancy.starter.server.resource.servlet.handler.ServletAuthenticationEntryPoint;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 资源服务器配置器.
 *
 * @author Fan
 */
@UtilityClass
public final class ServletResourceServerConfigurer {

    /**
     * 应用资源服务器默认配置.
     *
     * @param http                                 {@link HttpSecurity}
     * @param authenticationEntryPoint             {@link ServletAuthenticationEntryPoint}
     * @param accessDeniedHandler                  {@link ServletAccessDeniedHandler}
     * @param authorizeCustomizers                 {@link ServletAuthorizeCustomizer}
     * @param internalAuthenticationFilterProvider {@link ServletInternalAuthenticationFilter}
     */
    public static void applyDefaults(
            HttpSecurity http,
            ServletAuthenticationEntryPoint authenticationEntryPoint,
            ServletAccessDeniedHandler accessDeniedHandler,
            ObjectProvider<ServletAuthorizeCustomizer> authorizeCustomizers,
            ObjectProvider<ServletInternalAuthenticationFilter> internalAuthenticationFilterProvider
    ) {
        // 禁用 CSRF
        http.csrf(AbstractHttpConfigurer::disable);
        // 跨域配置
        http.cors(Customizer.withDefaults());

        // 资源服务器配置
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        // 应用所有自定义的授权配置器
        http.authorizeHttpRequests(registry -> authorizeCustomizers.orderedStream()
                .forEach(customizer -> customizer.customize(registry)));

        // 当 ServletInternalAuthenticationFilter 存在时, 将其添加到过滤器链中, 放在 UsernamePasswordAuthenticationFilter 之前
        ServletInternalAuthenticationFilter filter = internalAuthenticationFilterProvider.getIfAvailable();
        if (filter != null) {
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }
    }
}

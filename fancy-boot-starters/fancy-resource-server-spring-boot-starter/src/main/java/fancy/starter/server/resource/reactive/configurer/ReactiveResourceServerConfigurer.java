package fancy.starter.server.resource.reactive.configurer;

import fancy.starter.server.resource.reactive.authentication.ReactiveInternalAuthenticationFilter;
import fancy.starter.server.resource.reactive.authorize.ReactiveAuthorizeCustomizer;
import fancy.starter.server.resource.reactive.handler.ReactiveAccessDeniedHandler;
import fancy.starter.server.resource.reactive.handler.ReactiveAuthenticationEntryPoint;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * {@link ConditionalOnWebApplication.Type#REACTIVE} 资源服务器配置器.
 *
 * @author Fan
 */
@UtilityClass
public final class ReactiveResourceServerConfigurer {

    /**
     * 应用资源服务器默认配置.
     *
     * @param http                                 {@link ServerHttpSecurity}
     * @param authenticationEntryPoint             {@link ReactiveAuthenticationEntryPoint}
     * @param accessDeniedHandler                  {@link ReactiveAccessDeniedHandler}
     * @param jwtAuthenticationConverter           JWT 认证转换器
     * @param authorizeCustomizers                 {@link ReactiveAuthorizeCustomizer}
     * @param internalAuthenticationFilterProvider {@link ReactiveInternalAuthenticationFilter}
     */
    public static void applyDefaults(
            ServerHttpSecurity http,
            ReactiveAuthenticationEntryPoint authenticationEntryPoint,
            ReactiveAccessDeniedHandler accessDeniedHandler,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter,
            ObjectProvider<ReactiveAuthorizeCustomizer> authorizeCustomizers,
            ObjectProvider<ReactiveInternalAuthenticationFilter> internalAuthenticationFilterProvider
    ) {
        // 禁用 CSRF
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        // 跨域配置
        http.cors(Customizer.withDefaults());

        // 资源服务器配置
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        // 应用所有自定义的授权配置器
        http.authorizeExchange(spec -> authorizeCustomizers.orderedStream()
                .forEach(customizer -> customizer.customize(spec)));

        // 当 ReactiveInternalAuthenticationFilter 存在时, 将其添加到过滤器链中, 放在 AUTHENTICATION 之前
        ReactiveInternalAuthenticationFilter filter = internalAuthenticationFilterProvider.getIfAvailable();
        if (filter != null) {
            http.addFilterBefore(filter, SecurityWebFiltersOrder.AUTHENTICATION);
        }
    }
}

package fancy.starter.server.resource.autoconfigure.reactive;

import fancy.starter.server.resource.cors.CorsConfigurer;
import fancy.starter.server.resource.properties.ResourceServerProperties;
import fancy.starter.server.resource.reactive.authentication.ReactiveInternalAuthenticationFilter;
import fancy.starter.server.resource.reactive.authorize.ReactiveAuthorizeCustomizer;
import fancy.starter.server.resource.reactive.configurer.ReactiveResourceServerConfigurer;
import fancy.starter.server.resource.reactive.handler.ReactiveAccessDeniedHandler;
import fancy.starter.server.resource.reactive.handler.ReactiveAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link ConditionalOnWebApplication.Type#REACTIVE} 资源服务器自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(ResourceServerProperties.class)
@AllArgsConstructor
public class ReactiveResourceServerAutoConfiguration {

    private ResourceServerProperties resourceServerProperties;

    private ObjectProvider<ReactiveAuthorizeCustomizer> authorizeCustomizers;

    @Bean
    @ConditionalOnMissingBean
    SecurityWebFilterChain defaultResourceServerSecurityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveAuthenticationEntryPoint authenticationEntryPoint,
            ReactiveAccessDeniedHandler accessDeniedHandler,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveJwtAuthenticationConverter,
            ObjectProvider<ReactiveInternalAuthenticationFilter> internalAuthenticationFilterProvider
    ) {
        // 应用资源服务器默认配置
        ReactiveResourceServerConfigurer.applyDefaults(http, authenticationEntryPoint, accessDeniedHandler, reactiveJwtAuthenticationConverter, authorizeCustomizers, internalAuthenticationFilterProvider);
        // 其他请求都需要认证
        http.authorizeExchange(spec -> spec.anyExchange().authenticated());
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuthenticationEntryPoint reactiveAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return new ReactiveAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAccessDeniedHandler reactiveAccessDeniedHandler(JsonMapper jsonMapper) {
        return new ReactiveAccessDeniedHandler(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fancy.security.internal-token", name = "token")
    public ReactiveInternalAuthenticationFilter reactiveInternalAuthenticationFilter() {
        return new ReactiveInternalAuthenticationFilter(resourceServerProperties.getInternalToken().getToken());
    }

    @Bean
    public ReactiveAuthorizeCustomizer defaultReactiveAuthorizeCustomizer() {
        // 默认授权配置器, 放行 "/api" 路径下的所有请求
        return spec -> spec.pathMatchers("/api/**").permitAll();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties")
    @ConditionalOnMissingBean(name = "actuatorReactiveAuthorizeCustomizer")
    public ReactiveAuthorizeCustomizer actuatorReactiveAuthorizeCustomizer() {
        // 当 actuator 在 classpath 时, 放行所有 actuator 端点
        return spec -> spec.pathMatchers("/actuator/**").permitAll();
    }

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveJwtAuthenticationConverter() {
        ResourceServerProperties.Jwt jwt = resourceServerProperties.getJwt();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置解析权限信息的前缀, 为空则是去掉前缀
        grantedAuthoritiesConverter.setAuthorityPrefix(jwt.getAuthorityPrefix());
        // 设置权限信息在 Access Token Claims 中的 Key, 从而解析获取权限信息
        grantedAuthoritiesConverter.setAuthoritiesClaimName(jwt.getClaimName());

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = CorsConfigurer.buildConfiguration(resourceServerProperties.getCors());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}

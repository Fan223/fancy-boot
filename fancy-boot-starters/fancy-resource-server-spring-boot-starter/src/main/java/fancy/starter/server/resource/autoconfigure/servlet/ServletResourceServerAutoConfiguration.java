package fancy.starter.server.resource.autoconfigure.servlet;

import fancy.starter.server.resource.cors.CorsConfigurer;
import fancy.starter.server.resource.properties.ResourceServerProperties;
import fancy.starter.server.resource.servlet.authentication.ServletInternalAuthenticationFilter;
import fancy.starter.server.resource.servlet.authorize.ServletAuthorizeCustomizer;
import fancy.starter.server.resource.servlet.configurer.ServletResourceServerConfigurer;
import fancy.starter.server.resource.servlet.handler.ServletAccessDeniedHandler;
import fancy.starter.server.resource.servlet.handler.ServletAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 资源服务器自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(ResourceServerProperties.class)
@AllArgsConstructor
public class ServletResourceServerAutoConfiguration {

    private ResourceServerProperties resourceServerProperties;

    private ObjectProvider<ServletAuthorizeCustomizer> authorizeCustomizers;

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain defaultResourceServerSecurityFilterChain(
            HttpSecurity http,
            ServletAuthenticationEntryPoint authenticationEntryPoint,
            ServletAccessDeniedHandler accessDeniedHandler,
            ObjectProvider<ServletInternalAuthenticationFilter> internalAuthenticationFilterProvider
    ) {
        // 应用资源服务器默认配置
        ServletResourceServerConfigurer.applyDefaults(http, authenticationEntryPoint, accessDeniedHandler, authorizeCustomizers, internalAuthenticationFilterProvider);
        // 其他请求都需要认证
        http.authorizeHttpRequests(registry -> registry.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServletAuthenticationEntryPoint servletAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return new ServletAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServletAccessDeniedHandler servletAccessDeniedHandler(JsonMapper jsonMapper) {
        return new ServletAccessDeniedHandler(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "fancy.security.internal-token", name = "token")
    public ServletInternalAuthenticationFilter internalAuthenticationFilter() {
        return new ServletInternalAuthenticationFilter(resourceServerProperties.getInternalToken().getToken());
    }

    @Bean
    public ServletAuthorizeCustomizer defaultAuthorizeCustomizer() {
        // 默认授权配置器, 放行 "/api" 路径下的所有请求
        return registry -> registry.requestMatchers("/api/**").permitAll();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties")
    @ConditionalOnMissingBean(name = "actuatorAuthorizeCustomizer")
    public ServletAuthorizeCustomizer actuatorAuthorizeCustomizer() {
        // 当 actuator 在 classpath 时, 放行所有 actuator 端点
        return registry -> registry.requestMatchers("/actuator/**").permitAll();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        ResourceServerProperties.Jwt jwt = resourceServerProperties.getJwt();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 设置解析权限信息的前缀, 为空则是去掉前缀
        grantedAuthoritiesConverter.setAuthorityPrefix(jwt.getAuthorityPrefix());
        // 设置权限信息在 Access Token Claims 中的 Key, 从而解析获取权限信息
        grantedAuthoritiesConverter.setAuthoritiesClaimName(jwt.getClaimName());

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = CorsConfigurer.buildConfiguration(resourceServerProperties.getCors());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}

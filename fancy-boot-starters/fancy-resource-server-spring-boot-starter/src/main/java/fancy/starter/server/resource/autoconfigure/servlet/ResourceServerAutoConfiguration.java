package fancy.starter.server.resource.autoconfigure.servlet;

import fancy.starter.server.resource.properties.SecurityProperties;
import fancy.starter.server.resource.servlet.authentication.InternalAuthenticationFilter;
import fancy.starter.server.resource.servlet.authorize.AuthorizeCustomizer;
import fancy.starter.server.resource.servlet.configurer.ServletResourceServerConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 资源服务器自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@AllArgsConstructor
public class ResourceServerAutoConfiguration {

    private SecurityProperties securityProperties;

    private ObjectProvider<AuthorizeCustomizer> authorizeCustomizers;

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain defaultResourceServerSecurityFilterChain(
            HttpSecurity http,
            ServletResourceServerConfigurer resourceServerConfigurer,
            ObjectProvider<InternalAuthenticationFilter> internalAuthenticationFilterProvider
    ) {
        // 应用资源服务器配置
        http.apply(resourceServerConfigurer);
        // 禁用 CSRF
        http.csrf(AbstractHttpConfigurer::disable);
        // 跨域配置
        http.cors(Customizer.withDefaults());
        http.authorizeHttpRequests(registry -> {
            // 应用所有自定义的授权配置器
            authorizeCustomizers.orderedStream()
                    .forEach(customizer -> customizer.customize(registry));
            // 其他请求都需要认证
            registry.anyRequest().authenticated();
        });

        // 当 InternalAuthenticationFilter 存在时, 将其添加到过滤器链中, 放在 UsernamePasswordAuthenticationFilter 之前
        InternalAuthenticationFilter internalTokenAuthFilter = internalAuthenticationFilterProvider.getIfAvailable();
        if (internalTokenAuthFilter != null) {
            http.addFilterBefore(internalTokenAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

    @Bean
    public AuthorizeCustomizer defaultAuthorizeCustomizer() {
        // 默认授权配置器, 放行 "/api" 路径下的所有请求
        return registry -> registry.requestMatchers("/api/**").permitAll();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties")
    @ConditionalOnMissingBean(name = "actuatorAuthorizeCustomizer")
    public AuthorizeCustomizer actuatorAuthorizeCustomizer() {
        // 当 actuator 在 classpath 时, 放行所有 actuator 端点
        return registry -> registry.requestMatchers("/actuator/**").permitAll();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        SecurityProperties.Jwt jwt = securityProperties.getJwt();
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
    @ConditionalOnProperty(prefix = "fancy.security.internal-token", name = "token")
    public InternalAuthenticationFilter internalAuthenticationFilter() {
        return new InternalAuthenticationFilter(securityProperties.getInternalToken().getToken());
    }

    @Bean
    @ConditionalOnMissingBean
    public CorsConfigurationSource corsConfigurationSource() {
        SecurityProperties.Cors cors = securityProperties.getCors();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(cors.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(cors.getAllowedMethods());
        corsConfiguration.setAllowedHeaders(cors.getAllowedHeaders());
        corsConfiguration.setAllowCredentials(cors.isAllowCredentials());
        corsConfiguration.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}

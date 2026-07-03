package fancy.log.starter.autoconfigure;

import fancy.boot.core.lang.StringUtils;
import fancy.log.starter.aspect.ControllerLogAspect;
import fancy.log.starter.aspect.LogAspect;
import fancy.log.starter.filter.TraceIdFilter;
import fancy.log.starter.properties.LogProperties;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 日志自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
@RequiredArgsConstructor
public class LogAutoConfiguration {

    private final LogProperties properties;

    @Value("${spring.application.name:}")
    private String applicationName;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(properties.getServiceName()) && StringUtils.isNotBlank(applicationName)) {
            properties.setServiceName(applicationName);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ControllerLogAspect controllerLogAspect() {
        return new ControllerLogAspect(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogAspect logAspect() {
        return new LogAspect(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterFilterRegistrationBean() {
        // 注册 TraceIdFilter
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TraceIdFilter());
        // 指定拦截路径
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}

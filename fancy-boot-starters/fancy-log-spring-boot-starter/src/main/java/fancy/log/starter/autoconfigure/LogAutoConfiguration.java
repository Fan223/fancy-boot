package fancy.log.starter.autoconfigure;

import fancy.log.starter.aspect.ControllerLogAspect;
import fancy.log.starter.aspect.LogAdvice;
import fancy.log.starter.aspect.LogAspect;
import fancy.log.starter.filter.TraceIdFilter;
import fancy.log.starter.printer.LogPrinter;
import fancy.log.starter.properties.LogProperties;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * 日志自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class LogAutoConfiguration {

    private final LogProperties properties;

    private final Environment environment;

    @Bean
    @ConditionalOnMissingBean
    public LogPrinter logPrinter() {
        return new LogPrinter(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogAdvice logAdvice(LogPrinter printer) {
        return new LogAdvice(properties, printer);
    }


    @Bean
    @ConditionalOnMissingBean
    public LogAspect logAspect(LogAdvice logAdvice) {
        return new LogAspect(properties, logAdvice);
    }

    @Bean
    @ConditionalOnMissingBean
    public ControllerLogAspect controllerLogAspect(LogAdvice logAdvice) {
        return new ControllerLogAspect(properties, logAdvice);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistrationBean() {
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TraceIdFilter());
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}

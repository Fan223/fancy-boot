package fancy.mybatis.plus.starter.bootstrap;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * P6Spy 环境后置处理器.
 *
 * @author Fan
 */
public class P6SpyEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String LOG_FORMAT = "分类: %(category) | 耗时: %(executionTime)ms | \n\t%(sql)";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        properties.putIfAbsent("decorator.datasource.p6spy.log-format", LOG_FORMAT);

        PropertySource<?> propertySource = new MapPropertySource("p6Spy", properties);
        environment.getPropertySources().addLast(propertySource);
    }
}

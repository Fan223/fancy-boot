package fancy.starter.datasource.annotation;

import java.lang.annotation.*;

/**
 * 动态数据源注解.
 *
 * @author Fan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ds {

    String value();
}

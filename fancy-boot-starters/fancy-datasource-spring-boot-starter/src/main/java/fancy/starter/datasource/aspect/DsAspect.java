package fancy.starter.datasource.aspect;

import fancy.boot.core.annotation.AnnotationResolver;
import fancy.starter.datasource.annotation.Ds;
import fancy.starter.datasource.context.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 动态数据源切面.
 *
 * @author Fan
 */
@Aspect
public class DsAspect {

    @Around("@within(fancy.starter.datasource.annotation.Ds) || @annotation(fancy.starter.datasource.annotation.Ds)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 解析数据源注解, 没有注解则继续执行原方法
        Ds ds = AnnotationResolver.resolve(joinPoint, Ds.class);
        if (ds == null) {
            return joinPoint.proceed();
        }

        // 有注解则将数据源标识压入上下文, 执行原方法, 最后弹出数据源标识
        try {
            DataSourceContextHolder.push(ds.value());
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.poll();
        }
    }
}

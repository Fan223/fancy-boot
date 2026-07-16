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
        // 解析获取动态数据源注解
        Ds ds = AnnotationResolver.resolve(joinPoint, Ds.class);
        try {
            DataSourceContextHolder.push(ds.value());
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.poll();
        }
    }
}

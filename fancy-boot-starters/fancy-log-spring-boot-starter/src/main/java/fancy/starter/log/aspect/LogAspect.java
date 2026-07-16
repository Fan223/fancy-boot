package fancy.starter.log.aspect;

import fancy.boot.core.annotation.AnnotationResolver;
import fancy.starter.log.annotation.Log;
import fancy.starter.log.properties.LogProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * {@link Log} 切面.
 *
 * @author Fan
 */
@Aspect
@RequiredArgsConstructor
public class LogAspect {

    private final LogProperties properties;

    private final LogAdvice logAdvice;

    @Around("@within(fancy.starter.log.annotation.Log) || @annotation(fancy.starter.log.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log log = AnnotationResolver.resolve(joinPoint, Log.class);
        if (!properties.isEnabled() || !properties.getAnnotation().isEnabled()) {
            return joinPoint.proceed();
        }
        return logAdvice.execute(joinPoint, log);
    }
}

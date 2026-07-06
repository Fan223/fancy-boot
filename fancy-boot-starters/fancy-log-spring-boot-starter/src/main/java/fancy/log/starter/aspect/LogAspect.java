package fancy.log.starter.aspect;

import fancy.boot.core.annotation.AnnotationResolver;
import fancy.log.starter.annotation.Log;
import fancy.log.starter.properties.LogProperties;
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

    @Around("@within(fancy.log.starter.annotation.Log) || @annotation(fancy.log.starter.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log log = AnnotationResolver.resolve(joinPoint, Log.class);
        if (log == null || !properties.isEnabled() || !properties.getAnnotation().isEnabled()) {
            return joinPoint.proceed();
        }
        return logAdvice.execute(joinPoint, log);
    }
}

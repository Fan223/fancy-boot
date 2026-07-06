package fancy.log.starter.aspect;

import fancy.boot.core.annotation.AnnotationResolver;
import fancy.log.starter.annotation.Log;
import fancy.log.starter.properties.LogProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Controller 日志切面.
 *
 * @author Fan
 */
@Aspect
@RequiredArgsConstructor
public class ControllerLogAspect {

    private final LogProperties properties;

    private final LogAdvice logAdvice;

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log log = AnnotationResolver.resolve(joinPoint, Log.class);
        // 如果方法或类上存在 @Log 注解则跳过.
        if (log != null || !properties.isEnabled() || !properties.getController().isEnabled()) {
            return joinPoint.proceed();
        }
        return logAdvice.execute(joinPoint, null);
    }
}

package fancy.log.starter.aspect;

import fancy.boot.core.annotation.AnnotationResolver;
import fancy.log.starter.annotation.Log;
import fancy.log.starter.event.LogEvent;
import fancy.log.starter.printer.LogPrinter;
import fancy.log.starter.properties.LogProperties;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * Controller 日志切面.
 *
 * @author Fan
 */
@Aspect
@AllArgsConstructor
public class ControllerLogAspect {

    private final LogProperties properties;

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log log = AnnotationResolver.resolve(joinPoint, Log.class);
        // 如果方法或类上存在 @Log 注解则跳过
        if (log != null || !properties.isEnabled() || !properties.getController().isEnabled()) {
            return joinPoint.proceed();
        }

        long start = System.nanoTime();
        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            publishEvent(joinPoint, result, exception, costMs);
        }
    }

    /**
     * 发布日志事件.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param result    返回结果
     * @param exception {@link Throwable}
     * @param costMs    耗时(毫秒)
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, Object result, Throwable exception, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogEvent event = LogEvent.builder()
                .serviceName(properties.getServiceName())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .args(properties.isPrintArgs() ? joinPoint.getArgs() : null)
                .maxArgsLength(properties.getMaxArgsLength())
                .result(properties.isPrintResult() ? result : null)
                .maxResultLength(properties.getMaxResultLength())
                .costMs(costMs)
                .exception(exception)
                .build();
        LogPrinter.print(event);
    }
}

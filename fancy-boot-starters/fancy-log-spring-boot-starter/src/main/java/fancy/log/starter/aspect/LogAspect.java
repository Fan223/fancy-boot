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
 * {@link Log} 切面.
 *
 * @author Fan
 */
@Aspect
@AllArgsConstructor
public class LogAspect {

    private final LogProperties properties;

    @Around("@within(fancy.log.starter.annotation.Log) || @annotation(fancy.log.starter.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Log log = AnnotationResolver.resolve(joinPoint, Log.class);
        if (log == null || !properties.isEnabled() || !properties.getAnnotation().isEnabled()) {
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
            publishEvent(joinPoint, log, result, exception, costMs);
        }
    }

    /**
     * 发布日志事件.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param log       {@link Log}
     * @param result    返回结果
     * @param exception {@link Throwable}
     * @param costMs    耗时(毫秒)
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, Log log, Object result, Throwable exception, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogEvent event = LogEvent.builder()
                .serviceName(properties.getServiceName())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .tag(log.value())
                .args(log.printArgs() ? joinPoint.getArgs() : null)
                .maxArgsLength(log.maxArgsLength())
                .result(log.printResult() ? result : null)
                .maxResultLength(log.maxResultLength())
                .costMs(costMs)
                .exception(exception)
                .build();
        LogPrinter.print(event);
    }
}

package fancy.log.starter.aspect;

import fancy.log.starter.annotation.Log;
import fancy.log.starter.event.LogEvent;
import fancy.log.starter.printer.LogPrinter;
import fancy.log.starter.properties.LogProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * 日志切面公共处理.
 *
 * @author Fan
 */
@RequiredArgsConstructor
public class LogAdvice {

    private final LogProperties properties;

    private final LogPrinter printer;

    /**
     * 执行切面逻辑.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param log       {@link Log}
     * @return {@link Object}
     */
    public Object execute(ProceedingJoinPoint joinPoint, Log log) throws Throwable {
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
     * 发布日志事件, {@link Log} 为 null 时使用 {@link LogProperties}.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @param log       {@link Log}
     * @param result    返回结果
     * @param exception {@link Throwable}
     * @param costMs    耗时
     */
    private void publishEvent(ProceedingJoinPoint joinPoint, Log log, Object result, Throwable exception, long costMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        boolean printArgs = log != null ? log.printArgs() : properties.isPrintArgs();
        int maxArgsLength = log != null ? log.maxArgsLength() : properties.getMaxArgsLength();
        boolean printResult = log != null ? log.printResult() : properties.isPrintResult();
        int maxResultLength = log != null ? log.maxResultLength() : properties.getMaxResultLength();

        LogEvent event = LogEvent.builder()
                .serviceName(properties.getServiceName())
                .className(signature.getDeclaringTypeName())
                .methodName(signature.getName())
                .tag(log != null ? log.value() : null)
                .args(printArgs ? joinPoint.getArgs() : null)
                .maxArgsLength(maxArgsLength)
                .result(printResult ? result : null)
                .maxResultLength(maxResultLength)
                .costMs(costMs)
                .exception(exception)
                .build();
        printer.print(event);
    }
}

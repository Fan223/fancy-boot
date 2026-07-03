package fancy.log.starter.aspect;

import fancy.log.starter.annotation.Log;
import fancy.log.starter.event.LogEvent;
import fancy.log.starter.printer.LogPrinter;
import fancy.log.starter.properties.LogProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 日志切面公共处理. 由 {@link LogAspect} 和 {@link ControllerLogAspect} 委托调用,
 * 消除 try-catch-finally + publishEvent 的重复实现.
 *
 * @author Fan
 */
@Component
@RequiredArgsConstructor
public class LogAdvice {

    private final LogProperties properties;

    /**
     * 执行切面逻辑. {@code log} 为 null 时使用全局 properties.
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
        LogPrinter.print(event);
    }
}
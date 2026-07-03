package fancy.log.starter.printer;

import fancy.boot.core.lang.StringUtils;
import fancy.log.starter.event.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * 日志打印器.
 *
 * @author Fan
 */
@Slf4j
//@UtilityClass
@RequiredArgsConstructor
public class LogPrinter {

    /**
     * Spring 环境, 由 {@code LogAutoConfiguration} 注入, 用于在 {@code properties.serviceName} 为空时
     * 回填 {@code spring.application.name}.
     */
    private final Environment environment;

    /**
     * 由 {@code LogAutoConfiguration} 在启动期注入.
     */
//    public static void setEnvironment(Environment environment) {
//        LogPrinter.environment = environment;
//    }

    /**
     * 打印日志.
     *
     * @param event {@link LogEvent}
     */
    public static void print(LogEvent event) {
        StringBuilder msg = new StringBuilder(128);

        String tag = event.tag();
        if (StringUtils.isNotBlank(tag)) {
            msg.append("[").append(tag).append("] ");
        }

        String serviceName = resolveServiceName(event.serviceName());
        if (StringUtils.isNotBlank(serviceName)) {
            msg.append(serviceName).append(" | ");
        }

        msg.append(event.className())
                .append('#')
                .append(event.methodName())
                .append(" | 耗时=")
                .append(event.costMs())
                .append("ms");

        String args = sanitize(event.args(), event.maxArgsLength());
        if (args != null) {
            msg.append(" | 入参=").append(args);
        }
        String result = sanitize(event.result(), event.maxResultLength());
        if (result != null) {
            msg.append(" | 返回结果=").append(result);
        }

        Throwable exception = event.exception();
        if (exception == null) {
            log.info("{}", msg);
        } else {
            // 会自动打印完整的异常堆栈信息
            log.error("{}", msg, exception);
        }
    }

    private static String resolveServiceName(String configured) {
        if (StringUtils.isNotBlank(configured)) {
            return configured;
        }
        if (environment != null) {
            return environment.getProperty("spring.application.name");
        }
        return null;
    }

    /**
     * 长度处理.
     *
     * @param value     {@link Object}
     * @param maxLength 最大长度
     * @return {@link String}
     */
    public static String sanitize(Object value, int maxLength) {
        if (value == null) {
            return null;
        }

        String text;
        if (value.getClass().isArray()) {
            text = Arrays.deepToString((Object[]) value);
        } else {
            text = String.valueOf(value);
        }

        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
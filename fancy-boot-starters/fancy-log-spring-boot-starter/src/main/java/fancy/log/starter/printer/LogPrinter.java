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
@RequiredArgsConstructor
public class LogPrinter {

    private final Environment environment;

    /**
     * 打印日志.
     *
     * @param event {@link LogEvent}
     */
    public void print(LogEvent event) {
        StringBuilder msg = new StringBuilder(128);

        String tag = event.tag();
        if (StringUtils.isNotBlank(tag)) {
            msg.append("[").append(tag).append("] ");
        }

        String serviceName = resolveServiceName(event.serviceName());
        if (StringUtils.isNotBlank(serviceName)) {
            msg.append("服务: ").append(serviceName).append(" | ");
        }

        msg.append("接口: ").append(event.className())
                .append('#')
                .append(event.methodName())
                .append(" | 耗时: ")
                .append(event.costMs())
                .append("ms");

        String args = sanitize(event.args(), event.maxArgsLength());
        if (args != null) {
            msg.append(" | 入参: ").append(args);
        }
        String result = sanitize(event.result(), event.maxResultLength());
        if (result != null) {
            msg.append(" | \n\t返回结果: ").append(result);
        }

        Throwable exception = event.exception();
        if (exception == null) {
            log.info("{}", msg);
        } else {
            // 会自动打印完整的异常堆栈信息
            log.error("{}", msg, exception);
        }
    }

    private String resolveServiceName(String serviceName) {
        if (StringUtils.isNotBlank(serviceName)) {
            return serviceName;
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

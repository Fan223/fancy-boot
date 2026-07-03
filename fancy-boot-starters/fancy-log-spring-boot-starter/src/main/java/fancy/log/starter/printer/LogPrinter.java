package fancy.log.starter.printer;

import fancy.log.starter.event.LogEvent;
import io.micrometer.common.util.StringUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 日志打印器.
 *
 * @author Fan
 */
@UtilityClass
@Slf4j
public class LogPrinter {

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

        String serviceName = event.serviceName();
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

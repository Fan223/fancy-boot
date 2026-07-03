package fancy.log.starter.properties;

import fancy.log.starter.annotation.Log;
import fancy.log.starter.aspect.ControllerLogAspect;
import fancy.log.starter.aspect.LogAspect;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置, 用于 {@link LogAspect} 和 {@link ControllerLogAspect}.
 *
 * @author Fan
 */
@Data
@ConfigurationProperties(prefix = "fancy.log")
public class LogProperties {

    /**
     * 全局总开关.
     */
    private boolean enabled = true;

    /**
     * {@link LogAspect} 配置.
     */
    private Annotation annotation = new Annotation();

    /**
     * {@link ControllerLogAspect} 配置.
     */
    private Controller controller = new Controller();

    /**
     * 服务名.
     */
    private String serviceName;

    /**
     * 是否打印参数.
     */
    private boolean printArgs = true;

    /**
     * 最大参数长度.
     */
    private int maxArgsLength = 2048;

    /**
     * 是否打印返回结果.
     */
    private boolean printResult = true;

    /**
     * 最大返回结果长度.
     */
    private int maxResultLength = 2048;

    @Data
    public static class Annotation {
        /**
         * 是否启用 {@link Log} 切面.
         */
        private boolean enabled = true;
    }

    @Data
    public static class Controller {
        /**
         * 是否启用 Controller 切面.
         */
        private boolean enabled = true;
    }
}

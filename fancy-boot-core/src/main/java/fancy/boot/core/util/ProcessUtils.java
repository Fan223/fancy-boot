package fancy.boot.core.util;

import lombok.experimental.UtilityClass;

/**
 * {@link Process} 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class ProcessUtils {

    /**
     * 获取当前进程 ID.
     *
     * @return {@code long}
     */
    public static long getCurrentPid() {
        return ProcessHandle.current().pid();
    }
}

package fan.fancy.core.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * ID 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class IdUtils {

    /**
     * 雪花 ID 实例.
     */
    private static final Snowflake SNOWFLAKE = Snowflake.of();

    /**
     * 生成雪花 ID.
     *
     * @return {@code long}
     */
    public static long generateSnowflakeId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成雪花 ID 字符串.
     *
     * @return {@link String}
     */
    public static String generateSnowflakeIdStr() {
        return String.valueOf(generateSnowflakeId());
    }

    /**
     * 生成 {@link UUID} 字符串.
     *
     * @return {@link String}
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成数据中心 ID.
     *
     * @param maxDataCenterId 最大数据中心 ID
     * @return {@code long}
     */
    public static long generateDataCenterId(long maxDataCenterId) {
        long id = 1L;
        byte[] mac = NetUtils.getLocalHardwareAddress();
        if (null != mac) {
            id = ((0xFF & (long) mac[mac.length - 2]) | (0xFF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (maxDataCenterId + 1);
        }
        return id;
    }

    /**
     * 生成工作节点 ID.
     *
     * @param dataCenterId 数据中心 ID
     * @param maxWorkerId  最大工作节点 ID
     * @return {@code long}
     */
    public static long generateWorkerId(long dataCenterId, long maxWorkerId) {
        // DataCenterId + PID 的 HashCode 获取 16 个低位
        String workerId = String.valueOf(dataCenterId) + ProcessUtils.getCurrentPid();
        return (workerId.hashCode() & 0xFFFF) % (maxWorkerId + 1);
    }
}

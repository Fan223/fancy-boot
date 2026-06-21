package fan.fancy.core.lang;

import lombok.experimental.UtilityClass;

/**
 * 字节工具类.
 *
 * @author Fan
 */
@UtilityClass
public class ByteUtils {

    /**
     * 将字节数组转换为十六进制字符串.
     *
     * @param bytes {@code byte[]}
     * @return {@link String}
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            // 格式化每个字节为两位十六进制字符串
            builder.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) {
                builder.append(":");
            }
        }
        return builder.toString();
    }
}

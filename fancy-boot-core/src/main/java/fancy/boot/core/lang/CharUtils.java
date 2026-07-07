package fancy.boot.core.lang;

import lombok.experimental.UtilityClass;

/**
 * 字符工具类.
 *
 * @author Fan
 */
@UtilityClass
public class CharUtils {

    /**
     * 将字符转换为小写.
     *
     * @param ch {@code char}
     * @return {@code char}
     */
    public static char toLowerCase(char ch) {
        return Character.toLowerCase(ch);
    }
}

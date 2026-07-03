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
     * 判断字符是否为空白.
     *
     * @param ch {@code char}
     * @return {@code boolean}
     */
    public static boolean isBlank(char ch) {
        return Character.isWhitespace(ch);
    }

    /**
     * 判断字符是否不为空白.
     *
     * @param ch {@code char}
     * @return {@code boolean}
     */
    public static boolean isNotBlank(char ch) {
        return !isBlank(ch);
    }

    /**
     * 判断字符是否为大写.
     *
     * @param ch {@code char}
     * @return {@code boolean}
     */
    public static boolean isUpperCase(char ch) {
        return Character.isUpperCase(ch);
    }

    /**
     * 将字符转换为大写.
     *
     * @param ch {@code char}
     * @return {@code char}
     */
    public static char toUpperCase(char ch) {
        return Character.toUpperCase(ch);
    }

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

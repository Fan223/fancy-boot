package fancy.boot.core.lang;

import lombok.experimental.UtilityClass;

/**
 * {@link String} 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class StringUtils {

    /**
     * 判断 {@link CharSequence} 是否为空白.
     *
     * @param cs {@link CharSequence}
     * @return {@code boolean}
     */
    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        if (cs instanceof String s) {
            return s.isBlank();
        }
        return cs.toString().isBlank();
    }

    /**
     * 判断 {@link CharSequence} 是否不为空白.
     *
     * @param cs {@link CharSequence}
     * @return {@code boolean}
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 将 {@link CharSequence} 转换为 {@link String}.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toString(CharSequence cs) {
        return cs == null ? null : cs.toString();
    }

    /**
     * 首字母小写.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String lowerFirst(CharSequence cs) {
        if (isBlank(cs)) {
            return toString(cs);
        }

        char first = cs.charAt(0);
        char changed = Character.toLowerCase(first);
        if (first == changed) {
            return cs.toString();
        }

        int len = cs.length();
        if (len == 1) {
            return String.valueOf(changed);
        }
        return changed + cs.subSequence(1, len).toString();
    }
}

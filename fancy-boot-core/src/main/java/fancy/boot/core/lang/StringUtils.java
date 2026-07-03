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
     * 判断 {@link CharSequence} 是否为空.
     *
     * @param cs {@link CharSequence}
     * @return {@code boolean}
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * 判断 {@link CharSequence} 是否不为空.
     *
     * @param cs {@link CharSequence}
     * @return {@code boolean}
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

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
     * 转大写.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toUpperCase(CharSequence cs) {
        return isBlank(cs) ? toString(cs) : cs.toString().toUpperCase();
    }

    /**
     * 转小写.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toLowerCase(CharSequence cs) {
        return isBlank(cs) ? toString(cs) : cs.toString().toLowerCase();
    }

    /**
     * 首字母小写.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String lowerFirst(CharSequence cs) {
        return changeFirst(cs, true);
    }

    private static String changeFirst(CharSequence cs, boolean lower) {
        if (isBlank(cs)) {
            return toString(cs);
        }

        char first = cs.charAt(0);
        char changed = lower ? CharUtils.toLowerCase(first) : CharUtils.toUpperCase(first);
        if (first == changed) {
            return cs.toString();
        }

        int len = cs.length();
        if (len == 1) {
            return String.valueOf(changed);
        }
        return changed + cs.subSequence(1, len).toString();
    }

    /**
     * 首字母大写.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String upperFirst(CharSequence cs) {
        return changeFirst(cs, false);
    }

    /**
     * 转驼峰, hello_world -> helloWorld.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toCamelCase(CharSequence cs) {
        if (isBlank(cs)) {
            return toString(cs);
        }

        StringBuilder sb = new StringBuilder(cs.length());
        boolean upperNext = false;
        for (int i = 0; i < cs.length(); i++) {
            char c = cs.charAt(i);

            if (c == '_' || c == '-') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(CharUtils.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(CharUtils.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 转下划线, helloWorld -> hello_world.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toSnakeCase(CharSequence cs) {
        if (isBlank(cs)) {
            return toString(cs);
        }

        StringBuilder sb = new StringBuilder(cs.length() + 5);
        for (int i = 0; i < cs.length(); i++) {
            char c = cs.charAt(i);

            if (CharUtils.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(CharUtils.toLowerCase(c));
            } else if (c == '-') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 转串式, helloWorld -> hello-world.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toKebabCase(CharSequence cs) {
        return isEmpty(cs) ? toString(cs) : toSnakeCase(cs).replace('_', '-');
    }

    /**
     * 转全大写下划线, helloWorld -> HELLO_WORLD.
     *
     * @param cs {@link CharSequence}
     * @return {@link String}
     */
    public static String toUpperSnakeCase(CharSequence cs) {
        return isEmpty(cs) ? toString(cs) : toSnakeCase(cs).toUpperCase();
    }

    /**
     * 移除前缀.
     *
     * @param cs     {@link CharSequence}
     * @param prefix 前缀
     * @return {@link String}
     */
    public static String removePrefix(CharSequence cs, CharSequence prefix) {
        if (isEmpty(cs) || isEmpty(prefix)) {
            return toString(cs);
        }

        int cLen = cs.length();
        int pLen = prefix.length();
        if (cLen < pLen) {
            return cs.toString();
        }

        if (cs instanceof String s && prefix instanceof String p) {
            return s.startsWith(p) ? s.substring(pLen) : s;
        }

        for (int i = 0; i < pLen; i++) {
            if (cs.charAt(i) != prefix.charAt(i)) {
                return cs.toString();
            }
        }
        return cs.subSequence(pLen, cLen).toString();
    }

    /**
     * 移除前缀并小写首字母.
     *
     * @param cs     {@link CharSequence}
     * @param prefix 前缀
     * @return {@link String}
     */
    public static String removePrefixAndLowerFirst(CharSequence cs, CharSequence prefix) {
        return lowerFirst(removePrefix(cs, prefix));
    }
}

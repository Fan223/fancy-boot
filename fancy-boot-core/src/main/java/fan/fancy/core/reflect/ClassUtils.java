package fan.fancy.core.reflect;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Class} 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class ClassUtils {

    /**
     * 包装类类型与其对应的基本类型映射.
     */
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();

    /**
     * 基本类型与其对应的包装类类型映射.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();

    /**
     * 基本类型的宽化转换规则映射.
     */
    private static final Map<Class<?>, Class<?>[]> PRIMITIVE_WIDEN = new HashMap<>();

    static {
        // 基本类型与其对应的包装类类型映射
        WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
        WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
        WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
        WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
        WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
        WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
        WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
        WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);

        // 反向映射
        WRAPPER_TO_PRIMITIVE.forEach((wrapper, primitive) -> PRIMITIVE_TO_WRAPPER.put(primitive, wrapper));

        // 基本类型的宽化转换规则
        PRIMITIVE_WIDEN.put(byte.class, new Class<?>[]{short.class, int.class, long.class, float.class, double.class});
        PRIMITIVE_WIDEN.put(short.class, new Class<?>[]{int.class, long.class, float.class, double.class});
        PRIMITIVE_WIDEN.put(char.class, new Class<?>[]{int.class, long.class, float.class, double.class});
        PRIMITIVE_WIDEN.put(int.class, new Class<?>[]{long.class, float.class, double.class});
        PRIMITIVE_WIDEN.put(long.class, new Class<?>[]{float.class, double.class});
        PRIMITIVE_WIDEN.put(float.class, new Class<?>[]{double.class});
    }

    /**
     * 判断是否为基本类型的包装类.
     *
     * @param clazz {@link Class}
     * @return {@code boolean}
     */
    public static boolean isWrapper(Class<?> clazz) {
        return WRAPPER_TO_PRIMITIVE.containsKey(clazz);
    }

    /**
     * 将包装类转换为对应的基本类型.
     *
     * @param clazz {@link Class}
     * @return {@link Class}
     */
    public static Class<?> toPrimitive(Class<?> clazz) {
        return WRAPPER_TO_PRIMITIVE.get(clazz);
    }

    /**
     * 将基本类型类转换为对应的包装类.
     *
     * @param clazz {@link Class}
     * @return {@link Class}
     */
    public static Class<?> toWrapper(Class<?> clazz) {
        return PRIMITIVE_TO_WRAPPER.get(clazz);
    }

    /**
     * 判断 from 类型的对象是否可以赋值给 to 类型的对象.
     *
     * @param from {@link Class}
     * @param to   {@link Class}
     * @return {@code boolean}
     */
    public static boolean isAssignable(Class<?> from, Class<?> to) {
        if (to == null) {
            return false;
        }
        if (from == null) {
            // null 不能赋值给基本类型
            return !to.isPrimitive();
        }

        if (from.equals(to)) {
            return true;
        }

        // 基本类型处理
        if (from.isPrimitive() || to.isPrimitive()) {
            return isPrimitiveAssignable(from, to);
        }
        // 继承关系
        return to.isAssignableFrom(from);
    }

    /**
     * 存在基本类型时, 判断 from 类型对象是否可以赋值给 to 类型对象.
     *
     * @param from {@link Class}
     * @param to   {@link Class}
     * @return {@code boolean}
     */
    private static boolean isPrimitiveAssignable(Class<?> from, Class<?> to) {
        // 获取实际的基本类型
        Class<?> fromPrimitive = from.isPrimitive() ? from : toPrimitive(from);
        Class<?> toPrimitive = to.isPrimitive() ? to : toPrimitive(to);

        // 至少有一方不是基本类型或其包装类
        if (fromPrimitive == null || toPrimitive == null) {
            // 包装类型转基本类型
            if (to.isPrimitive() && isWrapper(from)) {
                return toPrimitive(from).equals(to);
            }
            // 基本类型转包装类型
            if (from.isPrimitive() && isWrapper(to)) {
                return toWrapper(from).equals(to);
            }
            return false;
        }

        if (fromPrimitive.equals(toPrimitive)) {
            return true;
        }
        return isPrimitiveWidening(fromPrimitive, toPrimitive);
    }

    /**
     * 使用基本类型的宽化转换规则, 判断 from 类型对象是否可以赋值给 to 类型对象..
     *
     * @param from {@link Class}
     * @param to   {@link Class}
     * @return {@code boolean}
     */
    private static boolean isPrimitiveWidening(Class<?> from, Class<?> to) {
        Class<?>[] widens = PRIMITIVE_WIDEN.get(from);
        if (widens == null) {
            return false;
        }

        for (Class<?> widen : widens) {
            if (widen.equals(to)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过全路径类名获取 {@link Class}.
     *
     * @param className 全路径类名
     * @return {@link Class}
     */
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取 {@link Object} 数组对应的 {@link Class} 数组.
     *
     * @param objects {@link Object} 数组
     * @return {@code Class<?>[]}
     */
    public static Class<?>[] getClasses(Object... objects) {
        if (objects == null) {
            return new Class<?>[0];
        }

        Class<?>[] classes = new Class<?>[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            classes[i] = obj == null ? null : obj.getClass();
        }
        return classes;
    }
}

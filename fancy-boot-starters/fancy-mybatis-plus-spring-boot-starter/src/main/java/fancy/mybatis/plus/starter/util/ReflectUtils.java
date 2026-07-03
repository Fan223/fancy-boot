package fancy.mybatis.plus.starter.util;

import lombok.experimental.UtilityClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 反射工具类.
 *
 * @author Fan
 */
@UtilityClass
public class ReflectUtils {

    /**
     * 通过全路径类名和构造参数创建 {@link Object} 实例.
     *
     * @param className 全路径类名
     * @param args      构造参数
     * @return {@link Object}
     */
    public static Object getInstance(String className, Object... args) {
        return getInstance(ClassUtils.getClass(className), args);
    }

    /**
     * 通过 {@link Class} 和构造参数创建其实例.
     *
     * @param clazz {@link Class}
     * @param args  构造参数
     * @param <T>   泛型类型
     * @return {@link T}
     */
    public static <T> T getInstance(Class<T> clazz, Object... args) {
        try {
            Constructor<T> constructor = getConstructor(clazz, args);
            return constructor.newInstance(args);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 通过 {@link Class} 和构造参数获取对应的构造方法.
     *
     * @param clazz {@link Class}
     * @param args  构造参数
     * @param <T>   泛型类型
     * @return {@link Constructor}
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) {
        // 获取参数类型数组
        Class<?>[] parameterTypes = ClassUtils.getClasses(args);
        // 获取所有构造方法
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        // 查找匹配的构造方法
        Constructor<?> constructor = findMatchConstructor(constructors, parameterTypes);

        if (constructor == null) {
            throw new ReflectException(
                    new NoSuchMethodException("No such constructor: " + clazz.getName() +
                            " with parameters: " + Arrays.toString(parameterTypes))
            );
        }
        constructor.setAccessible(true);
        return (Constructor<T>) constructor;
    }

    /**
     * 查找匹配的构造方法.
     *
     * @param constructors   构造方法数组
     * @param parameterTypes 参数类型数组
     * @return {@link Constructor}
     */
    private static Constructor<?> findMatchConstructor(Constructor<?>[] constructors, Class<?>[] parameterTypes) {
        for (Constructor<?> constructor : constructors) {
            if (isParameterMatch(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * 判断参数类型是否匹配, 支持基本类型和包装类类型以及继承关系的匹配.
     *
     * @param declaredParameterTypes 声明的参数类型数组
     * @param parameterTypes         实际参数类型数组
     * @return {@code boolean}
     */
    private static boolean isParameterMatch(Class<?>[] declaredParameterTypes, Class<?>[] parameterTypes) {
        if (declaredParameterTypes.length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < declaredParameterTypes.length; i++) {
            if (!ClassUtils.isAssignable(parameterTypes[i], declaredParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过方法名获取 {@link Object} 中指定参数的方法.
     *
     * @param obj        {@link Object}
     * @param methodName 方法名
     * @param args       参数
     * @return {@link Method}
     */
    public static Method getMethod(Object obj, String methodName, Object... args) {
        return getMethod(obj.getClass(), methodName, args);
    }

    /**
     * 通过方法名获取 {@link Class} 中指定参数的方法.
     *
     * @param clazz      {@link Class}
     * @param methodName 方法名
     * @param args       参数类型
     * @return {@link Method}
     */
    public static Method getMethod(Class<?> clazz, String methodName, Object... args) {
        Class<?>[] parameterTypes = ClassUtils.getClasses(args);
        Method[] methods = clazz.getDeclaredMethods();

        Method method = findMatchMethod(methods, methodName, parameterTypes);
        if (method == null) {
            throw new ReflectException(
                    new NoSuchMethodException("No such method: " + clazz.getName() +
                            "." + methodName + " with parameters: " + Arrays.toString(parameterTypes))
            );
        }
        method.setAccessible(true);
        return method;
    }

    /**
     * 查找匹配的方法.
     *
     * @param methods        方法数组
     * @param methodName     方法名
     * @param parameterTypes 参数类型数组
     * @return {@link Method}
     */
    private static Method findMatchMethod(Method[] methods, String methodName, Class<?>[] parameterTypes) {
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (isParameterMatch(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 通过方法名执行 {@link Object} 的方法.
     *
     * @param obj        {@link Object}
     * @param methodName 方法名
     * @param args       参数
     * @param <T>        泛型类型
     * @return {@link Object}
     */
    public static <T> T invoke(Object obj, String methodName, Object... args) {
        return invoke(obj, getMethod(obj, methodName, args), args);
    }

    /**
     * 执行 {@link Object} 的方法.
     *
     * @param obj    对象
     * @param method 方法
     * @param args   参数
     * @param <T>    泛型类型
     * @return {@link Object}
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, Method method, Object... args) {
        try {
            method.setAccessible(true);
            if (method.isDefault()) {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle handle = lookup.unreflect(method);
                return (T) handle.invokeWithArguments(args);
            }
            return (T) method.invoke(obj, args);
        } catch (Throwable e) {
            throw new ReflectException(e);
        }
    }
}

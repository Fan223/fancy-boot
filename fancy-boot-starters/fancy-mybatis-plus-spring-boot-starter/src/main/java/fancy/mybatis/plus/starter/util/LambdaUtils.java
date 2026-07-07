package fancy.mybatis.plus.starter.util;

import fancy.boot.core.lang.StringUtils;
import fancy.mybatis.plus.starter.util.reflect.ReflectUtils;
import lombok.experimental.UtilityClass;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

/**
 * Lambda 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class LambdaUtils {

    /**
     * 获取 {@link SerializableFunction} 对应的方法名.
     *
     * @param func {@link SerializableFunction}
     * @param <T>  入参类型, 即方法所属对象的类型
     * @return 方法名
     */
    public static <T> String getMethodName(SerializableFunction<T, ?> func) {
        return resolve(func).getImplMethodName();
    }

    /**
     * 将 {@link Serializable} 解析为 {@link SerializedLambda}.
     *
     * @param func {@link Serializable}
     * @return {@link SerializedLambda}
     */
    public static SerializedLambda resolve(Serializable func) {
        /*
         * Lambda 表达式序列化时, JVM 会添加一个 writeReplace() 方法, 返回一个 SerializedLambda, 即真正被序列化的对象.
         * 该对象包含了 Lambda 表达式的所有信息, 比如函数名 implMethodName 等.
         */
        return ReflectUtils.invoke(func, "writeReplace");
    }

    /**
     * 获取 {@link SerializableFunction} 对应的字段名.
     *
     * @param func {@link SerializableFunction}
     * @param <T>  入参类型, 即方法所属对象的类型
     * @return 字段名
     */
    public static <T> String getFieldName(SerializableFunction<T, ?> func) {
        String methodName = getMethodName(func);
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return StringUtils.lowerFirst(methodName.substring(3));
        } else if (methodName.startsWith("is")) {
            return StringUtils.lowerFirst(methodName.substring(2));
        }
        throw new IllegalArgumentException("Invalid Getter or Setter name: " + methodName);
    }
}

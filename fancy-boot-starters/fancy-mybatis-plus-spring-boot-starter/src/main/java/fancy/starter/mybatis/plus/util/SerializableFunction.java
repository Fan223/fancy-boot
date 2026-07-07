package fancy.starter.mybatis.plus.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * {@link Serializable} 函数式接口.
 *
 * @param <T> 函数输入类型
 * @param <R> 函数结果类型
 * @author Fan
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * 将函数应用于给定的参数.
     *
     * @param t 函数参数
     * @return 函数结果
     */
    R apply(T t);
}

package fancy.mybatis.plus.starter.util;

import java.io.Serial;

/**
 * 反射异常类.
 *
 * @author Fan
 */
public class ReflectException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7065123880961484444L;

    /**
     * 构造方法, 指定 {@link Throwable}.
     *
     * @param cause {@link Throwable}
     */
    public ReflectException(Throwable cause) {
        super(cause);
    }
}

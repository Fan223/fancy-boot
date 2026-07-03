package fancy.boot.core.http;

import java.io.Serial;

/**
 * 网络异常类.
 *
 * @author Fan
 */
public class NetException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5008667853996526199L;

    /**
     * 构造方法, 指定错误信息和 {@link Throwable}.
     *
     * @param message 错误信息
     * @param cause   {@link Throwable}
     */
    public NetException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造方法, 指定 {@link Throwable}.
     *
     * @param cause {@link Throwable}
     */
    public NetException(Throwable cause) {
        super(cause);
    }
}

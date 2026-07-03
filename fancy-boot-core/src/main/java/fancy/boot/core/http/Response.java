package fancy.boot.core.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * 统一响应类.
 *
 * @param code    状态码
 * @param message 消息
 * @param data    数据
 * @param <T>     数据类型
 * @author Fan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(int code, String message, T data) {

    /**
     * 构造方法校验, 消息不能为空.
     */
    public Response {
        Objects.requireNonNull(message, "message must not be null.");
    }

    /**
     * 返回使用指定 {@link HttpStatus} 创建的 {@link Response} 实例.
     *
     * @param status {@link HttpStatus}
     * @param data   数据
     * @return {@link Response}
     */
    public static <T> Response<T> of(HttpStatus status, T data) {
        return of(status.value(), status.reasonPhrase(), data);
    }

    /**
     * 返回使用指定状态码和消息创建的 {@link Response} 实例, 数据为 {@code null}.
     *
     * @param code    状态码
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> of(int code, String message) {
        return of(code, message, null);
    }

    /**
     * 返回使用指定状态码、消息和数据创建的 {@link Response} 实例.
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @return {@link Response}
     */
    public static <T> Response<T> of(int code, String message, T data) {
        return new Response<>(code, message, data);
    }

    /**
     * 成功响应, 使用 {@link HttpStatus#OK}, 数据为 {@code null}.
     *
     * @return {@link Response<T>}
     */
    public static <T> Response<T> success() {
        return success(null);
    }

    /**
     * 成功响应, 使用 {@link HttpStatus#OK}, 指定数据.
     *
     * @param data 数据
     * @return {@link Response}
     */
    public static <T> Response<T> success(T data) {
        return success(HttpStatus.OK.reasonPhrase(), data);
    }

    /**
     * 成功响应, 使用 {@link HttpStatus#OK} 状态码, 指定消息和数据.
     *
     * @param message 消息
     * @param data    数据
     * @return {@link Response}
     */
    public static <T> Response<T> success(String message, T data) {
        return of(HttpStatus.OK.value(), message, data);
    }

    /**
     * 失败响应, 使用 {@link HttpStatus#INTERNAL_SERVER_ERROR}, 数据为 {@code null}.
     *
     * @return {@link Response}
     */
    public static <T> Response<T> fail() {
        return fail(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
    }

    /**
     * 失败响应, 使用 {@link HttpStatus#INTERNAL_SERVER_ERROR} 状态码, 指定消息, 数据为 {@code null}.
     *
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> fail(String message) {
        return fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    /**
     * 失败响应, 指定状态码和消息, 数据为 {@code null}.
     *
     * @param code    状态码
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> fail(int code, String message) {
        return of(code, message);
    }

    /**
     * {@link HttpStatus#BAD_REQUEST} 响应, 指定消息, 数据为 {@code null}.
     *
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> badRequest(String message) {
        return of(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * {@link HttpStatus#UNAUTHORIZED} 响应, 指定消息, 数据为 {@code null}.
     *
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> unauthorized(String message) {
        return of(HttpStatus.UNAUTHORIZED.value(), message);
    }

    /**
     * {@link HttpStatus#FORBIDDEN} 响应, 指定消息, 数据为 {@code null}.
     *
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> forbidden(String message) {
        return of(HttpStatus.FORBIDDEN.value(), message);
    }

    /**
     * {@link HttpStatus#NOT_FOUND} 响应, 指定消息, 数据为 {@code null}.
     *
     * @param message 消息
     * @return {@link Response}
     */
    public static <T> Response<T> notFound(String message) {
        return of(HttpStatus.NOT_FOUND.value(), message);
    }

    /**
     * 判断是否为成功响应.
     *
     * @return {@code boolean}
     */
    @JsonIgnore
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * 判断是否为失败响应.
     *
     * @return {@code boolean}
     */
    @JsonIgnore
    public boolean isFail() {
        return !isSuccess();
    }
}

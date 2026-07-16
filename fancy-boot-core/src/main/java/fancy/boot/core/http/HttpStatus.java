package fancy.boot.core.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HTTP 状态码.
 *
 * @author Fan
 */
@AllArgsConstructor
@Getter
public enum HttpStatus {

    // 2xx, Success
    OK(200, "OK"),

    // 4xx, Client Error
    UNAUTHORIZED(401, "Unauthorized"),

    FORBIDDEN(403, "Forbidden"),

    // 5xx, Server Error
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    /**
     * 状态码数值.
     */
    private final int value;

    /**
     * 状态码描述.
     */
    private final String reasonPhrase;
}

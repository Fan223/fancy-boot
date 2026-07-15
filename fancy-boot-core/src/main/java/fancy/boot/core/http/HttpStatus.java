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

    // 4xx, Client Error
    UNAUTHORIZED(401),

    FORBIDDEN(403),

    // 5xx, Server Error
    INTERNAL_SERVER_ERROR(500);

    /**
     * 状态码数值.
     */
    private final int value;
}

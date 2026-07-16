package fancy.starter.server.resource.authentication;

import lombok.experimental.UtilityClass;

/**
 * 内部认证共用常量.
 *
 * @author Fan
 */
@UtilityClass
public class InternalAuthenticationConstants {

    /**
     * 内部认证请求头.
     */
    public static final String HEADER = "X-Internal-Token";

    /**
     * 内部认证默认主体标识.
     */
    public static final String PRINCIPAL = "internal-service";
}

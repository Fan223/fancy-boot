package fancy.starter.server.resource.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 资源服务器配置, 绑定 {@code fancy.security.*} 命名空间.
 *
 * @author Fan
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fancy.security")
public class ResourceServerProperties {

    /**
     * JWT 解析配置.
     */
    private Jwt jwt = new Jwt();

    /**
     * 内部认证 Token 配置.
     */
    private InternalToken internalToken = new InternalToken();

    /**
     * CORS 配置.
     */
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {

        /**
         * 权限信息在 Access Token Claims 中的 Key.
         */
        private String claimName = "authorities";

        /**
         * 解析权限信息的前缀, 为空则表示去掉前缀.
         */
        private String authorityPrefix = "";
    }

    @Getter
    @Setter
    public static class InternalToken {

        /**
         * 内部服务调用共享 Token, 为空则表示不启用内部认证过滤器.
         */
        private String token = "";
    }

    @Getter
    @Setter
    public static class Cors {

        /**
         * 允许的来源.
         */
        private List<String> allowedOrigins = List.of("*");

        /**
         * 允许的 HTTP 方法.
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");

        /**
         * 允许的请求头.
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * 是否允许携带凭证.
         */
        private boolean allowCredentials = true;

        /**
         * 预检请求缓存时间(秒).
         */
        private long maxAge = 3600L;
    }
}

package fancy.starter.server.resource.reactive.authentication;

import fancy.boot.core.lang.StringUtils;
import fancy.starter.server.resource.authentication.InternalAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * {@link ConditionalOnWebApplication.Type#REACTIVE} 内部认证过滤器, 在 JWT 校验前运行.
 * 当请求携带正确的 X-Internal-Token 时注入内部认证, 实现服务间内部调用免 JWT 认证.
 *
 * @author Fan
 */
@RequiredArgsConstructor
public class ReactiveInternalAuthenticationFilter implements WebFilter {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private static final String INTERNAL_SERVICE = "internal-service";

    private final String internalToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String requestToken = exchange.getRequest().getHeaders().getFirst(INTERNAL_TOKEN_HEADER);
        if (StringUtils.isNotBlank(requestToken) && StringUtils.isNotBlank(internalToken)
                && MessageDigest.isEqual(requestToken.getBytes(StandardCharsets.UTF_8), internalToken.getBytes(StandardCharsets.UTF_8))) {
            InternalAuthenticationToken authentication = new InternalAuthenticationToken(INTERNAL_SERVICE);
            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        return chain.filter(exchange);
    }
}

package fancy.starter.server.resource.servlet.authentication;

import fancy.boot.core.lang.StringUtils;
import fancy.starter.server.resource.authentication.InternalAuthenticationConstants;
import fancy.starter.server.resource.authentication.InternalAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * {@link ConditionalOnWebApplication.Type#SERVLET} 内部认证过滤器, 在 JWT 校验前运行.
 * 当请求携带正确的 {@code X-Internal-Token} 时注入内部认证, 实现服务间内部调用免 JWT 认证.
 *
 * @author Fan
 */
@RequiredArgsConstructor
public class ServletInternalAuthenticationFilter extends OncePerRequestFilter {

    private final String internalToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestToken = request.getHeader(InternalAuthenticationConstants.HEADER);
        if (StringUtils.isNotBlank(requestToken) && StringUtils.isNotBlank(internalToken)
                && MessageDigest.isEqual(requestToken.getBytes(StandardCharsets.UTF_8), internalToken.getBytes(StandardCharsets.UTF_8))) {
            InternalAuthenticationToken internalAuthenticationToken = new InternalAuthenticationToken(InternalAuthenticationConstants.PRINCIPAL);
            SecurityContextHolder.getContext().setAuthentication(internalAuthenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}

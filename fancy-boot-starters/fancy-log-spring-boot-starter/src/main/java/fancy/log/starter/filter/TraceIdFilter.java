package fancy.log.starter.filter;

import fancy.boot.core.lang.StringUtils;
import fancy.boot.core.util.IdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceId 过滤器.
 *
 * @author Fan
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_HEADER);
        if (StringUtils.isBlank(traceId)) {
            traceId = IdUtils.generateSnowflakeIdStr();
        }

        try {
            ThreadContext.put(TRACE_ID, traceId);
            response.setHeader(TRACE_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.remove(TRACE_ID);
        }
    }
}

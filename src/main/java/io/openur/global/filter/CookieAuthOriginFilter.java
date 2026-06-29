package io.openur.global.filter;

import io.openur.global.config.CorsAllowedOriginPatterns;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CookieAuthOriginFilter extends OncePerRequestFilter {

    private final CorsAllowedOriginPatterns corsAllowedOriginPatterns;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return !HttpMethod.POST.matches(request.getMethod())
            || (!"/v1/auth/login-nonce".equals(requestURI)
                && !"/v1/auth/refresh".equals(requestURI)
                && !"/v1/auth/logout".equals(requestURI)
                && !requestURI.startsWith("/v1/users/login/"));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!StringUtils.hasText(origin) || !corsAllowedOriginPatterns.isAllowed(origin)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden origin");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

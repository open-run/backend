package io.openur.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.openur.global.dto.ExceptionDto;
import io.openur.global.jwt.JwtUtil;
import io.openur.global.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


@RequiredArgsConstructor
@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = jwtUtil.getJwtFromHeader(request);

        // Authorization header 가 주어지지 않은 경우 존재 필요성 검사를 다른 필터에서 진행하기 때문에 본 필터를 건너띈다.
        if (!StringUtils.hasText(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(jwtToken)) {
            this.handleException(response, "Token is invalid, 유효하지 않은 JWT 토큰입니다.");
            return;
        }

        Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
        String email = claims.getSubject();

        try {
            this.setAuthentication(email);
        } catch (UsernameNotFoundException e) {
            this.handleException(response, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String username) throws UsernameNotFoundException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String email) throws UsernameNotFoundException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
    }

    private void handleException(HttpServletResponse response, String message) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        log.error(message);
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        ExceptionDto exceptionDto = ExceptionDto.builder()
            .statusCode(status.value())
            .state(status)
            .message(message)
            .build();

        String exception = objectMapper.writeValueAsString(exceptionDto);
        response.getWriter().write(exception);
    }
}

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


@RequiredArgsConstructor
@Slf4j(topic = "JwtUtil")
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = jwtUtil.getJwtFromHeader(request);

        if (StringUtils.hasText(jwtToken)) {

            if (!jwtUtil.validateToken(jwtToken)) {
                HttpStatus status = HttpStatus.UNAUTHORIZED;
                String errorMessage = "Token is invalid, 검증되지 않은 JWT 토큰입니다.";
                log.error(errorMessage);

                response.setStatus(status.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");

                ExceptionDto exceptionDto = ExceptionDto.builder()
                    .statusCode(status.value())
                    .state(status)
                    .message(errorMessage)
                    .build();

                String exception = objectMapper.writeValueAsString(exceptionDto);
                response.getWriter().write(exception);
                return;
            }

            Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
            String email = claims.getSubject();

            try {
                this.setAuthentication(email);
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
    }
}

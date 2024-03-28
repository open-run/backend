package io.openur.global.Filter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import javax.servlet.Filter;


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String SECRET_KEY;

    private Key key;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String jwtToken = resolveToken(request);
            if (jwtToken != null && validateToken(jwtToken)) {
                Claims claims = parseToken(jwtToken);
                String username = claims.getSubject();
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to set user authentication in security context", e);
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String jwtToken) {
        return true;
    }

    private Claims parseToken(String jwtToken) {
        byte[] bytes = Base64.getDecoder().decode(SECRET_KEY);
        key = Keys.hmacShaKeyFor(bytes);
        return Jwts.parser()
            .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
            .parseClaimsJws(jwtToken)
            .getBody();
    }
}

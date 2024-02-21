package io.openur.global.config;

import io.openur.global.interceptor.LogInterceptor;
import io.openur.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {
    private final JwtUtil jwtUtil;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor()) // 로그 출력 인터셉터
            .order(1)    // 적용할 인터셉터 순서 설정
            .addPathPatterns("/**")
            .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**"); // 인터셉터에서 제외할 패턴

//        registry.addInterceptor(new AuthenticationInterceptor(jwtUtil, validation)) // 토큰 검증
//            .order(2)    // 적용할 필터 순서 설정
//            .addPathPatterns("/**")
//            .excludePathPatterns("/v1/users/**", "/v2/todos/**", "/swagger-ui/**", "/v3/api-docs/**"); // 인터셉터에서 제외할 패턴

    }
}

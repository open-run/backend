package io.openur.oauth;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final OAuth2UserService oAuthService;

    public SecurityConfiguration(OAuth2UserService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .anyRequest().authenticated()
        );
        http.oauth2Login(oauth2Configurer -> oauth2Configurer
            .successHandler(successHandler())
            .userInfoEndpoint(
                userInfoEndpoint -> userInfoEndpoint  // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때 설정을 저장
                    .userService(oAuthService)
                // OAuth2 로그인 성공 시, 후작업을 진행할 UserService 인터페이스 구현체 등록
            )
        );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

            String id = defaultOAuth2User.getAttributes().get("id").toString();
            String body = """
                {"id":"%s"}
                """.formatted(id);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            PrintWriter writer = response.getWriter();
            writer.println(body);
            writer.flush();
        });
    }
}

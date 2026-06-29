package io.openur.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final CorsAllowedOriginPatterns corsAllowedOriginPatterns;

    public WebConfig(CorsAllowedOriginPatterns corsAllowedOriginPatterns) {
        this.corsAllowedOriginPatterns = corsAllowedOriginPatterns;
    }

    // Swagger uses http "OPTIONS" method before sending actual request
    // We call this as pre-flight : Swagger CORS issue
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(corsAllowedOriginPatterns.values().toArray(String[]::new))
            .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}

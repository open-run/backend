package io.openur.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    // Swagger uses http "OPTIONS" method before sending actual request
    // We call this as pre-flight : Swagger CORS issue
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("GET", "POST", "PATCH", "OPTIONS", "DELETE")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600);
	}
}

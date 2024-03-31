package io.openur.global.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerAuthConfig {
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("user")
            .pathsToMatch("/v1/users/login/**", "/v1/users/nickname/exist")
            .addOpenApiCustomizer(buildSecurityOpenApi())
            .build();
    }

    public OpenApiCustomizer buildSecurityOpenApi() {
        return openApi -> openApi.addSecurityItem(
                new SecurityRequirement().addList("jwt token"))
            .getComponents()
            .addSecuritySchemes("jwt token", new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .bearerFormat("JWT")
                .scheme("bearer"));
    }
}

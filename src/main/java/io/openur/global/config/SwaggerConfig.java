package io.openur.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {


    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("unauthenticated")
            .pathsToMatch("/v1/users/login/**", "/v1/users/nickname/exist")
            .build();
    }
    @Bean
    public OpenAPI springOpenurOpenAPI() {
        String title = "openur";
        String description = "openur 프로젝트";

        Info info = new Info().title(title).description(description).version("1.0.0");

        return new OpenAPI().info(info);
    }
}

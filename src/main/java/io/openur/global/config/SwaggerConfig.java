package io.openur.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/v1/**")
            .build();
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        String title = "openur";
        String description = "openur 프로젝트";

        Info info = new Info().title(title).description(description).version("1.0.0");

        return new OpenAPI().info(info);
    }
}

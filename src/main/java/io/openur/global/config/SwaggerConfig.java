package io.openur.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;

public class SwaggerConfig {
    private Docket testDocket(String groupName, Predicate<String> selector) {
        return new Docket(DocumentationType.OAS_30)
            .useDefaultResponseMessages(false)
            .securityContexts(List.of(this.securityContext())) // SecurityContext 설정
            .securitySchemes(List.of(this.apiKey())) // ApiKey 설정
            .groupName("v1")
            .select()
            .apis(RequestHandlerSelectors.
                basePackage("패키지명"))
            .paths(PathSelectors.ant("/v1/**")).build();
    }

    // JWT SecurityContext 구성
    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return List.of(new SecurityReference("Authorization", authorizationScopes));
    }

    // ApiKey 정의
    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/v1/**")
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

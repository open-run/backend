package io.openur.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final boolean serveLocalNftAssets;
    private final String localNftAssetRoot;

    public WebConfig(
        @Value("${openrun.nft.assets.serve-local:false}") boolean serveLocalNftAssets,
        @Value("${openrun.nft.assets.local-root:}") String localNftAssetRoot
    ) {
        this.serveLocalNftAssets = serveLocalNftAssets;
        this.localNftAssetRoot = localNftAssetRoot;
    }

    // Swagger uses http "OPTIONS" method before sending actual request
    // We call this as pre-flight : Swagger CORS issue
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PATCH", "OPTIONS", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        if (!serveLocalNftAssets || !StringUtils.hasText(localNftAssetRoot)) {
            return;
        }

        String resourceLocation = Path.of(localNftAssetRoot)
            .toAbsolutePath()
            .normalize()
            .toUri()
            .toString();

        registry.addResourceHandler("/local-nft-assets/**")
            .addResourceLocations(resourceLocation);
    }
}

package io.openur.global.config;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

@Component
public class CorsAllowedOriginPatterns {

    private static final List<String> PROD_VALUES = List.of(
        "https://open-run.vercel.app",
        "https://open-run-admin.vercel.app",
        "https://open-run.xyz",
        "https://www.open-run.xyz"
    );

    private static final List<String> LOCAL_VALUES = List.of(
        "http://localhost:*",
        "http://127.0.0.1:*",
        "https://laughably-unblended-tiera.ngrok-free.dev"
    );

    private final Environment environment;
    private final CorsConfiguration corsConfiguration = new CorsConfiguration();
    private List<String> values = PROD_VALUES;

    public CorsAllowedOriginPatterns(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void init() {
        List<String> resolvedValues = new ArrayList<>(PROD_VALUES);
        if (environment.acceptsProfiles(Profiles.of("local", "test"))) {
            resolvedValues.addAll(LOCAL_VALUES);
        }

        String extraOrigins = environment.getProperty("openrun.cors.extra-allowed-origin-patterns");
        if (StringUtils.hasText(extraOrigins)) {
            Arrays.stream(extraOrigins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(resolvedValues::add);
        }

        values = List.copyOf(resolvedValues);
        corsConfiguration.setAllowedOriginPatterns(values);
    }

    public List<String> values() {
        return values;
    }

    public boolean isAllowed(String origin) {
        return corsConfiguration.checkOrigin(origin) != null;
    }
}

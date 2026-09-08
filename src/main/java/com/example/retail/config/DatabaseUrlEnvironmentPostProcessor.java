package com.example.retail.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * When profile {@code prod} is active, turn Neon/Render {@code DATABASE_URL}
 * (or {@code SPRING_DATASOURCE_URL}) into {@code spring.datasource.url}.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }
        String raw = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("spring.datasource.url"),
                environment.getProperty("DATABASE_URL")
        );
        String jdbc = DatabaseUrls.toJdbcUrl(raw);
        if (jdbc == null || jdbc.isBlank() || jdbc.equals(environment.getProperty("spring.datasource.url"))) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.url", jdbc);
        environment.getPropertySources().addFirst(new MapPropertySource("prodDatabaseUrl", map));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

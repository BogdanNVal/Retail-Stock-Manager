package com.example.retail.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Turn DATABASE_URL (postgres://…) into JDBC properties before profiles finish loading.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("spring.datasource.url"),
                environment.getProperty("DATABASE_URL")
        );
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse(raw);
        if (parsed == null || parsed.jdbcUrl() == null || parsed.jdbcUrl().isBlank()) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        String currentUrl = environment.getProperty("spring.datasource.url");
        if (!parsed.jdbcUrl().equals(currentUrl)) {
            map.put("spring.datasource.url", parsed.jdbcUrl());
        }
        if (parsed.username() != null && firstNonBlank(environment.getProperty("SPRING_DATASOURCE_USERNAME")) == null) {
            map.put("spring.datasource.username", parsed.username());
        }
        if (parsed.password() != null && firstNonBlank(environment.getProperty("SPRING_DATASOURCE_PASSWORD")) == null) {
            map.put("spring.datasource.password", parsed.password());
        }
        if (!map.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("prodDatabaseUrl", map));
        }
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

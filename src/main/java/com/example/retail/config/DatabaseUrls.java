package com.example.retail.config;

/**
 * Converts hosted DATABASE_URL values (Neon, Render, Heroku) into a JDBC URL
 * Spring Boot's PostgreSQL driver accepts.
 */
public final class DatabaseUrls {

    private DatabaseUrls() {
    }

    public static String toJdbcUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String url = raw.trim();
        if (url.isEmpty()) {
            return url;
        }
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        return url;
    }
}

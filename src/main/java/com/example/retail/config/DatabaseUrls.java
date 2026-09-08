package com.example.retail.config;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Converts hosted DATABASE_URL values (Neon, Render, Heroku) into a JDBC URL
 * and optional username/password. The PostgreSQL driver does not treat
 * {@code jdbc:postgresql://user:pass@host/db} as credentials in the host part.
 */
public final class DatabaseUrls {

    public record Parsed(String jdbcUrl, String username, String password) {
    }

    private DatabaseUrls() {
    }

    public static String toJdbcUrl(String raw) {
        Parsed parsed = parse(raw);
        return parsed == null ? null : parsed.jdbcUrl();
    }

    public static Parsed parse(String raw) {
        if (raw == null) {
            return null;
        }
        String url = raw.trim();
        if (url.isEmpty()) {
            return new Parsed("", null, null);
        }
        if (url.startsWith("jdbc:postgresql://")) {
            return parseUri("postgresql://" + url.substring("jdbc:postgresql://".length()), url);
        }
        if (url.startsWith("postgres://")) {
            return parseUri("postgresql://" + url.substring("postgres://".length()), null);
        }
        if (url.startsWith("postgresql://")) {
            return parseUri(url, null);
        }
        return new Parsed(url, null, null);
    }

    private static Parsed parseUri(String postgresqlUri, String jdbcIfUnchanged) {
        try {
            URI uri = new URI(postgresqlUri);
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    username = userInfo.substring(0, colon);
                    password = userInfo.substring(colon + 1);
                } else {
                    username = userInfo;
                }
            }
            if (uri.getHost() == null) {
                return new Parsed(jdbcIfUnchanged != null ? jdbcIfUnchanged : toJdbcPrefix(postgresqlUri), username, password);
            }
            StringBuilder jdbc = new StringBuilder("jdbc:postgresql://");
            jdbc.append(uri.getHost());
            if (uri.getPort() > 0) {
                jdbc.append(':').append(uri.getPort());
            }
            if (uri.getRawPath() != null) {
                jdbc.append(uri.getRawPath());
            }
            if (uri.getRawQuery() != null) {
                jdbc.append('?').append(uri.getRawQuery());
            }
            return new Parsed(jdbc.toString(), username, password);
        } catch (URISyntaxException ex) {
            String fallback = jdbcIfUnchanged != null ? jdbcIfUnchanged : toJdbcPrefix(postgresqlUri);
            return new Parsed(fallback, null, null);
        }
    }

    private static String toJdbcPrefix(String postgresqlUri) {
        if (postgresqlUri.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + postgresqlUri.substring("postgresql://".length());
        }
        return postgresqlUri;
    }
}

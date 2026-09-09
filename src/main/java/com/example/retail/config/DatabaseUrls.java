package com.example.retail.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Neon/Render hand you postgres://user:pass@host/db. The JDBC driver does
 * not treat user:pass in the host as credentials, so we split them out.
 * Passwords can contain @ — we use the last @ before the path.
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
            return parsePostgresFamily(url.substring("jdbc:".length()), true);
        }
        if (url.startsWith("postgres://")) {
            return parsePostgresFamily("postgresql://" + url.substring("postgres://".length()), false);
        }
        if (url.startsWith("postgresql://")) {
            return parsePostgresFamily(url, false);
        }
        return new Parsed(url, null, null);
    }

    private static Parsed parsePostgresFamily(String postgresqlUri, boolean alreadyJdbcShape) {
        final String prefix = "postgresql://";
        if (!postgresqlUri.startsWith(prefix)) {
            return new Parsed(alreadyJdbcShape ? "jdbc:" + postgresqlUri : toJdbcPrefix(postgresqlUri), null, null);
        }
        String rest = postgresqlUri.substring(prefix.length());
        int pathStart = indexOfPathOrQuery(rest);
        String authority = pathStart >= 0 ? rest.substring(0, pathStart) : rest;
        String pathAndQuery = pathStart >= 0 ? rest.substring(pathStart) : "";

        String username = null;
        String password = null;
        String hostPort = authority;
        int at = authority.lastIndexOf('@');
        if (at >= 0) {
            String userInfo = authority.substring(0, at);
            hostPort = authority.substring(at + 1);
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = decode(userInfo.substring(0, colon));
                password = decode(userInfo.substring(colon + 1));
            } else if (!userInfo.isBlank()) {
                username = decode(userInfo);
            }
        }
        if (hostPort.isBlank()) {
            return new Parsed(alreadyJdbcShape ? "jdbc:" + postgresqlUri : toJdbcPrefix(postgresqlUri), username, password);
        }

        String path = pathAndQuery;
        String query = null;
        int q = pathAndQuery.indexOf('?');
        if (q >= 0) {
            path = pathAndQuery.substring(0, q);
            query = sanitizeQuery(pathAndQuery.substring(q + 1));
        }

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://");
        jdbc.append(hostPort);
        jdbc.append(path.isEmpty() ? "" : path);
        if (query != null && !query.isEmpty()) {
            jdbc.append('?').append(query);
        }
        return new Parsed(jdbc.toString(), username, password);
    }

    private static int indexOfPathOrQuery(String rest) {
        int slash = rest.indexOf('/');
        int q = rest.indexOf('?');
        if (slash < 0) {
            return q;
        }
        if (q < 0) {
            return slash;
        }
        return Math.min(slash, q);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }

    /**
     * Neon likes to append channel_binding=require. Spring Boot 3.2's pgJDBC
     * doesn't know that name and can stall. Drop it; keep sslmode=require.
     */
    static String sanitizeQuery(String rawQuery) {
        StringBuilder out = new StringBuilder();
        for (String part : rawQuery.split("&")) {
            if (part.isEmpty()) {
                continue;
            }
            String key = part;
            int eq = part.indexOf('=');
            if (eq >= 0) {
                key = part.substring(0, eq);
            }
            if ("channel_binding".equalsIgnoreCase(key) || "channelBinding".equalsIgnoreCase(key)) {
                continue;
            }
            if (out.length() > 0) {
                out.append('&');
            }
            out.append(part);
        }
        return out.toString();
    }

    private static String toJdbcPrefix(String postgresqlUri) {
        if (postgresqlUri.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + postgresqlUri.substring("postgresql://".length());
        }
        return postgresqlUri;
    }
}

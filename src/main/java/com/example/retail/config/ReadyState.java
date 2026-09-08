package com.example.retail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Publishes a tiny readiness signal for the Docker early proxy and fails fast
 * in {@code prod} when the Postgres URL was never configured.
 */
@Component
public class ReadyState {

    private static final Logger log = LoggerFactory.getLogger(ReadyState.class);

    private volatile boolean ready;

    public boolean isReady() {
        return ready;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        ready = true;
        log.info("Application ready — public proxy can forward traffic to Tomcat");
    }

    /** Fails fast in {@code prod} if Postgres was never configured. */
    public static void assertProdDatabaseConfigured(Environment env) {
        if (!env.matchesProfiles("prod")) {
            return;
        }
        String url = env.getProperty("spring.datasource.url");
        if (url == null || url.isBlank() || url.startsWith("${")) {
            throw new IllegalStateException(
                    "prod profile requires DATABASE_URL or SPRING_DATASOURCE_URL "
                            + "(Neon postgresql://user:pass@host/db?sslmode=require). "
                            + "Set it in the Render dashboard Environment tab.");
        }
        if (!url.contains("postgres")) {
            throw new IllegalStateException(
                    "prod datasource URL does not look like Postgres: " + redact(url));
        }
        log.info("prod datasource configured at host {}", hostOf(url));
    }

    private static String hostOf(String jdbcUrl) {
        try {
            String withoutUser = jdbcUrl.replaceFirst("^jdbc:postgresql://[^@]+@", "jdbc:postgresql://");
            int start = withoutUser.indexOf("://") + 3;
            int end = withoutUser.indexOf('/', start);
            if (end < 0) {
                end = withoutUser.length();
            }
            String hostPort = withoutUser.substring(start, end);
            return hostPort.contains("@") ? hostPort.substring(hostPort.indexOf('@') + 1) : hostPort;
        } catch (Exception ex) {
            return "(unparsed)";
        }
    }

    private static String redact(String url) {
        return url.replaceAll("//([^/@:]+):([^/@]+)@", "//$1:***@");
    }
}

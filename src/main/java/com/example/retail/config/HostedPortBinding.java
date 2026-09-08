package com.example.retail.config;

/**
 * Render (and Cloud Run) mark a service "live" as soon as PID 1 is running —
 * often a minute before Tomcat accepts HTTP. When we detect a hosted
 * environment, bind the public {@code PORT} immediately with
 * {@link EarlyBindProxy} and put Tomcat on a loopback port behind it.
 */
public final class HostedPortBinding {

    public record Ports(int publicPort, int internalPort, boolean proxy) {
    }

    private HostedPortBinding() {
    }

    public static Ports fromEnvironment() {
        return resolve(
                System.getenv("PORT"),
                System.getenv("RENDER"),
                System.getenv("K_SERVICE"),
                System.getenv("SPRING_PROFILES_ACTIVE"));
    }

    static Ports resolve(String portEnv, String render, String kService, String profiles) {
        int publicPort = parsePort(portEnv, 8080);
        boolean hosted = notBlank(render) || notBlank(kService)
                || (notBlank(portEnv) && containsProd(profiles));
        if (!hosted) {
            return new Ports(publicPort, publicPort, false);
        }
        int internalPort = publicPort == 8080 ? 8081 : 8080;
        return new Ports(publicPort, internalPort, true);
    }

    private static boolean containsProd(String profiles) {
        if (profiles == null) {
            return false;
        }
        for (String part : profiles.split(",")) {
            if ("prod".equalsIgnoreCase(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static int parsePort(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int port = Integer.parseInt(value.trim());
            return port > 0 && port <= 65535 ? port : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

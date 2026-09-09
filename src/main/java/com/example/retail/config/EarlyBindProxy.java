package com.example.retail.config;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Grab PORT immediately so Render's scanner (and the browser) get a TCP
 * connection. Until Tomcat is up, people see a "starting" page instead of
 * connection refused.
 */
public final class EarlyBindProxy {

    private static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "connection", "content-length", "expect", "host", "keep-alive",
            "proxy-authenticate", "proxy-authorization", "te", "trailers",
            "transfer-encoding", "upgrade");

    private static final Set<String> SKIP_RESPONSE_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailers", "transfer-encoding", "upgrade", "content-length");

    private EarlyBindProxy() {
    }

    public static HttpServer start(int publicPort, int internalPort) throws IOException {
        return start(new InetSocketAddress("0.0.0.0", publicPort), internalPort);
    }

    static HttpServer start(InetSocketAddress bind, int internalPort) throws IOException {
        HttpServer server = HttpServer.create(bind, 0);
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(250))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        server.createContext("/", exchange -> handle(exchange, client, internalPort));
        server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "early-bind-proxy");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        return server;
    }

    static void handle(HttpExchange exchange, HttpClient client, int internalPort) throws IOException {
        boolean committed = false;
        try {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            HttpRequest.Builder builder = HttpRequest.newBuilder(backendUri(exchange.getRequestURI(), internalPort))
                    .timeout(Duration.ofSeconds(120));
            copyRequestHeaders(exchange, builder);
            String method = exchange.getRequestMethod();
            builder.method(method, requestBody.length == 0
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(requestBody));
            HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            Headers out = exchange.getResponseHeaders();
            response.headers().map().forEach((name, values) -> {
                if (name != null && !SKIP_RESPONSE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                    out.put(name, values);
                }
            });
            byte[] body = response.body() == null ? new byte[0] : response.body();
            // HttpServer: length >0 is fixed, 0 is chunked, <0 means no body.
            if (body.length == 0) {
                exchange.sendResponseHeaders(response.statusCode(), -1);
                committed = true;
            } else {
                exchange.sendResponseHeaders(response.statusCode(), body.length);
                committed = true;
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (!committed) {
                writeStartingPage(exchange);
            }
        } catch (Exception ex) {
            if (!committed) {
                writeStartingPage(exchange);
            }
        } finally {
            exchange.close();
        }
    }

    static URI backendUri(URI requestUri, int internalPort) {
        String path = requestUri.getRawPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        String query = requestUri.getRawQuery();
        String url = "http://127.0.0.1:" + internalPort + path;
        if (query != null && !query.isEmpty()) {
            url += "?" + query;
        }
        return URI.create(url);
    }

    static String startingPageHtml() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta http-equiv="refresh" content="5"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>Starting — Retail Stock Manager</title>
                  <style>
                    body { margin:0; font-family:"Segoe UI","Helvetica Neue",sans-serif;
                      color:#1f2a24; background:linear-gradient(180deg,#ebe6db,#e7efe9); min-height:100vh; }
                    main { max-width:40rem; margin:12vh auto; padding:2rem;
                      background:#fffdf8; border:1px solid #d7d0c3; border-radius:12px; }
                    h1 { color:#1f6f5b; }
                    p { color:#5c6b63; line-height:1.5; }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Retail Stock Manager is starting</h1>
                    <p>Render already says the service is live, but Spring is still starting.
                    This page refreshes every 5 seconds. First boot can take a couple of minutes;
                    after idle sleep, more like 30–60 seconds.</p>
                  </main>
                </body>
                </html>
                """;
    }

    private static void copyRequestHeaders(HttpExchange exchange, HttpRequest.Builder builder) {
        Headers in = exchange.getRequestHeaders();
        in.forEach((name, values) -> {
            if (name == null || SKIP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                return;
            }
            for (String value : values) {
                try {
                    builder.header(name, value);
                } catch (IllegalArgumentException ignored) {
                    // HttpRequest rejects some hop-by-hop headers.
                }
            }
        });
        String host = firstHeader(in, "Host");
        if (firstHeader(in, "X-Forwarded-Host") == null && host != null) {
            builder.header("X-Forwarded-Host", host);
        }
        if (firstHeader(in, "X-Forwarded-Proto") == null) {
            builder.header("X-Forwarded-Proto", "http");
        }
        String remote = exchange.getRemoteAddress() != null
                ? exchange.getRemoteAddress().getAddress().getHostAddress()
                : null;
        if (firstHeader(in, "X-Forwarded-For") == null && remote != null) {
            builder.header("X-Forwarded-For", remote);
        }
    }

    private static String firstHeader(Headers headers, String name) {
        var values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        String value = values.get(0);
        return value == null || value.isBlank() ? null : value;
    }

    private static void writeStartingPage(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("HEAD".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        if ("/favicon.ico".equals(exchange.getRequestURI().getRawPath())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        byte[] body = startingPageHtml().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}

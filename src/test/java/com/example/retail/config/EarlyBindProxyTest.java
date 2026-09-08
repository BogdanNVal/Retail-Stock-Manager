package com.example.retail.config;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarlyBindProxyTest {

    private HttpServer proxy;
    private HttpServer backend;

    @AfterEach
    void stopServers() {
        if (proxy != null) {
            proxy.stop(0);
        }
        if (backend != null) {
            backend.stop(0);
        }
    }

    @Test
    void startingPage_candTomcatNuEGata() throws Exception {
        proxy = EarlyBindProxy.start(new InetSocketAddress("127.0.0.1", 0), 1);
        int port = proxy.getAddress().getPort();
        HttpResponse<String> response = get("http://127.0.0.1:" + port + "/");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Retail Stock Manager is starting"));
        assertTrue(response.body().contains("refresh"));
    }

    @Test
    void proxy_forwardGetSiQuery() throws Exception {
        backend = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        backend.createContext("/", exchange -> {
            byte[] body = ("ok:" + exchange.getRequestURI()).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        backend.start();

        proxy = EarlyBindProxy.start(new InetSocketAddress("127.0.0.1", 0), backend.getAddress().getPort());
        HttpResponse<String> response = get("http://127.0.0.1:" + proxy.getAddress().getPort() + "/produse?q=1");
        assertEquals(200, response.statusCode());
        assertEquals("ok:/produse?q=1", response.body());
    }

    @Test
    void proxy_forwardPost() throws Exception {
        backend = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        backend.createContext("/", exchange -> {
            byte[] incoming = exchange.getRequestBody().readAllBytes();
            byte[] body = ("posted:" + new String(incoming, StandardCharsets.UTF_8))
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        backend.start();

        proxy = EarlyBindProxy.start(new InetSocketAddress("127.0.0.1", 0), backend.getAddress().getPort());
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + proxy.getAddress().getPort() + "/login"))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("username=admin"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("posted:username=admin", response.body());
    }

    @Test
    void backendUri_pastreazaPathSiQuery() {
        assertEquals(
                URI.create("http://127.0.0.1:8080/api/produse?id=2"),
                EarlyBindProxy.backendUri(URI.create("/api/produse?id=2"), 8080));
    }

    private static HttpResponse<String> get(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

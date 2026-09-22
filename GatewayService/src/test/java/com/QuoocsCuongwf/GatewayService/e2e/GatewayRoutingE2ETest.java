package com.QuoocsCuongwf.GatewayService.e2e;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GatewayRoutingE2ETest {

    private static final StubBackend AUTH_BACKEND = StubBackend.start("auth");
    private static final StubBackend WALLET_BACKEND = StubBackend.start("wallet");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void gatewayBackendUrls(DynamicPropertyRegistry registry) {
        registry.add("AUTH_SERVICE_URL", AUTH_BACKEND::baseUrl);
        registry.add("WALLET_SERVICE_URL", WALLET_BACKEND::baseUrl);
        registry.add("app.cors.allowed-origins", () -> "http://localhost:3000");
    }

    @AfterAll
    static void stopBackends() {
        AUTH_BACKEND.stop();
        WALLET_BACKEND.stop();
    }

    @Test
    void routesAuthRequestsToAuthService() {
        HttpResponse<String> response = send(HttpRequest.newBuilder(gatewayUri("/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"user@example.com\",\"password\":\"secret\"}"))
                .build());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).contains("\"backend\":\"auth\"");

        StubRequest forwarded = AUTH_BACKEND.lastRequest();
        assertThat(forwarded.method()).isEqualTo("POST");
        assertThat(forwarded.path()).isEqualTo("/api/v1/auth/login");
        assertThat(forwarded.body()).contains("user@example.com");
    }

    @Test
    void routesWalletRequestsToWalletServiceAndPreservesHeaders() {
        HttpResponse<String> response = send(HttpRequest.newBuilder(gatewayUri("/api/v1/wallet/transfer"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer jwt-token")
                .header("Idempotency-Key", "idem-gateway-e2e")
                .POST(HttpRequest.BodyPublishers.ofString("{\"toWalletAddress\":\"0xabc\",\"amount\":1000}"))
                .build());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body()).contains("\"backend\":\"wallet\"");

        StubRequest forwarded = WALLET_BACKEND.lastRequest();
        assertThat(forwarded.method()).isEqualTo("POST");
        assertThat(forwarded.path()).isEqualTo("/api/v1/wallet/transfer");
        assertThat(forwarded.firstHeader("Authorization")).isEqualTo("Bearer jwt-token");
        assertThat(forwarded.firstHeader("Idempotency-Key")).isEqualTo("idem-gateway-e2e");
        assertThat(forwarded.body()).contains("0xabc");
    }

    private URI gatewayUri(String path) {
        return URI.create("http://localhost:" + gatewayPort + path);
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("Gateway request failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gateway request interrupted", e);
        }
    }

    private record StubRequest(
            String method,
            String path,
            String body,
            com.sun.net.httpserver.Headers headers
    ) {
        String firstHeader(String name) {
            List<String> values = headers.get(name);
            return values == null || values.isEmpty() ? null : values.get(0);
        }
    }

    private static final class StubBackend {
        private final String name;
        private final HttpServer server;
        private final List<StubRequest> requests = new CopyOnWriteArrayList<>();

        private StubBackend(String name, HttpServer server) {
            this.name = name;
            this.server = server;
        }

        static StubBackend start(String name) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
                StubBackend backend = new StubBackend(name, server);
                server.createContext("/", backend::handle);
                server.setExecutor(Executors.newCachedThreadPool());
                server.start();
                return backend;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start " + name + " test backend", e);
            }
        }

        String baseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        StubRequest lastRequest() {
            assertThat(requests).isNotEmpty();
            return requests.get(requests.size() - 1);
        }

        void stop() {
            server.stop(0);
        }

        private void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(new StubRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    body,
                    exchange.getRequestHeaders()
            ));

            byte[] response = ("{\"backend\":\"" + name + "\",\"path\":\""
                    + exchange.getRequestURI().getPath() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        }
    }
}

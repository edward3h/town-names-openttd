package io.github.edward3h.townnames.bananas.http;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import io.github.edward3h.townnames.bananas.BananasEntry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BananasHttpClientTest {

    private static final String SEARCH_RESPONSE = """
      [
        {
          "content-id": "ABCD1234",
          "md5sum": "abc123",
          "version": "1.0.0",
          "name": "UK Town Names",
          "description": "British place names",
          "license": "GPL v2",
          "author": "TestAuthor"
        }
      ]
      """;

    private static final byte[] FAKE_GRF = {0x01, 0x02, 0x03};

    private HttpServer mockServer;
    private int port;
    private BananasHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);
        port = mockServer.getAddress().getPort();

        mockServer.createContext("/package/newgrf", exchange -> {
            byte[] body = SEARCH_RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        mockServer.createContext("/download/ABCD1234", exchange -> {
            exchange.sendResponseHeaders(200, FAKE_GRF.length);
            exchange.getResponseBody().write(FAKE_GRF);
            exchange.close();
        });

        mockServer.start();
        client = new BananasHttpClient("http://localhost:" + port);
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
        mockServer.stop(0);
    }

    @Test
    void searchReturnsParsedEntries() throws IOException, InterruptedException {
        List<BananasEntry> results = client.search("uk");
        assertEquals(1, results.size());
        assertEquals("UK Town Names", results.get(0).name());
        assertEquals("GPL v2", results.get(0).licence());
    }

    @Test
    void downloadReturnsByteArray() throws IOException, InterruptedException {
        BananasEntry entry = client.search("uk").get(0);
        byte[] bytes = client.download(entry);
        assertArrayEquals(FAKE_GRF, bytes);
    }

    @Test
    void searchThrowsOnNon200() throws IOException {
        mockServer.createContext("/err/package/newgrf", exchange -> {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        try (var errorClient = new BananasHttpClient("http://localhost:" + port + "/err")) {
            assertThrows(IOException.class, () -> errorClient.search("x"));
        }
    }

    @Test
    void downloadThrowsOnNon200() throws IOException, InterruptedException {
        mockServer.createContext("/err/download/ABCD1234", exchange -> {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        });
        try (var errorClient = new BananasHttpClient("http://localhost:" + port + "/err")) {
            BananasEntry entry = client.search("uk").get(0);
            assertThrows(IOException.class, () -> errorClient.download(entry));
        }
    }
}

package io.github.edward3h.townnames.bananas;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BananasClientTest {

  private static final String SEARCH_RESPONSE =
      """
        [{"content-id":"ABCD1234","md5sum":"abc123","version":"1.0.0",
          "name":"UK Town Names","license":"GPL v2","author":"TestAuthor"}]
        """;
  private static final byte[] FAKE_GRF = {0x01, 0x02, 0x03};

  private HttpServer mockServer;
  private int port;

  @BeforeEach
  void setUp() throws IOException {
    mockServer = HttpServer.create(new InetSocketAddress(0), 0);
    port = mockServer.getAddress().getPort();
    mockServer.createContext(
        "/package/newgrf",
        exchange -> {
          byte[] body = SEARCH_RESPONSE.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    mockServer.createContext(
        "/download/ABCD1234",
        exchange -> {
          exchange.sendResponseHeaders(200, FAKE_GRF.length);
          exchange.getResponseBody().write(FAKE_GRF);
          exchange.close();
        });
    mockServer.start();
  }

  @AfterEach
  void tearDown() {
    mockServer.stop(0);
  }

  @Test
  void searchDelegatesToApi(@TempDir Path cacheDir) throws Exception {
    var client =
        BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    List<BananasEntry> results = client.search("uk");
    assertEquals(1, results.size());
    assertEquals("UK Town Names", results.get(0).name());
  }

  @Test
  void downloadCachesOnFirstCall(@TempDir Path cacheDir) throws Exception {
    var client =
        BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    BananasEntry entry = client.search("").get(0);
    GrfDownloadResult result = client.download(entry);
    assertTrue(Files.exists(result.localPath()));
    assertArrayEquals(FAKE_GRF, Files.readAllBytes(result.localPath()));
  }

  @Test
  void downloadReturnsFromCacheOnSecondCall(@TempDir Path cacheDir) throws Exception {
    var client =
        BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    BananasEntry entry = client.search("").get(0);
    GrfDownloadResult first = client.download(entry);
    // Stop mock server — second download must not make a network call
    mockServer.stop(0);
    GrfDownloadResult second = client.download(entry);
    assertEquals(first.localPath(), second.localPath());
  }

  @Test
  void defaultCacheDirIsTildeCache() {
    var client = BananasClient.builder().build();
    Path expected = Path.of(System.getProperty("user.home"), ".cache", "town-names-openttd");
    assertEquals(expected, client.cacheDirectory());
  }
}

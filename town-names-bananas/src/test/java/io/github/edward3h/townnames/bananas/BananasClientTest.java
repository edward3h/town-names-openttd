package io.github.edward3h.townnames.bananas;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
  private BananasClient client;

  @BeforeEach
  void setUp() throws IOException {
    byte[] fakeTar = makeTar("TestPack-1.0/test.grf", FAKE_GRF);
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
          exchange.sendResponseHeaders(200, fakeTar.length);
          exchange.getResponseBody().write(fakeTar);
          exchange.close();
        });
    mockServer.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (client != null) {
      client.close();
    }
    mockServer.stop(0);
  }

  @Test
  void searchDelegatesToApi(@TempDir Path cacheDir) throws Exception {
    client = BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    List<BananasEntry> results = client.search("uk");
    assertEquals(1, results.size());
    assertEquals("UK Town Names", results.get(0).name());
  }

  @Test
  void downloadCachesOnFirstCall(@TempDir Path cacheDir) throws Exception {
    client = BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    BananasEntry entry = client.search("").get(0);
    GrfDownloadResult result = client.download(entry);
    assertTrue(Files.exists(result.localPath()));
    assertArrayEquals(FAKE_GRF, Files.readAllBytes(result.localPath()));
  }

  @Test
  void downloadReturnsFromCacheOnSecondCall(@TempDir Path cacheDir) throws Exception {
    client = BananasClient.builder().cacheDir(cacheDir).baseUrl("http://localhost:" + port).build();
    BananasEntry entry = client.search("").get(0);
    GrfDownloadResult first = client.download(entry);
    // Stop mock server — second download must not make a network call
    mockServer.stop(0);
    GrfDownloadResult second = client.download(entry);
    assertEquals(first.localPath(), second.localPath());
  }

  @Test
  void defaultCacheDirIsTildeCache() throws Exception {
    // This test creates ~/.cache/town-names-openttd as a side effect (GrfCache calls
    // Files.createDirectories eagerly), which is safe and idempotent on all CI runners.
    client = BananasClient.builder().build();
    Path expected = Path.of(System.getProperty("user.home"), ".cache", "town-names-openttd");
    assertEquals(expected, client.cacheDirectory());
  }

  /** Builds a minimal uncompressed TAR containing one file entry. */
  static byte[] makeTar(String filename, byte[] content) {
    int paddedSize = ((content.length + 511) / 512) * 512;
    byte[] tar = new byte[512 + paddedSize + 1024]; // header + data + two zero blocks

    byte[] nameBytes = filename.getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(nameBytes, 0, tar, 0, Math.min(nameBytes.length, 99));

    byte[] mode = "0000644\0".getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(mode, 0, tar, 100, mode.length);

    byte[] sizeField = String.format("%011o\0", content.length).getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(sizeField, 0, tar, 124, sizeField.length);

    tar[156] = '0'; // regular file type

    // Checksum: sum of header bytes with checksum field treated as spaces
    Arrays.fill(tar, 148, 156, (byte) ' ');
    int checksum = 0;
    for (int i = 0; i < 512; i++) checksum += (tar[i] & 0xFF);
    byte[] checksumField = String.format("%06o\0 ", checksum).getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(checksumField, 0, tar, 148, checksumField.length);

    System.arraycopy(content, 0, tar, 512, content.length);
    return tar;
  }
}

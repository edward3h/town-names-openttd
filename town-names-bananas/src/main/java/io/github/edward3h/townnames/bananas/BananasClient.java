package io.github.edward3h.townnames.bananas;

import io.github.edward3h.townnames.bananas.cache.GrfCache;
import io.github.edward3h.townnames.bananas.http.BananasHttpClient;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Downloads and caches NewGRF town-name files from the OpenTTD Bananas content server.
 *
 * <p>Use {@link #builder()} to configure the cache directory and optional custom base URL.
 *
 * <p>Thread safety: not thread-safe. Callers requiring concurrent access must synchronise
 * externally.
 */
public final class BananasClient implements AutoCloseable {

  private static final Path DEFAULT_CACHE_DIR =
      Path.of(System.getProperty("user.home"), ".cache", "town-names-openttd");

  private final GrfCache cache;
  private final BananasHttpClient http;
  private final Path cacheDirectory;

  private BananasClient(Path cacheDirectory, String baseUrl) {
    this.cacheDirectory = cacheDirectory;
    this.cache = new GrfCache(cacheDirectory);
    this.http = new BananasHttpClient(baseUrl);
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /** The resolved cache directory path. */
  public Path cacheDirectory() {
    return cacheDirectory;
  }

  /**
   * Search for NewGRF town-name packages matching the query. An empty query returns all available
   * town-name GRFs.
   *
   * @throws IOException on network or parse error
   * @throws InterruptedException if the thread is interrupted while waiting
   */
  public List<BananasEntry> search(String query) throws IOException, InterruptedException {
    return http.search(query);
  }

  /**
   * Download a GRF file. Returns immediately from the local cache if already present; otherwise
   * fetches from the Bananas server and caches the result.
   *
   * @throws IOException on network or I/O error
   * @throws InterruptedException if interrupted while downloading
   */
  public GrfDownloadResult download(BananasEntry entry) throws IOException, InterruptedException {
    Optional<Path> cached = cache.lookup(entry);
    if (cached.isPresent()) {
      return new GrfDownloadResult(cached.get());
    }
    byte[] bytes = http.download(entry);
    Path stored = cache.store(entry, bytes);
    return new GrfDownloadResult(stored);
  }

  @Override
  public void close() {
    http.close();
  }

  /** Builder for {@link BananasClient}. */
  public static final class Builder {

    private Path cacheDir = DEFAULT_CACHE_DIR;
    private String baseUrl = "https://bananas-api.openttd.org";

    private Builder() {}

    /** Set a custom cache directory (pass an absolute path). */
    public Builder cacheDir(Path cacheDir) {
      this.cacheDir = Objects.requireNonNull(cacheDir, "cacheDir must not be null");
      return this;
    }

    /** Override the Bananas API base URL (used in tests). */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
      return this;
    }

    public BananasClient build() {
      return new BananasClient(cacheDir, baseUrl);
    }
  }
}

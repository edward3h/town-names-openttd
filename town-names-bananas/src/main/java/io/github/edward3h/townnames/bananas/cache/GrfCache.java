package io.github.edward3h.townnames.bananas.cache;

import io.github.edward3h.townnames.bananas.BananasEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-system cache for downloaded GRF files.
 *
 * <p>Files are keyed by {@code {grfid}_{md5sum}_{version}.grf}.
 * On construction, the cache directory is scanned and any existing files are indexed.
 * {@link #store} writes atomically (write to temp, then move).
 * The cache is grow-only: existing entries are never removed or overwritten.
 */
public final class GrfCache {

  private final Path cacheDirectory;
  private final Map<String, Path> index;

  public GrfCache(Path cacheDirectory) {
    try {
      Files.createDirectories(cacheDirectory);
    } catch (IOException e) {
      throw new UncheckedIOException("Cannot create cache directory: " + cacheDirectory, e);
    }
    this.cacheDirectory = cacheDirectory;
    this.index = new ConcurrentHashMap<>(scan(cacheDirectory));
  }

  /** Returns the cached path for this entry, if present. */
  public Optional<Path> lookup(BananasEntry entry) {
    return Optional.ofNullable(index.get(cacheKey(entry)));
  }

  /**
   * Store bytes for an entry in the cache, atomically.
   *
   * @return the path where the file was stored
   */
  public Path store(BananasEntry entry, byte[] content) throws IOException {
    String key = cacheKey(entry);
    Path target = cacheDirectory.resolve(key);
    Path temp = cacheDirectory.resolve(key + ".tmp");
    Files.write(temp, content);
    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    index.put(key, target);
    return target;
  }

  private static String cacheKey(BananasEntry entry) {
    return entry.contentId().grfid()
        + "_" + entry.contentId().md5sum()
        + "_" + entry.version()
        + ".grf";
  }

  private static Map<String, Path> scan(Path dir) {
    var result = new ConcurrentHashMap<String, Path>();
    try (var stream = Files.list(dir)) {
      stream
          .filter(p -> p.getFileName().toString().endsWith(".grf"))
          .forEach(p -> result.put(p.getFileName().toString(), p));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to scan cache directory: " + dir, e);
    }
    return result;
  }
}

package io.github.edward3h.townnames.bananas.cache;

import static org.junit.jupiter.api.Assertions.*;

import io.github.edward3h.townnames.bananas.BananasEntry;
import io.github.edward3h.townnames.bananas.ContentId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GrfCacheTest {

    private static final ContentId ID = new ContentId("ABCD1234", "abc123def456");
    private static final BananasEntry ENTRY =
        new BananasEntry(ID, "1.0.0", "Test GRF", null, "GPL v2", "Author");

    @Test
    void missCacheLookupReturnsEmpty(@TempDir Path cacheDir) {
        var cache = new GrfCache(cacheDir);
        assertTrue(cache.lookup(ENTRY).isEmpty());
    }

    @Test
    void hitAfterStore(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        byte[] content = {0x01, 0x02, 0x03};
        Path stored = cache.store(ENTRY, content);
        var hit = cache.lookup(ENTRY);
        assertTrue(hit.isPresent());
        assertEquals(stored, hit.get());
        assertArrayEquals(content, Files.readAllBytes(stored));
    }

    @Test
    void cacheKeyIsContentIdAndVersion(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        var otherEntry = new BananasEntry(
            new ContentId("ABCD1234", "abc123def456"), "2.0.0",
            "Same GRF, different version", null, "GPL v2", "Author");
        cache.store(ENTRY, new byte[]{0x01});
        // Different version = different cache key
        assertTrue(cache.lookup(otherEntry).isEmpty());
    }

    @Test
    void cacheFilenameIsCorrect(@TempDir Path cacheDir) throws IOException {
        var cache = new GrfCache(cacheDir);
        Path stored = cache.store(ENTRY, new byte[]{0x42});
        assertEquals("ABCD1234_abc123def456_1.0.0.grf", stored.getFileName().toString());
    }

    @Test
    void storeIsAtomic(@TempDir Path cacheDir) throws IOException {
        // Verify no .tmp file remains after store
        var cache = new GrfCache(cacheDir);
        cache.store(ENTRY, new byte[]{0x01});
        try (var stream = Files.list(cacheDir)) {
            assertTrue(stream.noneMatch(p -> p.toString().endsWith(".tmp")));
        }
    }

    @Test
    void populatesFromExistingDirectoryOnConstruction(@TempDir Path cacheDir) throws IOException {
        // Pre-populate the directory as if a previous run had downloaded a file
        String filename = "ABCD1234_abc123def456_1.0.0.grf";
        Files.write(cacheDir.resolve(filename), new byte[]{0x42});
        var cache = new GrfCache(cacheDir);
        assertTrue(cache.lookup(ENTRY).isPresent());
    }
}

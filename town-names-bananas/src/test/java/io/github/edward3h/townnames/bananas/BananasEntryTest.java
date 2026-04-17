package io.github.edward3h.townnames.bananas;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BananasEntryTest {

    @Test
    void contentIdEquality() {
        var a = new ContentId("ABCD1234", "abc123");
        var b = new ContentId("ABCD1234", "abc123");
        assertEquals(a, b);
    }

    @Test
    void bananasEntryHoldsMetadata() {
        var id = new ContentId("ABCD", "def0");
        var entry = new BananasEntry(id, "1.0", "UK Towns", null, "GPL v2", "Author");
        assertEquals("UK Towns", entry.name());
        assertNull(entry.description());
    }

    @Test
    void grfDownloadResultHoldsPath() {
        var result = new GrfDownloadResult(java.nio.file.Path.of("/tmp/test.grf"));
        assertEquals(java.nio.file.Path.of("/tmp/test.grf"), result.localPath());
    }
}

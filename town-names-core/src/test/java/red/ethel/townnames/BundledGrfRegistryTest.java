package red.ethel.townnames;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import red.ethel.townnames.registry.BundledGrfRegistry;

class BundledGrfRegistryTest {

    @Test
    void registryListsBundledGrfs() {
        var names = BundledGrfRegistry.getInstance().availableNames();
        assertTrue(names.contains("dystopian-town-names"), "Expected dystopian-town-names: " + names);
        assertTrue(names.contains("massachusetts-town-names"), "Expected massachusetts-town-names: " + names);
    }

    @Test
    void registryIsASingleton() {
        assertSame(BundledGrfRegistry.getInstance(), BundledGrfRegistry.getInstance());
    }

    @Test
    void openReturnsStreamForBundledGrf() throws Exception {
        try (var stream = BundledGrfRegistry.getInstance().open("dystopian-town-names")) {
            assertNotNull(stream);
            assertTrue(stream.available() > 0);
        }
    }
}

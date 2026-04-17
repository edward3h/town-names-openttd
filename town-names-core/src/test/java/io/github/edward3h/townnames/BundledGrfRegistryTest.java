package io.github.edward3h.townnames;

import static org.junit.jupiter.api.Assertions.*;

import io.github.edward3h.townnames.registry.BundledGrfRegistry;
import org.junit.jupiter.api.Test;

class BundledGrfRegistryTest {

  @Test
  void registryReportsAvailableNames() {
    var registry = BundledGrfRegistry.getInstance();
    // When no GRFs are bundled yet (empty bundledGrfs list), the list is empty but non-null
    assertNotNull(registry.availableNames());
  }

  @Test
  void registryIsASingleton() {
    assertSame(BundledGrfRegistry.getInstance(), BundledGrfRegistry.getInstance());
  }
}

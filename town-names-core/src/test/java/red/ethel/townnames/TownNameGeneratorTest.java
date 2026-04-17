package red.ethel.townnames;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TownNameGeneratorTest {

    @Test
    void builderWithNoBundledOrFileSourcesFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TownNameGenerator.builder().build());
    }

    @Test
    void builderWithUnknownBundledNameFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TownNameGenerator.builder().withBundled("no-such-grf").build());
    }

    @Test
    void generatesNonEmptyNameFromFile() throws Exception {
        // Use the test resource GRF bundled in the test classpath
        Path testGrf = Path.of(
                TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var result = TownNameGenerator.builder().withGrf(testGrf).build();
        assertTrue(result.skippedSources().isEmpty());
        String name = result.generator().generate();
        assertFalse(name.isBlank());
    }

    @Test
    void seededGeneratorIsDeterministic() throws Exception {
        Path testGrf = Path.of(
                TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var g1 = TownNameGenerator.builder()
                .withGrf(testGrf)
                .withSeed(42L)
                .build()
                .generator();
        var g2 = TownNameGenerator.builder()
                .withGrf(testGrf)
                .withSeed(42L)
                .build()
                .generator();
        assertEquals(g1.generate(), g2.generate());
    }

    @Test
    void badFileSourceIsSkipped() throws Exception {
        Path good = Path.of(
                TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        Path bad = Path.of("/nonexistent/path.grf");
        var result = TownNameGenerator.builder().withGrf(good).withGrf(bad).build();
        assertEquals(List.of(bad), result.skippedSources());
        assertFalse(result.generator().generate().isBlank());
    }

    @Test
    void allBadSourcesThrows() {
        Path bad = Path.of("/nonexistent/path.grf");
        assertThrows(
                IllegalArgumentException.class,
                () -> TownNameGenerator.builder().withGrf(bad).build());
    }

    @Test
    void generateBulkEquivalentToSequential() throws Exception {
        Path testGrf = Path.of(
                TownNameGeneratorTest.class.getResource("/test-names.grf").toURI());
        var g = TownNameGenerator.builder()
                .withGrf(testGrf)
                .withSeed(7L)
                .build()
                .generator();
        var h = TownNameGenerator.builder()
                .withGrf(testGrf)
                .withSeed(7L)
                .build()
                .generator();
        assertEquals(g.generate(5), List.of(h.generate(), h.generate(), h.generate(), h.generate(), h.generate()));
    }
}

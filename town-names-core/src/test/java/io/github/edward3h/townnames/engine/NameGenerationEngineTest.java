package io.github.edward3h.townnames.engine;

import static org.junit.jupiter.api.Assertions.*;
import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.NameEntry;
import io.github.edward3h.townnames.grf.NamePart;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class NameGenerationEngineTest {

    private static GrfData twoPartGrf() {
        // Part 0: "North" (50) | "South" (50)
        // Part 1: "field" (100)
        var part0 = new NamePart(List.of(
            new NameEntry.Text(50, "North"),
            new NameEntry.Text(50, "South")));
        var part1 = new NamePart(List.of(
            new NameEntry.Text(100, "field")));
        return new GrfData((byte) 0, List.of(part0, part1));
    }

    @Test
    void generatesConcatenatedName() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        // With a fixed seed the output should be deterministic
        String name = engine.generate(new Random(42L));
        assertFalse(name.isEmpty());
        assertTrue(name.equals("Northfield") || name.equals("Southfield"),
            "Unexpected name: " + name);
    }

    @Test
    void deterministicWithSameSeed() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        String a = engine.generate(new Random(99L));
        String b = engine.generate(new Random(99L));
        assertEquals(a, b);
    }

    @Test
    void bulkGenerateEquivalentToSequential() {
        var engine = new NameGenerationEngine(List.of(twoPartGrf()));
        var rng1 = new Random(7L);
        var rng2 = new Random(7L);
        List<String> bulk = engine.generate(rng1, 5);
        List<String> seq = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) seq.add(engine.generate(rng2));
        assertEquals(seq, bulk);
    }

    @Test
    void partReferenceResolvesRecursively() {
        // Part 0: PartRef(100, 1) — delegates to Part 1, contributing "bury"
        // Part 1: "bury" (100)    — also processed directly as a top-level part, contributing "bury"
        // All parts are concatenated, so result = "bury" + "bury" = "burybury"
        // This verifies PartRef is resolved (if broken, Part 0 would contribute "" → result would be "bury")
        var part0 = new NamePart(List.of(new NameEntry.PartRef(100, 1)));
        var part1 = new NamePart(List.of(new NameEntry.Text(100, "bury")));
        var data = new GrfData((byte) 0, List.of(part0, part1));
        var engine = new NameGenerationEngine(List.of(data));
        assertEquals("burybury", engine.generate(new Random(0L)));
    }

    @Test
    void multipleSourcesEachGenerateFromOwnParts() {
        // Source A: part0="Alpha" | part1="ville" — only valid combination: "Alphaville"
        var sourceA = new GrfData((byte) 0, List.of(
            new NamePart(List.of(new NameEntry.Text(100, "Alpha"))),
            new NamePart(List.of(new NameEntry.Text(100, "ville")))));
        // Source B: part0="Beta" | part1="burg" — only valid combination: "Betaburg"
        var sourceB = new GrfData((byte) 0, List.of(
            new NamePart(List.of(new NameEntry.Text(100, "Beta"))),
            new NamePart(List.of(new NameEntry.Text(100, "burg")))));

        var engine = new NameGenerationEngine(List.of(sourceA, sourceB));

        // Over many iterations each result must be entirely from one source — never cross-mixed.
        // Use a single seeded Random so the stream of calls exercises both source indices.
        java.util.Set<String> generated = new java.util.HashSet<>();
        var rng = new Random(12345L);
        for (int i = 0; i < 100; i++) {
            generated.add(engine.generate(rng));
        }
        assertTrue(generated.stream().allMatch(n -> n.equals("Alphaville") || n.equals("Betaburg")),
            "Cross-source names generated: " + generated);
        assertTrue(generated.contains("Alphaville"), "Source A never selected");
        assertTrue(generated.contains("Betaburg"),   "Source B never selected");
    }
}

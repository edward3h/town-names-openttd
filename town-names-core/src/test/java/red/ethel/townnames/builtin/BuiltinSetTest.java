package red.ethel.townnames.builtin;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import red.ethel.townnames.BuildResult;
import red.ethel.townnames.TownNameGenerator;

class BuiltinSetTest {

    @ParameterizedTest
    @EnumSource(BuiltinSet.class)
    void eachSetGeneratesNonEmptyName(BuiltinSet set) {
        String name = set.generate(new Random(42L));
        assertNotNull(name);
        assertFalse(name.isBlank(), "Expected non-blank name from " + set + ", got: '" + name + "'");
    }

    @ParameterizedTest
    @EnumSource(BuiltinSet.class)
    void deterministicWithSameSeed(BuiltinSet set) {
        String a = set.generate(new Random(99L));
        String b = set.generate(new Random(99L));
        assertEquals(a, b, "Expected same output for same seed from " + set);
    }

    @Test
    void builtinNamesListsAllSets() {
        var names = TownNameGenerator.builtinNames();
        assertEquals(BuiltinSet.values().length, names.size());
        for (BuiltinSet set : BuiltinSet.values()) {
            assertTrue(names.contains(set.name()), "Missing: " + set.name());
        }
    }

    @Test
    void withBuiltinIntegratesWithTownNameGenerator() {
        BuildResult result = TownNameGenerator.builder()
                .withBuiltin(BuiltinSet.ENGLISH_ORIGINAL)
                .withSeed(1L)
                .build();
        assertTrue(result.skippedSources().isEmpty());
        String name = result.generator().generate();
        assertNotNull(name);
        assertFalse(name.isBlank());
    }

    @Test
    void withBuiltinAndGrfCombined() {
        // GERMAN + FRENCH both active — generator must still produce non-blank output
        BuildResult result = TownNameGenerator.builder()
                .withBuiltin(BuiltinSet.GERMAN)
                .withBuiltin(BuiltinSet.FRENCH)
                .withSeed(42L)
                .build();
        var names = result.generator().generate(20);
        assertTrue(names.stream().noneMatch(String::isBlank));
    }
}

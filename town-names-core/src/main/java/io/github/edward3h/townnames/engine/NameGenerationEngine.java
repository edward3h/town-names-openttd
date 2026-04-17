package io.github.edward3h.townnames.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates town names by randomly selecting one {@link NameSource} and delegating to it. Sources
 * may be GRF-based ({@link GrfNameSource}) or built-in generators.
 */
public final class NameGenerationEngine {

    private final List<NameSource> sources;

    public NameGenerationEngine(List<NameSource> sources) {
        if (sources.isEmpty()) throw new IllegalArgumentException("At least one source is required");
        this.sources = List.copyOf(sources);
    }

    /** Generate a single name using the provided RNG. */
    public String generate(Random rng) {
        return sources.get(rng.nextInt(sources.size())).generate(rng);
    }

    /** Generate {@code count} names. */
    public List<String> generate(Random rng, int count) {
        var names = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) names.add(generate(rng));
        return List.copyOf(names);
    }
}

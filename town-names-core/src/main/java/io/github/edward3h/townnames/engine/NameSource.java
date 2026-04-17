package io.github.edward3h.townnames.engine;

import java.util.Random;

/** A source that can generate a single town name. */
public interface NameSource {
    String generate(Random rng);
}

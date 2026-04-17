package io.github.edward3h.townnames;

import io.github.edward3h.townnames.builtin.BuiltinSet;
import io.github.edward3h.townnames.engine.GrfNameSource;
import io.github.edward3h.townnames.engine.NameGenerationEngine;
import io.github.edward3h.townnames.engine.NameSource;
import io.github.edward3h.townnames.grf.GrfParser;
import io.github.edward3h.townnames.registry.BundledGrfRegistry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Generates random town names from one or more sources (built-in generators, bundled GRFs, or
 * custom GRF files).
 *
 * <p>Use {@link #builder()} to configure sources and an optional seed, then call {@link
 * Builder#build()} to get a {@link BuildResult} containing the generator and any sources that could
 * not be loaded.
 */
public final class TownNameGenerator {

  private final NameGenerationEngine engine;
  private final Random rng;

  private TownNameGenerator(NameGenerationEngine engine, Optional<Long> seed) {
    this.engine = engine;
    // Single Random instance created at construction time so sequential generate() calls
    // advance through the sequence rather than restarting from the same seed each time.
    this.rng = seed.map(Random::new).orElseGet(Random::new);
  }

  /** Returns a new builder. */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns the names of all built-in generator sets. */
  public static List<String> builtinNames() {
    return Arrays.stream(BuiltinSet.values()).map(Enum::name).toList();
  }

  /** Generate a single random town name. */
  public String generate() {
    return engine.generate(rng);
  }

  /**
   * Generate {@code count} random town names. Equivalent to calling {@link #generate()} {@code
   * count} times.
   *
   * @throws IllegalArgumentException if count &lt;= 0
   */
  public List<String> generate(int count) {
    if (count <= 0) throw new IllegalArgumentException("count must be > 0, got: " + count);
    return engine.generate(rng, count);
  }

  /** Builder for {@link TownNameGenerator}. */
  public static final class Builder {

    private final List<BuiltinSet> builtinSets = new ArrayList<>();
    private final List<String> bundledNames = new ArrayList<>();
    private final List<Path> filePaths = new ArrayList<>();
    private Optional<Long> seed = Optional.empty();

    private Builder() {}

    /** Add a built-in generator set (e.g. {@code BuiltinSet.ENGLISH_ORIGINAL}). */
    public Builder withBuiltin(BuiltinSet set) {
      builtinSets.add(Objects.requireNonNull(set, "set must not be null"));
      return this;
    }

    /** Add a bundled GRF by name (e.g. {@code "uk-towns"}). */
    public Builder withBundled(String name) {
      bundledNames.add(name);
      return this;
    }

    /**
     * Add a GRF file from the file system. Unreadable files are skipped (see {@link BuildResult}).
     */
    public Builder withGrf(Path path) {
      filePaths.add(path);
      return this;
    }

    /** Fix the random seed for deterministic output. */
    public Builder withSeed(long seed) {
      this.seed = Optional.of(seed);
      return this;
    }

    /** Build the generator. */
    public BuildResult build() {
      var registry = BundledGrfRegistry.getInstance();

      // Validate bundled names eagerly — unknown name is always a developer error
      for (String name : bundledNames) {
        if (!registry.availableNames().contains(name)) {
          throw new IllegalArgumentException(
              "Unknown bundled GRF name '" + name + "'. Available: " + registry.availableNames());
        }
      }

      if (builtinSets.isEmpty() && bundledNames.isEmpty() && filePaths.isEmpty()) {
        throw new IllegalArgumentException("At least one source must be specified");
      }

      var allSources = new ArrayList<NameSource>();
      var skipped = new ArrayList<Path>();

      // Add built-in generators
      allSources.addAll(builtinSets);

      // Load bundled GRFs
      for (String name : bundledNames) {
        try (var stream = registry.open(name)) {
          GrfParser.parse(stream).stream().map(GrfNameSource::new).forEach(allSources::add);
        } catch (IOException e) {
          throw new IllegalStateException("Failed to load bundled GRF '" + name + "'", e);
        }
      }

      // Load file GRFs; skip unreadable ones
      for (Path path : filePaths) {
        try {
          var sources = GrfParser.parse(path).stream().map(GrfNameSource::new).toList();
          if (sources.isEmpty()) {
            skipped.add(path);
          } else {
            allSources.addAll(sources);
          }
        } catch (IOException e) {
          skipped.add(path);
        }
      }

      if (allSources.isEmpty()) {
        throw new IllegalArgumentException(
            "No valid GRF data found. All file sources were invalid: " + filePaths);
      }

      var engine = new NameGenerationEngine(allSources);
      return new BuildResult(new TownNameGenerator(engine, seed), List.copyOf(skipped));
    }
  }
}

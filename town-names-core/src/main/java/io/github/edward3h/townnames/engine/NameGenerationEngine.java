package io.github.edward3h.townnames.engine;

import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.NameEntry;
import io.github.edward3h.townnames.grf.NamePart;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates town names by randomly selecting one GRF source and applying weighted random selection
 * across its parts. PartRef indices are local to the selected source.
 */
public final class NameGenerationEngine {

  private final List<GrfData> sources;

  public NameGenerationEngine(List<GrfData> sources) {
    if (sources.isEmpty())
      throw new IllegalArgumentException("At least one GRF source is required");
    this.sources = List.copyOf(sources);
  }

  /** Generate a single name using the provided RNG. */
  public String generate(Random rng) {
    // Pick one source at random; PartRef indices are local to that source
    GrfData source = sources.get(rng.nextInt(sources.size()));
    List<NamePart> parts = source.parts();
    if (parts.isEmpty()) return "";

    var sb = new StringBuilder();
    for (NamePart part : parts) {
      sb.append(selectFromPart(part, parts, rng));
    }
    return sb.toString();
  }

  /** Generate {@code count} names. Equivalent to calling {@link #generate(Random)} count times. */
  public List<String> generate(Random rng, int count) {
    var names = new ArrayList<String>(count);
    for (int i = 0; i < count; i++) names.add(generate(rng));
    return List.copyOf(names);
  }

  private String selectFromPart(NamePart part, List<NamePart> localParts, Random rng) {
    int total = part.entries().stream().mapToInt(NameEntry::probability).sum();
    if (total == 0) return "";

    int pick = rng.nextInt(total);
    int cumulative = 0;
    for (NameEntry entry : part.entries()) {
      cumulative += entry.probability();
      if (pick < cumulative) {
        return switch (entry) {
          case NameEntry.Text t -> t.text();
          case NameEntry.PartRef r -> {
            if (r.partIndex() < localParts.size()) {
              yield selectFromPart(localParts.get(r.partIndex()), localParts, rng);
            }
            yield "";
          }
        };
      }
    }
    return "";
  }
}

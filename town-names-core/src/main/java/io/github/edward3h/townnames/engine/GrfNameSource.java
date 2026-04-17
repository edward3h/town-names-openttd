package io.github.edward3h.townnames.engine;

import io.github.edward3h.townnames.grf.GrfData;
import io.github.edward3h.townnames.grf.NameEntry;
import io.github.edward3h.townnames.grf.NamePart;
import java.util.List;
import java.util.Random;

/** Wraps a single {@link GrfData} and generates names via weighted random selection. */
public final class GrfNameSource implements NameSource {

  private final GrfData data;

  public GrfNameSource(GrfData data) {
    this.data = data;
  }

  @Override
  public String generate(Random rng) {
    List<NamePart> parts = data.parts();
    if (parts.isEmpty()) return "";
    var sb = new StringBuilder();
    for (NamePart part : parts) {
      sb.append(selectFromPart(part, parts, rng));
    }
    return sb.toString();
  }

  private static String selectFromPart(NamePart part, List<NamePart> localParts, Random rng) {
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

package io.github.edward3h.townnames.grf;

import java.util.List;

/**
 * Parsed data from a single Action 0F sprite: one language/style id and its associated parts. A
 * .grf file may yield multiple GrfData instances (one per Action 0F id found).
 */
public record GrfData(byte id, List<NamePart> parts) {}

package red.ethel.townnames.grf;

import java.util.List;

/**
 * One ordered group of weighted entries. To produce a token, select one entry proportionally by
 * probability weight.
 */
public record NamePart(List<NameEntry> entries) {}

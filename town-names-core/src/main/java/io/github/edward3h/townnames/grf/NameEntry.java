package io.github.edward3h.townnames.grf;

/** A single entry within a NamePart: either a text string or a reference to another part. */
public sealed interface NameEntry permits NameEntry.Text, NameEntry.PartRef {

    /** Probability weight for this entry (1–127 for text; bit 7 set means PartRef). */
    int probability();

    /** A literal text string to append when this entry is selected. */
    record Text(int probability, String text) implements NameEntry {}

    /** A reference to another part (by index) to recurse into. */
    record PartRef(int probability, int partIndex) implements NameEntry {}
}

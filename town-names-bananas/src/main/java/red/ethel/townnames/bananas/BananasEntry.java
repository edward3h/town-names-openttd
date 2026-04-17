package red.ethel.townnames.bananas;

/**
 * Metadata for a single NewGRF file available on the Bananas content server.
 *
 * <p>Note: the field is named {@code licence} (British English); the Bananas REST API uses {@code
 * license} — the HTTP client maps the API field to this name.
 *
 * @param contentId unique content identifier
 * @param version version string (e.g. "1.0.0")
 * @param name human-readable display name
 * @param description optional description
 * @param licence licence under which the GRF is distributed
 * @param author original author name
 */
public record BananasEntry(
        ContentId contentId, String version, String name, String description, String licence, String author) {}

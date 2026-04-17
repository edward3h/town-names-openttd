package red.ethel.townnames.bananas;

import java.nio.file.Path;

/**
 * The result of a successful {@link BananasClient#download} call.
 *
 * @param localPath the absolute path on disk to the downloaded (or cached) GRF file
 */
public record GrfDownloadResult(Path localPath) {}

package io.github.edward3h.townnames;

import java.nio.file.Path;
import java.util.List;

/**
 * The result of building a {@link TownNameGenerator}.
 *
 * @param generator the configured generator, ready to use
 * @param skippedSources file paths that were unreadable or contained no valid Action 0F data; empty
 *     when all sources loaded successfully
 */
public record BuildResult(TownNameGenerator generator, List<Path> skippedSources) {}

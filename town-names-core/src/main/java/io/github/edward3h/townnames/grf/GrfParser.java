package io.github.edward3h.townnames.grf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses NewGRF container v1 binary data and extracts all Action 0F (town name generator)
 * pseudo-sprites.
 *
 * <p>Reference: https://newgrf-specs.tt-wiki.net/wiki/Action0F
 */
public final class GrfParser {

  private GrfParser() {}

  /** Parse all Action 0F entries from a .grf file on disk. */
  public static List<GrfData> parse(Path path) throws IOException {
    try (var in = Files.newInputStream(path)) {
      return parse(in);
    }
  }

  /** Parse all Action 0F entries from a raw stream of NewGRF container v1 sprites. */
  public static List<GrfData> parse(InputStream in) throws IOException {
    var results = new ArrayList<GrfData>();
    byte[] lengthBuf = new byte[2];

    while (true) {
      int read = in.readNBytes(lengthBuf, 0, 2);
      if (read < 2) break; // EOF

      int spriteLen = (lengthBuf[0] & 0xFF) | ((lengthBuf[1] & 0xFF) << 8);
      if (spriteLen == 0) break; // terminator

      int info = in.read();
      if (info == -1) break;

      byte[] body = in.readNBytes(spriteLen - 1); // -1 for the info byte already read

      if (info != 0xFF) continue; // not a pseudo-sprite — skip
      if (body.length == 0 || body[0] != 0x0F) continue; // not Action 0F — skip

      GrfData data = parseAction0F(body);
      if (data != null) results.add(data);
    }

    return results;
  }

  private static GrfData parseAction0F(byte[] body) {
    if (body.length < 3) return null; // action(1) + id(1) + num-parts(1)

    byte id = body[1];
    int numParts = body[2] & 0xFF;
    int pos = 3;
    var parts = new ArrayList<NamePart>();

    for (int p = 0; p < numParts && pos < body.length; p++) {
      int count = body[pos++] & 0xFF;
      var entries = new ArrayList<NameEntry>();

      for (int e = 0; e < count && pos < body.length; e++) {
        int probByte = body[pos++] & 0xFF;
        boolean isRef = (probByte & 0x80) != 0;
        int prob = probByte & 0x7F;

        if (isRef) {
          if (pos >= body.length) break;
          int partIndex = body[pos++] & 0xFF;
          entries.add(new NameEntry.PartRef(prob, partIndex));
        } else {
          // read null-terminated UTF-8 string
          int start = pos;
          while (pos < body.length && body[pos] != 0x00) pos++;
          String text = new String(body, start, pos - start, StandardCharsets.UTF_8);
          if (pos < body.length) pos++; // consume NUL
          entries.add(new NameEntry.Text(prob, text));
        }
      }

      parts.add(new NamePart(List.copyOf(entries)));
    }

    return new GrfData(id, List.copyOf(parts));
  }
}

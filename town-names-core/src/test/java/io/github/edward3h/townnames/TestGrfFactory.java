package io.github.edward3h.townnames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestGrfFactory {

    private TestGrfFactory() {}

    /** Creates a minimal valid GRF file at the given path. */
    public static Path writeMinimalGrf(Path path) throws IOException {
        byte[] bytes = buildGrfBytes();
        Files.write(path, bytes);
        return path;
    }

    public static byte[] buildGrfBytes() {
        byte[] text1 = "North".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] text2 = "South".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // body = action(1) + id(1) + num-parts(1) + count(1) + prob(1) + text1 + NUL + prob(1) + text2
        // + NUL
        int bodyLen = 1 + 1 + 1 + 1 + 1 + text1.length + 1 + 1 + text2.length + 1;
        // spriteLen includes the info byte (Container v1 format)
        int spriteLen = bodyLen + 1;
        byte[] sprite = new byte[2 + 1 + bodyLen];
        sprite[0] = (byte) (spriteLen & 0xFF); // length low (includes info byte)
        sprite[1] = (byte) ((spriteLen >> 8) & 0xFF); // length high
        sprite[2] = (byte) 0xFF;
        int i = 3;
        sprite[i++] = 0x0F;
        sprite[i++] = 0x00;
        sprite[i++] = 0x01;
        sprite[i++] = 0x02;
        sprite[i++] = 50;
        for (byte b : text1) sprite[i++] = b;
        sprite[i++] = 0x00;
        sprite[i++] = 50;
        for (byte b : text2) sprite[i++] = b;
        sprite[i] = 0x00;
        return sprite;
    }
}

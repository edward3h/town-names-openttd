package io.github.edward3h.townnames.grf;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class GrfParserTest {

    @Test
    void grfDataHoldsPartsIndexedById() {
        var text = new NameEntry.Text(10, "Oakfield");
        var part = new NamePart(List.of(text));
        var data = new GrfData((byte) 0x00, List.of(part));

        assertEquals((byte) 0x00, data.id());
        assertEquals(1, data.parts().size());
        assertEquals("Oakfield", ((NameEntry.Text) data.parts().get(0).entries().get(0)).text());
    }

    // Container v1 length field includes the info byte.
    // Minimal valid NewGRF pseudo-sprite with one Action 0F:
    //   id=0x00, 1 part, 2 entries: "North" (prob=50), "South" (prob=50)
    private static byte[] minimalGrf() {
        byte[] text1 = "North".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] text2 = "South".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // body = action(1) + id(1) + num-parts(1) + count(1) + prob(1) + text1 + NUL + prob(1) + text2 + NUL
        int bodyLen = 1 + 1 + 1 + 1 + 1 + text1.length + 1 + 1 + text2.length + 1;
        // spriteLen = bodyLen + 1 because Container v1 length includes the info byte
        int spriteLen = bodyLen + 1;
        byte[] sprite = new byte[2 + 1 + bodyLen];
        sprite[0] = (byte) (spriteLen & 0xFF);       // length low (includes info byte)
        sprite[1] = (byte) ((spriteLen >> 8) & 0xFF);// length high
        sprite[2] = (byte) 0xFF;                     // info: pseudo-sprite
        int i = 3;
        sprite[i++] = 0x0F;   // action
        sprite[i++] = 0x00;   // id
        sprite[i++] = 0x01;   // num-parts
        sprite[i++] = 0x02;   // count
        sprite[i++] = 50;     // prob entry 1
        for (byte b : text1) sprite[i++] = b;
        sprite[i++] = 0x00;   // NUL
        sprite[i++] = 50;     // prob entry 2
        for (byte b : text2) sprite[i++] = b;
        sprite[i] = 0x00;     // NUL
        return sprite;
    }

    @Test
    void parsesAction0FFromInputStream() throws IOException {
        var results = GrfParser.parse(new ByteArrayInputStream(minimalGrf()));

        assertEquals(1, results.size());
        var data = results.get(0);
        assertEquals((byte) 0x00, data.id());
        assertEquals(1, data.parts().size());
        var entries = data.parts().get(0).entries();
        assertEquals(2, entries.size());
        assertEquals("North", ((NameEntry.Text) entries.get(0)).text());
        assertEquals("South", ((NameEntry.Text) entries.get(1)).text());
    }

    @Test
    void skipsNonPseudoSprites() throws IOException {
        // A sprite with info != 0xFF should be skipped silently
        byte[] sprite = { 0x01, 0x00, 0x01, 0x00 }; // length=1, info=0x01, body=0x00
        var results = GrfParser.parse(new ByteArrayInputStream(sprite));
        assertTrue(results.isEmpty());
    }

    @Test
    void skipsNonAction0FPseudoSprites() throws IOException {
        // info=0xFF but action=0x08 (not 0x0F) — should be skipped
        // spriteLen=2: info byte + 1 body byte (the action byte 0x08)
        byte[] sprite = { 0x02, 0x00, (byte) 0xFF, 0x08 };
        var results = GrfParser.parse(new ByteArrayInputStream(sprite));
        assertTrue(results.isEmpty());
    }
}

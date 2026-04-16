package io.github.edward3h.townnames.grf;

import static org.junit.jupiter.api.Assertions.*;
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
}

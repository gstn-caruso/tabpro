package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import org.junit.jupiter.api.Test;

class TrillPanelTest {

    @Test
    void startsWithTheGivenTrill() {
        Trill trill = new Trill(7, NoteValue.SIXTEENTH);

        TrillPanel panel = new TrillPanel(trill);

        assertEquals(trill, panel.toTrill());
    }
}

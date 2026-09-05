package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import org.junit.jupiter.api.Test;

class TremoloPickingPanelTest {

    @Test
    void startsWithTheGivenSpeed() {
        TremoloPicking picking = TremoloPicking.at(NoteValue.THIRTY_SECOND);

        TremoloPickingPanel panel = new TremoloPickingPanel(picking);

        assertEquals(picking, panel.toTremoloPicking());
    }
}

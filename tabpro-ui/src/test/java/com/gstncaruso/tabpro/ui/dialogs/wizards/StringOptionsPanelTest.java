package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StringOptionsPanelTest {

    @Test
    void startsWithEveryStringSelected() {
        StringOptionsPanel panel = new StringOptionsPanel(6, 4);

        assertEquals(Set.of(1, 2, 3, 4, 5, 6), panel.selectedStrings());
    }

    @Test
    void unselectingAStringDropsItFromTheSet() {
        StringOptionsPanel panel = new StringOptionsPanel(6, 4);

        panel.setStringSelected(3, false);

        assertEquals(Set.of(1, 2, 4, 5, 6), panel.selectedStrings());
    }

    @Test
    void startsWithNoChangesRequested() {
        StringOptionsPanel panel = new StringOptionsPanel(6, 4);

        assertTrue(panel.letRingChange().isEmpty());
        assertTrue(panel.palmMuteChange().isEmpty());
        assertTrue(panel.dynamicChange().isEmpty());
    }
}

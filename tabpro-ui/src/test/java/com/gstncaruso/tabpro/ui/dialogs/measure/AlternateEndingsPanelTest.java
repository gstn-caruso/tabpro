package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class AlternateEndingsPanelTest {

    @Test
    void startsWithNoneCheckedWhenThereAreNoEndings() {
        AlternateEndingsPanel panel = new AlternateEndingsPanel(List.of());

        assertEquals(List.of(), panel.toAlternateEndings());
    }

    @Test
    void startsWithTheGivenPassesChecked() {
        AlternateEndingsPanel panel = new AlternateEndingsPanel(List.of(1, 3));

        assertEquals(List.of(1, 3), panel.toAlternateEndings());
    }

    @Test
    void checkingABoxAddsItsPass() {
        AlternateEndingsPanel panel = new AlternateEndingsPanel(List.of());

        panel.setChecked(5, true);

        assertEquals(List.of(5), panel.toAlternateEndings());
    }

    @Test
    void uncheckingABoxRemovesItsPass() {
        AlternateEndingsPanel panel = new AlternateEndingsPanel(List.of(2, 4));

        panel.setChecked(2, false);

        assertEquals(List.of(4), panel.toAlternateEndings());
    }
}

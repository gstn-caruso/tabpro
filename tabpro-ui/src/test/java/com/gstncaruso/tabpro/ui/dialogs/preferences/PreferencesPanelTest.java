package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;

class PreferencesPanelTest {

    @Test
    void startsWithTheGivenPreferences() {
        Preferences preferences = new Preferences(NoteValue.EIGHTH, true, false);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(preferences, panel.toPreferences());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        panel.apply(new Preferences(NoteValue.SIXTEENTH, true, true));

        assertEquals(new Preferences(NoteValue.SIXTEENTH, true, true), panel.toPreferences());
    }
}

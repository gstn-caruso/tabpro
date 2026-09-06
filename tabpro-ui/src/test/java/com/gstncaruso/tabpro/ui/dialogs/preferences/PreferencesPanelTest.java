package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;

class PreferencesPanelTest {

    @Test
    void startsWithTheGivenPreferences() {
        Preferences preferences = new Preferences(NoteValue.EIGHTH, true, false, false);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(preferences, panel.toPreferences());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        panel.apply(new Preferences(NoteValue.SIXTEENTH, true, true, false));

        assertEquals(new Preferences(NoteValue.SIXTEENTH, true, true, false), panel.toPreferences());
    }

    @Test
    void showsTheBassInChordNamePreferenceByDefault() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        assertEquals(Preferences.defaults(), panel.toPreferences());
        assertTrue(Preferences.defaults().showBassInChordName());
    }

    @Test
    void offersToDisableUndoAndToConfigureTheAutosaveInterval() {
        Preferences preferences = Preferences.defaults().withUndoEnabled(false).withAutosaveEvery(5);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(preferences, panel.toPreferences());
    }
}

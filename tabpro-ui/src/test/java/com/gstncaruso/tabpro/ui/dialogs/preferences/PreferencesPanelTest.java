package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.awt.Component;
import javax.swing.JCheckBox;
import org.junit.jupiter.api.Test;

class PreferencesPanelTest {

    @Test
    void startsWithTheGivenPreferences() {
        Preferences preferences = new Preferences(NoteValue.EIGHTH, false, false, true, 20);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(preferences, panel.toPreferences());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        panel.apply(new Preferences(NoteValue.SIXTEENTH, true, false, true, 20));

        assertEquals(new Preferences(NoteValue.SIXTEENTH, true, false, true, 20), panel.toPreferences());
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

    /**
     * Sonido > Cuenta regresiva [Transport.toggleCountDown] ya prende una cuenta regresiva de
     * verdad. Esta casilla era un duplicado que no tocaba ese Transport para nada -Preferencias
     * solo la guardaba y releia a si misma-: el manual (linea 2148-2157) dice que Preferencias
     * [F12] configura el METRONOMO, y trata la Cuenta regresiva como un toggle propio del menu
     * Sonido, sin mencionar nunca una casilla en Preferencias para ella. Se saca en vez de
     * cablearla, para no inventar un control que el manual no describe.
     */
    @Test
    void noLongerOffersACountInCheckboxThatDuplicatesTheSoundMenuToggle() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        assertFalse(hasCheckboxLabeled(panel, "Cuenta regresiva antes de reproducir"));
    }

    private static boolean hasCheckboxLabeled(java.awt.Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JCheckBox checkBox && text.equals(checkBox.getText())) {
                return true;
            }
        }
        return false;
    }
}

package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Component;
import java.util.Locale;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PreferencesPanelTest {

    @Test
    void ningunCampoQuedaSinNombreNiTooltipAccesible() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void elComboDeFiguraPorDefectoMuestraElNombreEnCastellanoEnVezDelEnumCrudo() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        JComboBox combo = comboDeFigura(panel);
        Component rendered = combo.getRenderer()
                .getListCellRendererComponent(new JList<>(), NoteValue.QUARTER, 0, false, false);

        assertEquals("Negra", ((JLabel) rendered).getText());
    }

    @SuppressWarnings("rawtypes")
    private static JComboBox comboDeFigura(Component container) {
        if (container instanceof JComboBox combo) {
            return combo;
        }
        if (container instanceof java.awt.Container parent) {
            for (Component child : parent.getComponents()) {
                JComboBox found = comboDeFigura(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test
    void startsWithTheGivenPreferences() {
        Preferences preferences = new Preferences(NoteValue.EIGHTH, false, false, true, 20, false);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(preferences, panel.toPreferences());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        panel.apply(new Preferences(NoteValue.SIXTEENTH, true, false, true, 20, true));

        assertEquals(new Preferences(NoteValue.SIXTEENTH, true, false, true, 20, true), panel.toPreferences());
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

    /**
     * El manual, linea 1916: "You can force the multitrack view when using the Horizontal Screen
     * Mode in the Options > Preferences [F12]". La preferencia y su efecto ya existian; lo que
     * faltaba era el lugar donde el manual dice que se elige.
     */
    @Test
    void offersToForceTheMultitrackViewOnTheHorizontalScreen() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        checkBoxSaying(panel, "multipista").setSelected(true);

        assertTrue(panel.toPreferences().forceMultitrackInHorizontalMode());
    }

    private static JCheckBox checkBoxSaying(PreferencesPanel panel, String words) {
        for (Component child : panel.getComponents()) {
            if (child instanceof JCheckBox box && box.getText().toLowerCase(Locale.ROOT).contains(words)) {
                return box;
            }
        }
        throw new AssertionError("no hay ninguna casilla que hable de \"" + words + "\"");
    }

    /** Preferencias [F12] > Accesibilidad: la fuente base de la interfaz, hoy fija en 12pt. */
    @Test
    void offersToChooseTheInterfaceFontSize() {
        Preferences preferences = Preferences.defaults().withInterfaceFontSize(16);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(16, panel.toPreferences().interfaceFontSize());
    }

    /** Preferencias [F12] > Accesibilidad: manda sobre el tema oscuro/claro mientras esta prendido. */
    @Test
    void offersToTurnHighContrastOn() {
        Preferences preferences = Preferences.defaults().withHighContrastEnabled(true);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertTrue(panel.toPreferences().highContrastEnabled());
    }

    /** Preferencias [F12] > Accesibilidad: apaga las de FlatLaf y las propias de tabpro. */
    @Test
    void offersToTurnAnimationsOff() {
        Preferences preferences = Preferences.defaults().withAnimationsDisabled(true);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertTrue(panel.toPreferences().animationsDisabled());
    }
}

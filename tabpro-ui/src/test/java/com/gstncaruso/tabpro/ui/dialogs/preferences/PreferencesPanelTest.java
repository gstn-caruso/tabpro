package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Language;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.util.List;
import java.util.Locale;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class PreferencesPanelTest {

    @Test
    void everyFieldHasAnAccessibleNameAndTooltip() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void theAccessibilitySectionAndHighContrastCheckboxAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Accessibility", english.text("score_dialogs.PreferencesPanel.accessibility"));
        assertEquals("High Contrast", english.text("score_dialogs.PreferencesPanel.highContrast"));
    }

    @Test
    void theDefaultNoteValueComboShowsTheNameInSpanishInsteadOfTheRawEnum() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        JComboBox combo = noteValueCombo(panel);
        Component rendered = combo.getRenderer()
                .getListCellRendererComponent(new JList<>(), NoteValue.QUARTER, 0, false, false);

        assertEquals("Negra", ((JLabel) rendered).getText());
    }

    @SuppressWarnings("rawtypes")
    private static JComboBox noteValueCombo(Component container) {
        if (container instanceof JComboBox combo) {
            return combo;
        }
        if (container instanceof java.awt.Container parent) {
            for (Component child : parent.getComponents()) {
                JComboBox found = noteValueCombo(child);
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
        throw new AssertionError("there is no checkbox that mentions \"" + words + "\"");
    }

    @Test
    void offersToChooseTheInterfaceFontSize() {
        Preferences preferences = Preferences.defaults().withInterfaceFontSize(16);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertEquals(16, panel.toPreferences().interfaceFontSize());
    }

    @Test
    void offersToTurnHighContrastOn() {
        Preferences preferences = Preferences.defaults().withHighContrastEnabled(true);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertTrue(panel.toPreferences().highContrastEnabled());
    }

    @Test
    void offersToTurnAnimationsOff() {
        Preferences preferences = Preferences.defaults().withAnimationsDisabled(true);

        PreferencesPanel panel = new PreferencesPanel(preferences);

        assertTrue(panel.toPreferences().animationsDisabled());
    }

    @Test
    void choosingEnglishStoresEnglishAsTheInterfaceLanguage() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        languageCombo(panel).setSelectedItem(Language.ENGLISH);

        assertEquals(Language.ENGLISH, panel.toPreferences().interfaceLanguage());
    }

    @Test
    void theRestartNoteIsVisibleNextToTheLanguage() {
        PreferencesPanel panel = new PreferencesPanel(Preferences.defaults());

        JLabel note = labelSaying(panel, "Se aplica al reiniciar tabpro");

        assertTrue(note.isVisible());
        assertEquals("Takes effect after restarting tabpro",
                Texts.forLocale(Locale.ENGLISH).text("score_dialogs.PreferencesPanel.languageRestartNote"));
    }

    @Test
    void eachLanguageIsNamedInItselfWhileAutomaticFollowsTheInterface() {
        JComboBox<?> combo = languageCombo(new PreferencesPanel(Preferences.defaults()));
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals(List.of("Automático", "Español", "English"), renderedItems(combo));
        assertEquals("Automatic", english.text("score_dialogs.PreferencesPanel.languageAutomatic"));
        assertEquals("Español", english.text("score_dialogs.PreferencesPanel.languageSpanish"));
        assertEquals("English", english.text("score_dialogs.PreferencesPanel.languageEnglish"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<String> renderedItems(JComboBox combo) {
        List<String> rendered = new java.util.ArrayList<>();
        for (int index = 0; index < combo.getItemCount(); index++) {
            Component cell = combo.getRenderer()
                    .getListCellRendererComponent(new JList<>(), combo.getItemAt(index), index, false, false);
            rendered.add(((JLabel) cell).getText());
        }
        return rendered;
    }

    private static JLabel labelSaying(java.awt.Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && text.equals(label.getText())) {
                return label;
            }
            if (component instanceof java.awt.Container child) {
                JLabel found = labelSaying(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static JComboBox<?> languageCombo(Component component) {
        if (component instanceof JComboBox<?> combo && combo.getItemCount() > 0 && combo.getItemAt(0) instanceof Language) {
            return combo;
        }
        if (component instanceof java.awt.Container parent) {
            for (Component child : parent.getComponents()) {
                JComboBox<?> found = languageCombo(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}

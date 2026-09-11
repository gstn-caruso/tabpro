package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Component;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.Test;

class TuningEditorPanelTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TuningEditorPanel(Tuning.standard(), 25, player));
    }

    @Test
    void theFamilyButtonsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Guitars", english.text("score_dialogs.TuningEditorPanel.guitars"));
        assertEquals("Basses", english.text("score_dialogs.TuningEditorPanel.basses"));
        assertEquals("Others", english.text("score_dialogs.TuningEditorPanel.others"));
    }

    @Test
    void theStringRowLabelsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("String 3", english.text("score_dialogs.shared.string", 3));
        assertEquals("Listen to string 3", english.text("score_dialogs.TuningRow.listenToString", "string 3"));
    }

    @Test
    void theLibraryComboShowsTheNameAndStringSummaryInsteadOfTheRawRecord() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        @SuppressWarnings("unchecked")
        JComboBox<Tuning> library = Combos.firstWithItemType(panel, Tuning.class);
        Component rendered = library.getRenderer()
                .getListCellRendererComponent(new JList<>(), TuningLibrary.standardGuitar(), 0, false, false);

        assertEquals("Guitarra estándar (EADGBE)", ((JLabel) rendered).getText());
    }

    @Test
    void startsWithTheGivenTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        assertEquals(Tuning.standard(), panel.toTuning());
    }

    @Test
    void switchingFamilyOffersItsTunings() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standardBass(), 33, player);

        panel.switchToBasses();
        panel.selectFromLibrary(libraryTuningLabeled("Bajo Drop D (DADG)"));

        assertEquals("Bajo Drop D (DADG)", Labels.of(panel.toTuning()));
    }

    @Test
    void pickingFromTheLibraryReplacesTheTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);
        Tuning dropD = libraryTuningLabeled("Drop D (DADGBE)");

        panel.selectFromLibrary(dropD);

        assertEquals(dropD, panel.toTuning());
    }

    @Test
    void aTuningOutsideTheCurrentFamilyIsRejected() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        assertThrows(IllegalArgumentException.class, () -> panel.selectFromLibrary(TuningLibrary.standardBass()));
    }

    private static Tuning libraryTuningLabeled(String label) {
        return TuningLibrary.all().stream().filter(tuning -> Labels.of(tuning).equals(label)).findFirst().orElseThrow();
    }

    @Test
    void changingStringCountGrowsOrShrinksTheTuning() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.setStringCount(7);

        assertEquals(7, panel.toTuning().stringCount());
        assertEquals(Tuning.standard().strings(), panel.toTuning().strings().subList(0, 6));
    }

    @Test
    void editingAStringMakesTheTuningCustom() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.setStringPitch(6, new Pitch(38));

        assertEquals(new Pitch(38), panel.toTuning().pitchOfString(6));
    }

    @Test
    void listeningPlaysTheStringWithTheTracksInstrument() {
        TuningEditorPanel panel = new TuningEditorPanel(Tuning.standard(), 25, player);

        panel.listen(1);

        assertEquals(1, player.sounded().size());
        assertEquals(Tuning.standard().pitchOfString(1), player.sounded().getFirst().pitch());
        assertEquals(25, player.sounded().getFirst().program());
    }
}

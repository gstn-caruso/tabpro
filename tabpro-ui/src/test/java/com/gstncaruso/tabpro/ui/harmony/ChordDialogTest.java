package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Component;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordDialogTest {

    @Test
    void theFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Chord", english.text("views.ChordDialog.title"));
        assertEquals("Fundamental", english.text("views.ChordDialog.fundamental"));
        assertEquals("Alternative Names", english.text("views.ChordDialog.alternativeNames"));
        assertEquals("Use Diagram", english.text("views.ChordDialog.useDiagram"));
    }

    private final Preferences scratch =
            Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void theChordTypeComboShowsTheMusicalSuffixInsteadOfTheRawEnum() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<ChordType> types = Combos.firstWithItemType(panel, ChordType.class);
        Component rendered = types.getRenderer()
                .getListCellRendererComponent(new JList<>(), ChordType.MINOR_SEVENTH, 0, false, false);

        assertEquals("m7", ((JLabel) rendered).getText());
    }

    @Test
    void thePositionsAreAlwaysVisibleRadioButtonsThatChooseTheComplexity() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        javax.swing.JRadioButton simple = Combos.radioButtonWithText(panel, "Simple");
        assertNotNull(Combos.radioButtonWithText(panel, "Media"), "could not find the 'Media' radio button");
        assertNotNull(Combos.radioButtonWithText(panel, "Todas"), "could not find the 'Todas' radio button");
        assertNotNull(simple, "could not find the 'Simple' radio button");

        simple.doClick();

        assertEquals(ChordComplexity.SIMPLE, model.selection().complexity());
    }

    @Test
    void theBarreIsChosenWithAlwaysVisibleRadioButtons() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        javax.swing.JRadioButton force = Combos.radioButtonWithText(panel, "Forzar cejilla");
        assertNotNull(Combos.radioButtonWithText(panel, "Cualquiera"), "could not find the 'Cualquiera' radio button");
        assertNotNull(Combos.radioButtonWithText(panel, "Prohibir cejilla"), "could not find the 'Prohibir cejilla' radio button");
        assertNotNull(force, "could not find the 'Forzar cejilla' radio button");

        force.doClick();

        assertEquals(BarrePreference.FORCE, model.barrePreference());
    }

    @Test
    void theRootComboShowsTheNoteNameInSpanish() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<PitchClass> roots = Combos.firstWithItemType(panel, PitchClass.class);
        Component rendered = roots.getRenderer()
                .getListCellRendererComponent(new JList<>(), PitchClass.of("C"), 0, false, false);

        assertEquals("C (Do)", ((JLabel) rendered).getText());
    }

    @Test
    void theInversionComboShowsFundamentalForTheRootAndTheDegreeWithTheNoteForTheOthers() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        @SuppressWarnings("unchecked")
        JComboBox<com.gstncaruso.tabpro.core.harmony.Interval> inversions =
                Combos.firstWithItemType(panel, com.gstncaruso.tabpro.core.harmony.Interval.class);

        String rootLabel = ((JLabel) inversions.getRenderer().getListCellRendererComponent(
                new JList<>(), com.gstncaruso.tabpro.core.harmony.Interval.ROOT, 0, false, false)).getText();
        String thirdLabel = ((JLabel) inversions.getRenderer().getListCellRendererComponent(
                new JList<>(), com.gstncaruso.tabpro.core.harmony.Interval.MAJOR_THIRD, 0, false, false)).getText();

        assertEquals("Fundamental", rootLabel);
        assertEquals("E (3)", thirdLabel);
    }

    @Test
    void theAlternativeNamesListShowsTheChordNameInsteadOfTheRawRecord() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), Tuning.standard());
        ChordLibrary library = new ChordLibrary(scratch);

        ChordDialog.Panel panel = new ChordDialog.Panel(model, library, editor, new RecordingPlayer());

        JList<?> alternativeNames = Combos.firstListNamed(panel, "Nombres alternativos");
        Chord chord = Chord.of(PitchClass.of("C"), ChordType.MINOR_SEVENTH);
        String renderedText = Combos.renderedTextOfList(alternativeNames, chord);

        assertEquals("Cm7", renderedText);
    }
}

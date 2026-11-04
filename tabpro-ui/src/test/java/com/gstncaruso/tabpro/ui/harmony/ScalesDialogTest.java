package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.core.harmony.ScaleMatch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractButton;
import javax.swing.JList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScalesDialogTest {

    @Test
    void theFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Scales", english.text("views.ScalesDialog.title"));
        assertEquals("Tonality", english.text("views.ScalesDialog.tonality"));
        assertEquals("Scale", english.text("views.ScalesDialog.scale"));
        assertEquals("Find!", english.text("views.ScalesDialog.find"));
        assertEquals("Scales Found", english.text("views.ScalesDialog.matchesFound"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        AccessibilityAssertions.assertNoViolations(panel);
    }

    @Test
    void theTonicListShowsTheNoteNameInSpanish() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        JList<?> tonics = Combos.firstListNamed(panel, "Tonalidad");

        assertEquals("C (Do)", Combos.renderedTextOfList(tonics, PitchClass.of("C")));
    }

    @Test
    void theScaleListShowsTheNameInSpanishInsteadOfTheRawRecord() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        JList<?> scales = Combos.firstListNamed(panel, "Escala");

        assertEquals("Mayor (Jónico)", Combos.renderedTextOfList(scales, ScaleLibrary.major()));
    }

    @Test
    void aFoundScaleShowsItsTonicItsSpanishNameAndItsIncidentNotes() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        JList<?> found = Combos.firstListNamed(panel, "Escalas encontradas");

        assertEquals("D Dórico   [2]",
                Combos.renderedTextOfList(found, new ScaleMatch(PitchClass.of("D"), ScaleLibrary.dorian(), 2)));
    }

    @Test
    void withoutAPreviousChoiceItOpensWithCMajorAndTheDiagramAlreadyPainted() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), chosen);

        assertEquals(PitchClass.of("C"), chosen.tonic().orElseThrow());
        assertEquals(ScaleLibrary.major(), chosen.scale().orElseThrow());
        ScaleDegreesView degrees = firstDegreesView(panel);
        assertEquals(7, degrees.degreeCount());
    }

    @Test
    void withAPreviousChoiceItRespectsItInsteadOfForcingCMajor() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();
        chosen.choose(PitchClass.of("D"), ScaleLibrary.dorian());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), chosen);

        assertEquals(PitchClass.of("D"), chosen.tonic().orElseThrow());
        assertEquals(ScaleLibrary.dorian(), chosen.scale().orElseThrow());
    }

    @Test
    void pickingTonicAndScaleInTheListsReachesChosenScale() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), chosen);
        @SuppressWarnings("unchecked")
        JList<PitchClass> tonics = (JList<PitchClass>) Combos.firstListNamed(panel, "Tonalidad");
        @SuppressWarnings("unchecked")
        JList<Scale> scales = (JList<Scale>) Combos.firstListNamed(panel, "Escala");

        tonics.setSelectedValue(PitchClass.of("D"), true);
        scales.setSelectedValue(ScaleLibrary.major(), true);

        assertTrue(chosen.tonic().isPresent());
        assertEquals(PitchClass.of("D"), chosen.tonic().orElseThrow());
        assertEquals(ScaleLibrary.major(), chosen.scale().orElseThrow());
    }

    @Test
    void pickingAScaleMakesTheDegreeDiagramShowItsNotes() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();
        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), chosen);
        @SuppressWarnings("unchecked")
        JList<PitchClass> tonics = (JList<PitchClass>) Combos.firstListNamed(panel, "Tonalidad");
        @SuppressWarnings("unchecked")
        JList<Scale> scales = (JList<Scale>) Combos.firstListNamed(panel, "Escala");

        tonics.setSelectedValue(PitchClass.of("C"), true);
        scales.setSelectedValue(ScaleLibrary.major(), true);

        ScaleDegreesView degrees = firstDegreesView(panel);
        assertEquals(7, degrees.degreeCount());
        assertEquals("C", degrees.noteLabel(0));
    }

    @Test
    void theListenButtonPlaysTheChosenScaleAscending() {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();
        RecordingPlayer player = new RecordingPlayer();
        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, player, chosen);
        @SuppressWarnings("unchecked")
        JList<PitchClass> tonics = (JList<PitchClass>) Combos.firstListNamed(panel, "Tonalidad");
        @SuppressWarnings("unchecked")
        JList<Scale> scales = (JList<Scale>) Combos.firstListNamed(panel, "Escala");
        tonics.setSelectedValue(PitchClass.of("C"), true);
        scales.setSelectedValue(ScaleLibrary.major(), true);

        firstButtonNamed(panel, "Escuchar").doClick();

        List<Integer> soundedPitches = player.sounded().stream()
                .map(sounded -> sounded.pitch().midiNumber())
                .toList();
        assertEquals(List.of(60, 62, 64, 65, 67, 69, 71), soundedPitches);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "C mayor, C, false",
        "G mayor, G, false",
        "D mayor, D, false",
        "A mayor, A, false",
        "E mayor, E, false",
        "B mayor, B, false",
        "F#/Gb mayor, F#, false",
        "Db mayor, Db, false",
        "Ab mayor, Ab, false",
        "Eb mayor, Eb, false",
        "Bb mayor, Bb, false",
        "F mayor, F, false",
        "A menor, A, true",
        "E menor, E, true",
        "B menor, B, true",
        "F# menor, F#, true",
        "C# menor, C#, true",
        "G# menor, G#, true",
        "D#/Eb menor, D#, true",
        "Bb menor, Bb, true",
        "F menor, F, true",
        "C menor, C, true",
        "G menor, G, true",
        "D menor, D, true"
    })
    void givenScalesDialog_whenCircleOptionIsClicked_thenSelectionDegreesAndListeningUseThatScale(
            String accessibleName, String tonicName, boolean minor) {
        Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
        ChosenScale chosen = new ChosenScale();
        chosen.choose(PitchClass.of("D"), ScaleLibrary.dorian());
        RecordingPlayer player = new RecordingPlayer();
        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, player, chosen);
        PitchClass expectedTonic = PitchClass.of(tonicName);
        Scale expectedScale = minor ? ScaleLibrary.naturalMinor() : ScaleLibrary.major();

        AbstractButton option = firstButtonNamed(panel, accessibleName);
        assertNotNull(option, "Falta la opción accesible del círculo de quintas: " + accessibleName);
        assertTrue(option.isVisible());
        assertTrue(option.isEnabled());
        option.doClick();

        assertEquals(expectedTonic, chosen.tonic().orElseThrow());
        assertEquals(expectedScale, chosen.scale().orElseThrow());
        var expectedTones = expectedScale.notesFrom(expectedTonic);
        ScaleDegreesView degrees = firstDegreesView(panel);
        assertEquals(expectedTones.size(), degrees.degreeCount());
        for (int degree = 0; degree < expectedTones.size(); degree++) {
            assertEquals(expectedTones.get(degree).pitchClass().name(), degrees.noteLabel(degree));
        }

        firstButtonNamed(panel, "Escuchar").doClick();

        assertEquals(expectedTones.stream().map(tone -> tone.pitchClass().semitone()).toList(),
                player.sounded().stream().map(sounded -> sounded.pitch().midiNumber() % 12).toList());
        assertEquals(java.util.Collections.nCopies(expectedTones.size(), editor.currentTrack().channel().program()),
                player.sounded().stream().map(RecordingPlayer.Sounded::program).toList());
    }

    private static ScaleDegreesView firstDegreesView(Container root) {
        for (Component child : root.getComponents()) {
            if (child instanceof ScaleDegreesView view) {
                return view;
            }
            if (child instanceof Container container) {
                ScaleDegreesView found = firstDegreesView(container);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractButton firstButtonNamed(Container root, String accessibleName) {
        for (Component child : root.getComponents()) {
            if (child instanceof AbstractButton button
                    && accessibleName.equals(button.getAccessibleContext().getAccessibleName())) {
                return button;
            }
            if (child instanceof Container container) {
                AbstractButton found = firstButtonNamed(container, accessibleName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}

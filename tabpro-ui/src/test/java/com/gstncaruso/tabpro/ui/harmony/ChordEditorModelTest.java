package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordEditorModelTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
    private final FingeringMemory fingeringMemory = new FingeringMemory(scratch);
    private final ChordEditorModel model = new ChordEditorModel(Tuning.standard(), true, fingeringMemory);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void startsOnCMajorWithAllTheDiagrams() {
        assertEquals(PitchClass.of("C"), model.selection().root());
        assertEquals(ChordType.MAJOR, model.selection().type());
        assertFalse(model.isCustom());
        assertFalse(model.candidates().isEmpty());
        assertEquals(model.candidates().get(0), model.current());
    }

    @Test
    void pickingAnotherRootRebuildsTheDiagrams() {
        model.selectRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("G"), model.selection().root());
        assertTrue(model.current().name().startsWith("G"));
        assertFalse(model.isCustom());
    }

    @Test
    void pickingTheTypeRebuildsTheDiagrams() {
        model.selectType(ChordType.MINOR_SEVENTH);

        assertEquals(ChordType.MINOR_SEVENTH, model.selection().type());
        assertEquals("Cm7", model.current().name());
    }

    @Test
    void pickingTheBassBuildsAnInversion() {
        model.selectBass(PitchClass.of("E"));

        assertEquals("C/E", model.current().name());
        assertTrue(model.selection().chord().isInverted());
    }

    @Test
    void pickingTheInversionByDegreeSetsTheBassToThatDegreesNote() {
        model.selectInversion(Interval.MAJOR_THIRD);

        assertEquals(PitchClass.of("E"), model.selection().bass());
        assertEquals("C/E", model.current().name());
    }

    @Test
    void theComplexityFilterLeavesOutTheHardOnes() {
        model.selectType(ChordType.MAJOR_SEVENTH);
        model.selectComplexity(ChordComplexity.SIMPLE);

        assertTrue(model.candidates().stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void theBarreFilterOnlyKeepsTheOnesThatNeedIt() {
        model.selectRoot(PitchClass.of("F"));
        model.selectBarrePreference(BarrePreference.FORCE);

        assertFalse(model.candidates().isEmpty());
        assertTrue(model.candidates().stream().allMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void theForbiddenBarreFilterRemovesTheOnesThatNeedIt() {
        model.selectRoot(PitchClass.of("F"));
        model.selectBarrePreference(BarrePreference.FORBID);

        assertTrue(model.candidates().stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void pickingADiagramFromTheListMakesItTheCurrentOne() {
        model.selectType(ChordType.SEVENTH);
        ChordDiagram another = model.candidates().get(model.candidates().size() - 1);

        model.pickCandidate(another);

        assertEquals(another, model.current());
        assertFalse(model.isCustom());
    }

    @Test
    void offersAlternativeNamesForTheCurrentDiagram() {
        assertTrue(model.alternativeNames().stream().anyMatch(chord -> chord.name().equals("C")));
    }

    @Test
    void pickingAnAlternativeNameRebuildsZoneA() {
        model.selectType(ChordType.MINOR_SEVENTH);
        Chord alternative = Chord.of(PitchClass.of("F"), ChordType.SIXTH);

        model.pickAlternativeName(alternative);

        assertEquals(PitchClass.of("F"), model.selection().root());
        assertEquals(ChordType.SIXTH, model.selection().type());
        assertEquals("F6", model.current().name());
    }

    @Test
    void pressingAStringOnAFretSwitchesToCustomMode() {
        model.toggleFret(1, 3);

        assertTrue(model.isCustom());
        assertEquals("", model.current().name());
        assertEquals(3, model.current().fretOfString(1));
    }

    @Test
    void pressingTheSameNoteAgainRemovesIt() {
        model.toggleFret(1, 0);

        model.toggleFret(1, 0);

        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(1));
    }

    @Test
    void theHeaderTogglesBetweenOpenStringAndMutedString() {
        model.toggleFret(2, 3);

        model.toggleOpenOrMuted(2);
        assertEquals(0, model.current().fretOfString(2));

        model.toggleOpenOrMuted(2);
        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(2));

        model.toggleOpenOrMuted(2);
        assertEquals(0, model.current().fretOfString(2));
    }

    @Test
    void editingAFingerStaysInTheFingering() {
        model.setFinger(1, Finger.LITTLE);

        assertEquals(Finger.LITTLE, model.current().fingerOfString(1).orElseThrow());
    }

    @Test
    void clickingTheNumberMovesToTheNextFingerThenBackToNoFinger() {
        model.toggleFret(1, 3);
        model.setFinger(1, null);

        model.cycleFinger(1);
        assertEquals(Finger.THUMB, model.current().fingerOfString(1).orElseThrow());

        model.cycleFinger(1);
        assertEquals(Finger.INDEX, model.current().fingerOfString(1).orElseThrow());

        for (int i = 0; i < Finger.values().length - 1; i++) {
            model.cycleFinger(1);
        }
        assertTrue(model.current().fingerOfString(1).isEmpty(), "after the last finger it goes back to having none");
    }

    @Test
    void anOpenOrMutedStringIsNotFingered() {
        model.toggleOpenOrMuted(1);

        model.cycleFinger(1);

        assertTrue(model.current().fingerOfString(1).isEmpty());
    }

    @Test
    void editingAFingerRemembersItForTheSameShapeInAnotherChord() {
        model.selectRoot(PitchClass.of("F"));
        ChordDiagram fBarreDiagram = model.candidates().stream()
                .filter(ChordDiagram::requiresBarre)
                .findFirst()
                .orElseThrow(() -> new AssertionError("a position with a barre is needed for F major"));
        model.pickCandidate(fBarreDiagram);

        model.setFinger(1, Finger.LITTLE);

        model.selectRoot(PitchClass.of("G"));
        List<ChordDiagram> sameShapeDiagrams = model.candidates().stream()
                .filter(diagram -> diagram.shape().equals(fBarreDiagram.shape()))
                .toList();
        assertFalse(sameShapeDiagrams.isEmpty(), "G major has to offer the same barre shape as F");
        assertTrue(
                sameShapeDiagrams.stream().allMatch(diagram -> diagram.fingerOfString(1).equals(Optional.of(Finger.LITTLE))),
                "the hand-corrected fingering is reused in the similar shape");
    }

    @Test
    void changingChordsStartsWithNoOmittedTone() {
        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        model.selectType(ChordType.MINOR);

        assertTrue(model.omittedTones().isEmpty());
    }

    @Test
    void theTonesThatCanBeOmittedAreTheChordFormulaOnes() {
        model.selectType(ChordType.SEVENTH);

        assertEquals(
                List.of(Interval.ROOT, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH, Interval.MINOR_SEVENTH),
                model.omittableTones());
    }

    @Test
    void omittingAToneOffersDiagramsThatDoNotNeedIt() {
        int beforeOmitting = model.candidates().size();

        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        assertTrue(model.candidates().size() > beforeOmitting, "omitting the fifth adds new positions");
        assertTrue(model.omittedTones().contains(Interval.PERFECT_FIFTH));
    }

    @Test
    void uncheckingAToneRequiresItAgain() {
        model.setToneOmitted(Interval.PERFECT_FIFTH, true);

        model.setToneOmitted(Interval.PERFECT_FIFTH, false);

        assertTrue(model.omittedTones().isEmpty());
    }

    @Test
    void movingTheBaseFretDoesNotTouchWhatIsAlreadyFretted() {
        var frets = model.current().frets();

        model.setBaseFret(5);

        assertEquals(5, model.current().baseFret());
        assertEquals(frets, model.current().frets());
    }

    @Test
    void typingAnyNameOnlyAppliesInCustomMode() {
        model.toggleFret(1, 3);

        model.setCustomName("My weird chord");

        assertEquals("My weird chord", model.current().name());
    }

    @Test
    void whenTheDiagramIsUnusedTheResultOnlyShowsTheName() {
        model.setUseDiagram(false);

        assertFalse(model.result().shown());
        assertTrue(model.current().shown(), "zone B keeps showing the diagram while editing");
    }

    @Test
    void whenFingeringIsUnusedTheResultDoesNotCarryIt() {
        model.setShowFingering(false);

        assertTrue(model.result().fingering().stream().allMatch(finger -> finger == null));
    }

    @Test
    void byDefaultTheResultUsesDiagramAndFingering() {
        ChordDiagram result = model.result();

        assertTrue(result.shown());
        assertEquals(model.current().fingering(), result.fingering());
    }

    @Test
    void theShowBassPreferenceCanBeTurnedOff() {
        ChordEditorModel noBassModel = new ChordEditorModel(Tuning.standard(), false, fingeringMemory);
        noBassModel.selectBass(PitchClass.of("E"));

        assertEquals("C", noBassModel.current().name());
    }
}

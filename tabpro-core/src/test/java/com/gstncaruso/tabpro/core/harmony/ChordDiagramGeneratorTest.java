package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChordDiagramGeneratorTest {

    @Test
    void findsTheStandardFingeringForAMinorInStandardTuning() {
        Chord aMinor = Chord.of(PitchClass.of("La"), ChordType.MINOR);
        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(aMinor, Tuning.standard());

        assertTrue(diagrams.stream().anyMatch(d -> d.frets().equals(List.of(0, 1, 2, 2, 0, -1))));
    }

    @Test
    void noDiagramStretchesBeyondWhatIsAllowed() {
        Chord g7 = Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH);
        int maxSpan = 3;
        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(g7, Tuning.standard(), maxSpan);

        assertFalse(diagrams.isEmpty());
        assertTrue(diagrams.stream().allMatch(d -> d.fretSpan() <= maxSpan));
    }

    @Test
    void noStringPlaysANoteForeignToTheChord() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(cMajor, Tuning.standard());

        List<Integer> soundingSemitones = soundingSemitonesOf(diagrams);

        assertFalse(soundingSemitones.isEmpty(), "no diagram plays a string: there would be nothing to check");
        assertEquals(
                List.of(),
                soundingSemitones.stream().filter(semitone -> !cMajor.formulaSemitones().contains(semitone)).toList(),
                "some strings sound notes foreign to the chord");
    }

    private static List<Integer> soundingSemitonesOf(List<ChordDiagram> diagrams) {
        List<Integer> semitones = new ArrayList<>();
        for (ChordDiagram diagram : diagrams) {
            for (int string = 1; string <= diagram.stringCount(); string++) {
                if (diagram.isPlayed(string)) {
                    semitones.add(
                            (Tuning.standard().pitchOfString(string).midiNumber() + diagram.fretOfString(string)) % 12);
                }
            }
        }
        return semitones;
    }

    @Test
    void theIndicatedBassAlwaysSoundsOnTheLowestString() {
        Chord cWithEInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(cWithEInTheBass, Tuning.standard());

        assertFalse(diagrams.isEmpty());
        for (ChordDiagram diagram : diagrams) {
            int lowestString = lowestSoundingStringOf(diagram);
            int semitone = (Tuning.standard().pitchOfString(lowestString).midiNumber()
                            + diagram.fretOfString(lowestString))
                    % 12;
            assertEquals(PitchClass.of("Mi").semitone(), semitone);
        }
    }

    @Test
    void theSimpleFilterNeverReturnsBarreChords() {
        Chord f = Chord.of(PitchClass.of("Fa"), ChordType.MAJOR);
        List<ChordDiagram> simpleDiagrams =
                ChordDiagramGenerator.generate(f, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.SIMPLE);

        assertTrue(simpleDiagrams.stream().noneMatch(ChordDiagram::requiresBarre));
    }

    @Test
    void areOrderedByIncreasingDifficulty() {
        Chord eMinor = Chord.of(PitchClass.of("Mi"), ChordType.MINOR);
        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(eMinor, Tuning.standard());

        for (int i = 1; i < diagrams.size(); i++) {
            assertTrue(diagrams.get(i - 1).difficultyScore() <= diagrams.get(i).difficultyScore());
        }
    }

    @Test
    void generatesDiagramsForAnyTuning() {
        Chord dMajor = Chord.of(PitchClass.of("Re"), ChordType.MAJOR);
        Tuning dadgad = Tuning.of("DADGAD", 62, 57, 55, 50, 45, 38);

        List<ChordDiagram> diagrams = ChordDiagramGenerator.generate(dMajor, dadgad);

        assertFalse(diagrams.isEmpty());
    }

    @Test
    void omittingAToneRelaxesTheSearchWithoutForbiddingIt() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        List<ChordDiagram> normal = ChordDiagramGenerator.generate(
                cMajor, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX, Set.of());
        List<ChordDiagram> withoutRequiredFifth = ChordDiagramGenerator.generate(
                cMajor, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX,
                Set.of(Interval.PERFECT_FIFTH));

        assertTrue(withoutRequiredFifth.containsAll(normal), "omitting relaxes the requirement, it does not forbid the note");
        assertTrue(withoutRequiredFifth.size() > normal.size(), "new positions appear without the fifth");
    }

    @Test
    void omittingAnIntervalTheChordDoesNotHaveChangesNothing() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        List<ChordDiagram> normal = ChordDiagramGenerator.generate(cMajor, Tuning.standard());
        List<ChordDiagram> withIrrelevantOmission = ChordDiagramGenerator.generate(
                cMajor, Tuning.standard(), ChordDiagramGenerator.DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX,
                Set.of(Interval.MINOR_SEVENTH));

        assertEquals(normal, withIrrelevantOmission);
    }

    private static int lowestSoundingStringOf(ChordDiagram diagram) {
        for (int string = diagram.stringCount(); string >= 1; string--) {
            if (diagram.isPlayed(string)) {
                return string;
            }
        }
        throw new IllegalStateException("a valid diagram always has some string sounding");
    }
}

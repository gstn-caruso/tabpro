package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordDiagramsTest {

    @Test
    void aBeatWithoutNotesIsMutedOnAllStrings() {
        ChordDiagram diagram = ChordDiagrams.fromBeat(Beat.rest(Duration.quarter()), Tuning.standard());

        for (int string = 1; string <= 6; string++) {
            assertEquals(ChordDiagram.MUTED, diagram.fretOfString(string));
        }
    }

    @Test
    void eachNoteOfTheBeatLandsOnItsString() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2), new Note(4, 2));

        ChordDiagram diagram = ChordDiagrams.fromBeat(beat, Tuning.standard());

        assertEquals(0, diagram.fretOfString(6));
        assertEquals(2, diagram.fretOfString(5));
        assertEquals(2, diagram.fretOfString(4));
        assertEquals(ChordDiagram.MUTED, diagram.fretOfString(3));
    }

    @Test
    void startsAtTheLowestFrettedFretWhenThereAreNoOpenStrings() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 5), new Note(5, 7));

        ChordDiagram diagram = ChordDiagrams.fromBeat(beat, Tuning.standard());

        assertEquals(5, diagram.baseFret());
    }

    @Test
    void changingTheBaseFretDoesNotTouchTheFretsAlreadyPressed() {
        ChordDiagram diagram = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        ChordDiagram moved = ChordDiagrams.withBaseFret(diagram, 5);

        assertEquals(5, moved.baseFret());
        assertEquals(diagram.frets(), moved.frets());
        assertEquals(diagram.name(), moved.name());
    }
}

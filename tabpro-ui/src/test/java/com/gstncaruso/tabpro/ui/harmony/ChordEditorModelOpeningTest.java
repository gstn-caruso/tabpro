package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ChordEditorModelOpeningTest {

    @Test
    void anEmptyBeatStartsWithTheInitialSelection() {
        ChordEditorModel model = ChordEditorModel.forBeat(Beat.rest(Duration.quarter()), Tuning.standard());

        assertEquals(PitchClass.of("C"), model.selection().root());
        assertFalse(model.isCustom());
    }

    @Test
    void aBeatWithNotesAndNoChordLoadsThoseNotesIntoTheMainDiagram() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 2), new Note(4, 2));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertEquals(0, model.current().fretOfString(6));
        assertEquals(2, model.current().fretOfString(5));
        assertEquals(2, model.current().fretOfString(4));
    }

    @Test
    void aBeatWithNotesFormingAKnownChordDoesNotEndUpInCustomMode() {
        Beat beat = Beat.of(
                Duration.quarter(),
                new Note(6, 0), new Note(5, 2), new Note(4, 2),
                new Note(3, 0), new Note(2, 0), new Note(1, 0));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertFalse(model.isCustom());
        assertEquals("Em", model.current().name());
    }

    @Test
    void aBeatWithNotesFormingNoKnownChordEndsUpInCustomMode() {
        Beat beat = Beat.of(Duration.quarter(), new Note(6, 0), new Note(5, 1));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertTrue(model.isCustom());
        assertEquals("", model.current().name());
    }

    @Test
    void aBeatThatAlreadyHasAChordLoadsItAsIs() {
        ChordDiagram existingDiagram = ChordDiagram.named("Am7", List.of(0, 1, 0, 2, 0, -1));
        Beat beat = Beat.rest(Duration.quarter()).withEffects(BeatEffects.none().withChord(existingDiagram));

        ChordEditorModel model = ChordEditorModel.forBeat(beat, Tuning.standard());

        assertEquals(ChordDiagram.MUTED, model.current().fretOfString(6));
        assertEquals(0, model.current().fretOfString(5));
        assertEquals(2, model.current().fretOfString(4));
    }

    @Test
    void acceptingWritesTheChordIntoTheBeat() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());
        model.selectRoot(PitchClass.of("G"));

        model.applyTo(editor);

        assertTrue(editor.currentBeat().effects().chord().isPresent());
        assertEquals("G", editor.currentBeat().effects().chord().orElseThrow().name());
    }

    @Test
    void ifTheBeatHadNoNotesAcceptingWritesThem() {
        Editor editor = new Editor(Score.blank());
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());
        model.selectType(com.gstncaruso.tabpro.core.harmony.ChordType.MINOR);

        model.applyTo(editor);

        assertFalse(editor.currentBeat().notes().isEmpty(), "un beat vacio recibe las notas del diagrama elegido");
    }

    @Test
    void ifTheBeatAlreadyHadNotesAcceptingDoesNotTouchThem() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(5);
        Note originalNote = editor.currentBeat().noteOn(editor.cursor().string()).orElseThrow();
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());

        model.applyTo(editor);

        assertEquals(Optional.of(originalNote), editor.currentBeat().noteOn(originalNote.string()));
    }

    @Test
    void acceptingLeavesTheCursorWhereItWas() {
        Editor editor = new Editor(Score.blank());
        editor.moveTo(0, 0, 3);
        ChordEditorModel model = ChordEditorModel.forBeat(editor.currentBeat(), editor.currentTrack().tuning());

        model.applyTo(editor);

        assertEquals(3, editor.cursor().string());
    }
}

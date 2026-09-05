package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InstrumentEditingTest {

    private final Editor editor = new Editor(Score.blank());
    private final RecordingPlayer player = new RecordingPlayer();
    private final InstrumentEditing editing = new InstrumentEditing(editor, player);

    @Test
    void writesTheFretYouPressOnTheFretboard() {
        editing.pressFret(new Note(6, 3));

        assertEquals(Optional.of(new Note(6, 3)), editor.currentBeat().noteOn(6));
    }

    @Test
    void movesTheCursorToTheStringYouPress() {
        editing.pressFret(new Note(4, 7));

        assertEquals(4, editor.cursor().string());
    }

    @Test
    void addsTheNoteToTheOnesTheBeatAlreadyHas() {
        editing.pressFret(new Note(6, 3));
        editing.pressFret(new Note(5, 2));

        assertEquals(Optional.of(new Note(6, 3)), editor.currentBeat().noteOn(6));
        assertEquals(Optional.of(new Note(5, 2)), editor.currentBeat().noteOn(5));
    }

    @Test
    void aSecondFretOnTheSameStringReplacesTheFirst() {
        editing.pressFret(new Note(6, 3));
        editing.pressFret(new Note(6, 5));

        assertEquals(Optional.of(new Note(6, 5)), editor.currentBeat().noteOn(6));
        assertEquals(1, editor.currentBeat().notes().size());
    }

    @Test
    void writesTheKeyYouPressOnTheStringOfTheCursor() {
        editor.moveTo(0, 0, 3);

        editing.pressKey(60);

        assertEquals(Optional.of(new Note(3, 5)), editor.currentBeat().noteOn(3));
    }

    @Test
    void ignoresAKeyTheStringOfTheCursorCannotReach() {
        editor.moveTo(0, 0, 1);

        editing.pressKey(40);

        assertTrue(editor.currentBeat().isRest(), "la primera cuerda no llega al mi grave");
    }

    @Test
    void writesOnTheTuningOfTheTrackTheCursorIsOn() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.moveTo(0, 0, 4);

        editing.pressKey(31);

        assertEquals(Optional.of(new Note(4, 3)), editor.currentBeat().noteOn(4));
    }

    @Test
    void soundsTheNoteWithTheInstrumentOfTheTrack() {
        editing.pressFret(new Note(6, 3));

        assertEquals(
                List.of(new RecordingPlayer.Sounded(new Pitch(43), Track.GUITAR_PROGRAM)),
                player.sounded());
    }

    @Test
    void soundsNothingWhenNothingGetsWritten() {
        editor.moveTo(0, 0, 1);

        editing.pressKey(40);

        assertEquals(List.of(), player.sounded());
    }

    @Test
    void undoingTakesBackOnePressAtATime() {
        editing.pressFret(new Note(6, 3));
        editing.pressFret(new Note(5, 2));

        editor.undo();

        assertEquals(Optional.empty(), editor.currentBeat().noteOn(5));
        assertEquals(Optional.of(new Note(6, 3)), editor.currentBeat().noteOn(6));
    }
}

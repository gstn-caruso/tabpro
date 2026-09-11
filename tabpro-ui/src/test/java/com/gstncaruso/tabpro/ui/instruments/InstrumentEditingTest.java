package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InstrumentEditingTest {

    private final Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
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
    void writesAKeyTheCursorStringCannotReachOnAStringThatCan() {
        editor.moveTo(0, 0, 1);

        editing.pressKey(40);

        assertEquals(Optional.of(new Note(6, 0)), editor.currentBeat().noteOn(6));
    }

    @Test
    void ignoresAKeyNoStringCanReach() {
        editor.moveTo(0, 0, 1);

        editing.pressKey(20);

        assertTrue(editor.currentBeat().isRest(), "no string goes down that far");
    }

    @Test
    void aSecondKeySumsUpIntoAChordInsteadOfPushingTheFirstOut() {
        editor.moveTo(0, 0, 3);

        editing.pressKey(60);
        editing.pressKey(64);

        assertEquals(Optional.of(new Note(3, 5)), editor.currentBeat().noteOn(3));
        assertEquals(Optional.of(new Note(1, 0)), editor.currentBeat().noteOn(1));
    }

    @Test
    void everyKeyKeepsGrowingTheChordOfTheBeat() {
        editor.moveTo(0, 0, 3);

        editing.pressKey(60);
        editing.pressKey(64);
        editing.pressKey(67);

        assertEquals(3, editor.currentBeat().notes().size());
        assertEquals(
                List.of(60, 64, 67),
                editor.currentBeat().notes().stream()
                        .map(note -> editor.currentTrack().tuning().pitchOf(note).midiNumber())
                        .sorted()
                        .toList());
    }

    @Test
    void withNoFreeStringLeftTheKeyOverwritesTheCursorString() {
        for (int string = 1; string <= 6; string++) {
            editing.pressFret(new Note(string, 1));
        }
        editor.moveTo(0, 0, 3);

        editing.pressKey(60);

        assertEquals(Optional.of(new Note(3, 5)), editor.currentBeat().noteOn(3));
        assertEquals(6, editor.currentBeat().notes().size());
    }

    @Test
    void togglingAKeyOffFindsItWhereverItLandedInTheChord() {
        editor.moveTo(0, 0, 3);
        editing.toggleKey(60);
        editing.toggleKey(64);

        editing.toggleKey(64);

        assertEquals(Optional.of(new Note(3, 5)), editor.currentBeat().noteOn(3));
        assertEquals(1, editor.currentBeat().notes().size());
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

        editing.pressKey(20);

        assertEquals(List.of(), player.sounded());
    }

    @Test
    void togglingAnEmptyFretAddsIt() {
        editing.toggleFret(new Note(6, 3));

        assertEquals(Optional.of(new Note(6, 3)), editor.currentBeat().noteOn(6));
    }

    @Test
    void togglingTheSameFretAgainRemovesIt() {
        editing.toggleFret(new Note(6, 3));

        editing.toggleFret(new Note(6, 3));

        assertEquals(Optional.empty(), editor.currentBeat().noteOn(6));
    }

    @Test
    void togglingADifferentFretOnTheSameStringReplacesItInstead() {
        editing.toggleFret(new Note(6, 3));

        editing.toggleFret(new Note(6, 5));

        assertEquals(Optional.of(new Note(6, 5)), editor.currentBeat().noteOn(6));
    }

    @Test
    void pressingAndAdvancingWritesAndMovesToTheNextBeat() {
        editing.pressFretAndAdvance(new Note(6, 3));

        assertEquals(Optional.of(new Note(6, 3)), editor.currentTrack().measure(0).beat(0).noteOn(6));
        assertEquals(1, editor.cursor().beat());
    }

    @Test
    void togglingAKeyAddsAndThenRemovesIt() {
        editor.moveTo(0, 0, 3);

        editing.toggleKey(60);
        assertEquals(Optional.of(new Note(3, 5)), editor.currentBeat().noteOn(3));

        editing.toggleKey(60);
        assertEquals(Optional.empty(), editor.currentBeat().noteOn(3));
    }

    @Test
    void pressingAKeyAndAdvancingMovesToTheNextBeat() {
        editor.moveTo(0, 0, 3);

        editing.pressKeyAndAdvance(60);

        assertEquals(1, editor.cursor().beat());
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

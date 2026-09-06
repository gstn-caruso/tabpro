package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Al cambiar la afinacion, las notas conservan su altura en vez de perderse en silencio. */
class EditorTuningTest {

    @Test
    void preservesThePitchOfABanjoNoteWhenTheTrackBecomesAGuitar() {
        Tuning banjoOpenG = Tuning.of("Banjo Open G", 62, 59, 55, 50, 67);
        Track banjo = new Track("Banjo", banjoOpenG, Channel.playing(Track.GUITAR_PROGRAM),
                List.of(Measure.empty(TimeSignature.fourFour(), Duration.quarter())));
        Editor editor = new Editor(Score.blank());
        editor.addTrack(banjo);
        moveDown(editor, 4); // de la cuerda 1 a la 5, la mas aguda del banjo (Sol, 67)
        editor.setFret(0);

        editor.setTuning(1, Tuning.standard());

        assertEquals(Tuning.standard(), editor.score().track(1).tuning());
        assertEquals(Optional.of(new Note(1, 3)), editor.currentBeat().noteOn(1));
    }

    @Test
    void relocatesAChordThatFitsWhenAGuitarBecomesABass() {
        Editor editor = new Editor(Score.blank());
        moveDown(editor, 3); // cuerda 4: Re3 (50)
        editor.setFret(0);
        editor.moveDown(); // cuerda 5: La2 (45)
        editor.setFret(0);
        editor.moveDown(); // cuerda 6: Mi2 (40)
        editor.setFret(0);

        editor.setTuning(0, Tuning.standardBass());

        Track bass = editor.score().track(0);
        assertEquals(4, bass.stringCount());
        Beat beat = editor.currentBeat();
        assertEquals(3, beat.notes().size(), "las tres notas del acorde entraban en el bajo: ninguna se pierde");
        Set<Integer> pitches = beat.notes().stream()
                .map(note -> bass.tuning().pitchOf(note).midiNumber())
                .collect(Collectors.toSet());
        assertEquals(Set.of(40, 45, 50), pitches);
    }

    @Test
    void dropsANoteThatIsTooLowForTheNewTuning() {
        Editor editor = new Editor(Score.blank());
        moveDown(editor, 5); // cuerda 6: Mi2 (40), la mas grave de la guitarra estandar
        editor.setFret(0);

        editor.setTuning(0, Tuning.of("Ukelele en Do", 69, 64, 60, 67));

        assertTrue(editor.currentBeat().notes().isEmpty(), "el Mi2 queda por debajo de la cuerda mas grave del ukelele");
    }

    @Test
    void dropsANoteThatIsTooHighForTheNewTuning() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(20); // cuerda 1 al traste 20: Do5 (84)

        editor.setTuning(0, Tuning.standardBass());

        assertTrue(editor.currentBeat().notes().isEmpty(),
                "84 no entra en ninguna cuerda del bajo aunque se use el traste maximo");
    }

    @Test
    void preservesThePitchWhenTheTuningChangesButTheStringCountDoesNot() {
        Editor editor = new Editor(Score.blank());
        moveDown(editor, 5); // cuerda 6: Mi2 (40)
        editor.setFret(0);

        editor.setTuning(0, Tuning.of("Drop D", 64, 59, 55, 50, 45, 38));

        Track track = editor.score().track(0);
        assertEquals(6, track.stringCount());
        assertEquals(Optional.of(new Note(6, 2)), editor.currentBeat().noteOn(6));
    }

    @Test
    void retuningAnEmptyTrackDoesNotExplode() {
        Editor editor = new Editor(Score.blank());

        editor.setTuning(0, Tuning.standardBass());

        assertEquals(4, editor.score().track(0).stringCount());
        assertTrue(editor.currentBeat().notes().isEmpty());
    }

    @Test
    void undoingARetuneRestoresBothTheTuningAndTheDiscardedNotes() {
        Editor editor = new Editor(Score.blank());
        moveDown(editor, 5); // cuerda 6
        editor.setFret(40); // Mi2 + 40 = Do8 (80): no entra en el bajo
        Score before = editor.score();

        editor.setTuning(0, Tuning.standardBass());
        editor.undo();

        assertEquals(before, editor.score());
    }

    @Test
    void pullsTheCursorBackToAStringThatStillExistsAfterShrinkingTheTuning() {
        Editor editor = new Editor(Score.blank());
        moveDown(editor, 5);
        assertEquals(6, editor.cursor().string());

        editor.setTuning(0, Tuning.standardBass());

        assertEquals(4, editor.cursor().string());
    }

    private static void moveDown(Editor editor, int times) {
        for (int i = 0; i < times; i++) {
            editor.moveDown();
        }
    }
}

package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EditorClipboardTest {

    private final Editor editor = new Editor(
            new Score("Prueba", 120, List.of(Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));

    @Test
    void copyingWithoutSelectionTakesTheCurrentBar() {
        editor.setFret(5);

        editor.copy(true);

        assertFalse(editor.clipboard().isEmpty());
        assertEquals(1, editor.clipboard().content().measureCount());
    }

    @Test
    void pastingABarReplacesTheOneUnderTheCursor() {
        editor.setFret(5);
        editor.copy(false);
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();
        editor.moveRight();

        editor.paste(PasteOptions.replacingOnce());

        assertEquals(Optional.of(5), editor.score().track(0).measure(1).beat(0).noteOn(1).map(note -> note.fret()));
    }

    @Test
    void pastingSeveralTimesRepeatsTheBar() {
        editor.setFret(7);
        editor.copy(false);
        editor.moveTo(0, 0, 1);

        editor.paste(new PasteOptions(true, 3));

        assertEquals(4, editor.score().track(0).measureCount());
    }

    @Test
    void copyingEveryTrackPastesEveryTrack() {
        editor.setFret(5);
        editor.selectTrack(1);
        editor.setFret(3);
        editor.selectTrack(0);

        editor.copy(true);
        editor.paste(PasteOptions.insertingOnce());

        assertEquals(Optional.of(5), editor.score().track(0).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
        assertEquals(Optional.of(3), editor.score().track(1).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
    }

    @Test
    void cuttingTakesTheBarOutOfEveryTrack() {
        editor.insertMeasure();
        editor.moveTo(1, 0, 1);
        editor.setFret(5);
        editor.selectMeasures(0, 0);

        editor.cut();

        assertEquals(1, editor.score().track(0).measureCount());
        assertEquals(Optional.of(5), editor.score().track(0).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
    }

    @Test
    void aSelectionInsideABarCopiesBeatsAndNotBars() {
        editor.setFret(5);
        editor.startSelection(false);
        editor.moveRight();
        editor.setFret(7);

        editor.copy(false);

        assertTrue(editor.clipboard().content().holdsBeats());
        assertEquals(2, editor.clipboard().content().beats().size());
    }

    @Test
    void aSelectionOfBarsCoversEverythingInBetween() {
        editor.insertMeasure();
        editor.insertMeasure();

        editor.selectMeasures(0, 2);

        Selection selection = editor.selection().orElseThrow();
        assertTrue(selection.coversMeasure(1));
        assertEquals(3, selection.measureCount());
    }
}

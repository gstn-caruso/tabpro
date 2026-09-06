package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreCanvasTest {

    private final Editor editor = new Editor(new Score("Prueba", 120, List.of(
            Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));
    private final ScoreCanvas canvas = new ScoreCanvas(editor);

    @Test
    void startsInTheMultitrackView() {
        assertTrue(canvas.isMultitrack());
    }

    @Test
    void leavingTheMultitrackViewLeavesRoomForOneTrackOnly() {
        int everyTrack = canvas.getPreferredSize().height;

        canvas.setMultitrack(false);

        assertTrue(canvas.getPreferredSize().height < everyTrack);
    }

    @Test
    void turningATrackOffLeavesTheSameRoomAsLeavingTheMultitrackView() {
        canvas.setMultitrack(false);
        int onlyTheActiveOne = canvas.getPreferredSize().height;

        canvas.setMultitrack(true);
        canvas.setTrackShown(1, false);

        assertEquals(onlyTheActiveOne, canvas.getPreferredSize().height);
    }

    @Test
    void hidingANotationMakesTheScoreShorter() {
        int both = canvas.getPreferredSize().height;

        canvas.setStandardNotationShown(false);

        assertFalse(canvas.showsStandardNotation());
        assertTrue(canvas.showsTablature());
        assertTrue(canvas.getPreferredSize().height < both);
    }

    @Test
    void hidingBothNotationsBringsTheOtherOneBack() {
        canvas.setTablatureShown(false);
        canvas.setStandardNotationShown(false);

        assertTrue(canvas.showsTablature(), "una pista sin ninguna notacion no se veria");
    }

    @Test
    void theActiveTrackIsTheOneTheCursorIsOn() {
        canvas.setMultitrack(false);
        int height = canvas.getPreferredSize().height;

        editor.selectTrack(1);

        assertTrue(canvas.getPreferredSize().height != height,
                "el bajo tiene cuatro cuerdas, asi que ocupa menos alto que la guitarra");
    }
}

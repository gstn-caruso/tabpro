package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import org.junit.jupiter.api.Test;

class BeatViewsTest {

    @Test
    void showsTheBeatUnderTheCursorWhileNothingSounds() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(7);

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, Playhead.silent()));
    }

    @Test
    void showsTheBeatThatSoundsWhileItPlays() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(5);
        editor.moveTo(0, 0, 1);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(0, 0, 1));

        assertEquals(editor.currentTrack().measure(0).beat(1), BeatViews.beatToShow(editor, playhead));
    }

    @Test
    void followsTheTrackTheCursorIsOnAndNotAnotherOne() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.setFret(9);
        editor.selectTrack(0);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, playhead));
    }

    @Test
    void fallsBackToTheCursorWhenThePlayheadPointsPastTheScore() {
        Editor editor = new Editor(Score.blank());

        Playhead stale = Playhead.silent().advancedTo(new BeatPosition(0, 9, 9));

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, stale));
    }

    @Test
    void letsYouWriteWhileNothingSounds() {
        Editor editor = new Editor(Score.blank());

        assertTrue(BeatViews.showsTheCursorBeat(editor, Playhead.silent()));
    }

    @Test
    void doesNotLetYouWriteOnABeatYouAreNotSeeing() {
        Editor editor = new Editor(Score.blank());
        editor.moveRight();

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(0, 0, 0));

        assertFalse(
                BeatViews.showsTheCursorBeat(editor, playhead),
                "mientras suena se ve el beat que suena, no el del cursor");
    }

    @Test
    void letsYouWriteWhileAnotherTrackSounds() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.selectTrack(0);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));

        assertTrue(BeatViews.showsTheCursorBeat(editor, playhead));
    }

    @Test
    void usesTheTuningOfTheTrackTheCursorIsOn() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));

        assertEquals(4, BeatViews.tuningToShow(editor).stringCount());

        editor.selectTrack(0);

        assertEquals(6, BeatViews.tuningToShow(editor).stringCount());
    }

    @Test
    void bothViewsCanBeHidden() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        assertTrue(views.isFretboardVisible());
        assertTrue(views.isKeyboardVisible());

        views.setFretboardVisible(false);
        views.setKeyboardVisible(false);

        assertFalse(views.isFretboardVisible());
        assertFalse(views.isKeyboardVisible());
    }

    @Test
    void followsTheEditorWithoutBlowingUp() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());

        editor.setFret(Note.MAX_FRET);
        editor.addTrack(Track.standardBass("Bajo"));
        editor.setFret(4);
        views.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(1, 0, 0)));

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, Playhead.silent()));
    }
}

package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import org.junit.jupiter.api.Test;

class EditorTracksTest {

    @Test
    void addsATrackAndSelectsIt() {
        Editor editor = new Editor(Score.blank());

        editor.addTrack(Track.standardBass("Bass"));

        assertEquals(2, editor.score().trackCount());
        assertEquals(1, editor.cursor().track());
        assertEquals("Bass", editor.currentTrack().name());
    }

    @Test
    void addingATrackAssignsItTheNextFreeChannelPair() {
        Editor editor = new Editor(Score.blank());

        editor.addTrack(Track.standardBass("Bass"));
        editor.addTrack(Track.standardGuitar("Guitar 2"));

        Channel first = editor.score().track(0).channel();
        Channel second = editor.score().track(1).channel();
        Channel third = editor.score().track(2).channel();
        assertEquals(1, first.number());
        assertEquals(3, second.number());
        assertEquals(5, third.number());
        assertEquals(4, second.effectChannel());
        assertEquals(6, third.effectChannel());
    }

    @Test
    void addingATrackThatForcesChannels11to16AssignsItThere() {
        Editor editor = new Editor(Score.blank());

        editor.addTrack(Track.standardBass("Bass")
                .mappingSettings(settings -> settings.withForceChannels11to16(true)));

        Channel channel = editor.score().track(1).channel();
        assertEquals(11, channel.number());
        assertEquals(12, channel.effectChannel());
    }

    @Test
    void addingAPercussionTrackKeepsItOnTheTenthChannel() {
        Editor editor = new Editor(Score.blank());

        editor.addTrack(Track.percussion("Drums"));

        Channel percussion = editor.score().track(1).channel();
        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.number());
        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.effectChannel());
    }

    @Test
    void addingATrackGivesItAsManyMeasuresAsTheScoreAlreadyHas() {
        Editor editor = new Editor(Score.blank());
        editor.insertMeasure();
        editor.insertMeasure();

        editor.addTrack(Track.standardBass("Bass"));

        assertEquals(editor.score().track(0).measureCount(), editor.currentTrack().measureCount());
    }

    @Test
    void selectsAnotherTrack() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));

        editor.selectTrack(0);

        assertEquals(0, editor.cursor().track());
    }

    @Test
    void selectingATrackPullsTheCursorBackInsideIt() {
        Editor editor = new Editor(Score.blank());
        editor.moveDown();
        editor.moveDown();
        editor.moveDown();
        editor.moveDown();
        editor.moveDown();
        assertEquals(6, editor.cursor().string());

        editor.addTrack(Track.standardBass("Bass"));

        assertEquals(4, editor.cursor().string());
    }

    @Test
    void rejectsSelectingATrackThatIsNotThere() {
        Editor editor = new Editor(Score.blank());

        assertThrows(IllegalArgumentException.class, () -> editor.selectTrack(1));
        assertThrows(IllegalArgumentException.class, () -> editor.selectTrack(-1));
    }

    @Test
    void removesTheSelectedTrackAndSelectsTheOneBefore() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));

        editor.removeCurrentTrack();

        assertEquals(1, editor.score().trackCount());
        assertEquals(0, editor.cursor().track());
        assertEquals("Guitarra", editor.currentTrack().name());
    }

    @Test
    void refusesToRemoveTheOnlyTrack() {
        Editor editor = new Editor(Score.blank());

        assertThrows(IllegalStateException.class, editor::removeCurrentTrack);
    }

    @Test
    void renamesATrack() {
        Editor editor = new Editor(Score.blank());

        editor.renameTrack(0, "Rhythm");

        assertEquals("Rhythm", editor.score().track(0).name());
    }

    @Test
    void changesTheMixerOfATrackThatIsNotTheSelectedOne() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));

        editor.setProgram(0, 30);
        editor.setVolume(0, 80);
        editor.setPan(0, 10);
        editor.toggleMute(0);
        editor.toggleSolo(0);

        Channel channel = editor.score().track(0).channel();
        assertEquals(30, channel.program());
        assertEquals(80, channel.volume());
        assertEquals(10, channel.pan());
        assertTrue(channel.muted());
        assertTrue(channel.solo());
        assertEquals(1, editor.cursor().track());
    }

    @Test
    void mixerChangesCanBeUndone() {
        Editor editor = new Editor(Score.blank());

        editor.setVolume(0, 40);
        editor.undo();

        assertEquals(Channel.DEFAULT_VOLUME, editor.score().track(0).channel().volume());
    }

    @Test
    void addingATrackCanBeUndone() {
        Editor editor = new Editor(Score.blank());

        editor.addTrack(Track.standardBass("Bass"));
        editor.undo();

        assertEquals(1, editor.score().trackCount());
        assertEquals(0, editor.cursor().track());
    }

    @Test
    void insertingAMeasureKeepsEveryTrackTheSameLength() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));

        editor.insertMeasure();

        assertEquals(2, editor.score().track(0).measureCount());
        assertEquals(2, editor.score().track(1).measureCount());
    }

    @Test
    void deletingAMeasureKeepsEveryTrackTheSameLength() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));
        editor.insertMeasure();

        editor.deleteMeasure();

        assertEquals(1, editor.score().track(0).measureCount());
        assertEquals(1, editor.score().track(1).measureCount());
    }

    @Test
    void runningPastTheEndAppendsAMeasureToEveryTrack() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));
        fillTheMeasure(editor);

        editor.moveRight();

        assertEquals(2, editor.score().track(0).measureCount());
        assertEquals(2, editor.score().track(1).measureCount());
        assertEquals(1, editor.cursor().measure());
    }

    @Test
    void aFreshScoreSelectsItsFirstTrack() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bass"));

        editor.replaceScore(Score.blank());

        assertEquals(0, editor.cursor().track());
        assertFalse(editor.canUndo());
    }

    private void fillTheMeasure(Editor editor) {
        for (int beat = 0; beat < 3; beat++) {
            editor.moveRight();
        }
    }
}

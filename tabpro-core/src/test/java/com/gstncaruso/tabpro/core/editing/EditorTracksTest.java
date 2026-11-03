package com.gstncaruso.tabpro.core.editing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TestDefaultNames;
import com.gstncaruso.tabpro.core.model.Track;
import org.junit.jupiter.api.Test;

class EditorTracksTest {

    @Test
    void addsATrackAndSelectsIt() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.addTrack(Track.standardBass("Bass"));

        assertEquals(2, editor.score().trackCount());
        assertEquals(1, editor.cursor().track());
        assertEquals("Bass", editor.currentTrack().name());
    }

    @Test
    void addingATrackAssignsItTheNextFreeChannelPair() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

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
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.addTrack(Track.standardBass("Bass")
                .mappingSettings(settings -> settings.withForceChannels11to16(true)));

        Channel channel = editor.score().track(1).channel();
        assertEquals(11, channel.number());
        assertEquals(12, channel.effectChannel());
    }

    @Test
    void addingAPercussionTrackKeepsItOnTheTenthChannel() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.addTrack(Track.percussion("Drums"));

        Channel percussion = editor.score().track(1).channel();
        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.number());
        assertEquals(Channel.PERCUSSION_CHANNEL, percussion.effectChannel());
    }

    @Test
    void addingATrackGivesItAsManyMeasuresAsTheScoreAlreadyHas() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.insertMeasure();
        editor.insertMeasure();

        editor.addTrack(Track.standardBass("Bass"));

        assertEquals(editor.score().track(0).measureCount(), editor.currentTrack().measureCount());
    }

    @Test
    void selectsAnotherTrack() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));

        editor.selectTrack(0);

        assertEquals(0, editor.cursor().track());
    }

    @Test
    void selectingATrackPullsTheCursorBackInsideIt() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
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
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        assertThrows(IllegalArgumentException.class, () -> editor.selectTrack(1));
        assertThrows(IllegalArgumentException.class, () -> editor.selectTrack(-1));
    }

    @Test
    void removesTheSelectedTrackAndSelectsTheOneBefore() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));

        editor.removeCurrentTrack();

        assertEquals(1, editor.score().trackCount());
        assertEquals(0, editor.cursor().track());
        assertEquals("Test Guitar", editor.currentTrack().name());
    }

    @Test
    void refusesToRemoveTheOnlyTrack() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        assertThrows(IllegalStateException.class, editor::removeCurrentTrack);
    }

    @Test
    void renamesATrack() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.renameTrack(0, "Rhythm");

        assertEquals("Rhythm", editor.score().track(0).name());
    }

    @Test
    void changesTheMixerOfATrackThatIsNotTheSelectedOne() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
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
    void givenTwoTracksSharingPortAndChannel_whenOneVolumeChanges_thenBothVolumesMatch() {
        Channel sharedChannel = Channel.playing(24)
                .withPort(3).withNumber(7).withEffectChannel(8).withVolume(100);
        Score score = Score.blank(new TestDefaultNames())
                .withTrack(0, Track.standardGuitar("Guitar 1").withChannel(sharedChannel))
                .withTrackAdded(Track.standardGuitar("Guitar 2").withChannel(sharedChannel));
        Editor editor = new Editor(score);

        editor.setVolume(0, 80);

        assertEquals(80, editor.score().track(0).channel().volume());
        assertEquals(80, editor.score().track(1).channel().volume());
    }

    @Test
    void givenDifferentPortsAndSameChannel_whenOneVolumeChanges_thenOtherVolumeStaysUnchanged() {
        Channel firstChannel = Channel.playing(24)
                .withPort(3).withNumber(7).withEffectChannel(8).withVolume(100);
        Channel secondChannel = Channel.playing(24)
                .withPort(4).withNumber(7).withEffectChannel(8).withVolume(100);
        Score score = Score.blank(new TestDefaultNames())
                .withTrack(0, Track.standardGuitar("Guitar 1").withChannel(firstChannel))
                .withTrackAdded(Track.standardGuitar("Guitar 2").withChannel(secondChannel));
        Editor editor = new Editor(score);

        editor.setVolume(0, 80);

        assertEquals(80, editor.score().track(0).channel().volume());
        assertEquals(100, editor.score().track(1).channel().volume());
    }

    @Test
    void givenSamePortAndDifferentChannels_whenOneVolumeChanges_thenOtherVolumeStaysUnchanged() {
        Channel firstChannel = Channel.playing(24)
                .withPort(3).withNumber(7).withEffectChannel(8).withVolume(100);
        Channel secondChannel = Channel.playing(24)
                .withPort(3).withNumber(9).withEffectChannel(10).withVolume(100);
        Score score = Score.blank(new TestDefaultNames())
                .withTrack(0, Track.standardGuitar("Guitar 1").withChannel(firstChannel))
                .withTrackAdded(Track.standardGuitar("Guitar 2").withChannel(secondChannel));
        Editor editor = new Editor(score);

        editor.setVolume(0, 80);

        assertEquals(80, editor.score().track(0).channel().volume());
        assertEquals(100, editor.score().track(1).channel().volume());
    }

    @Test
    void givenSharedChannelTracks_whenVolumeChangeIsUndone_thenBothVolumesAreRestored() {
        Channel sharedChannel = Channel.playing(24)
                .withPort(3).withNumber(7).withEffectChannel(8).withVolume(100);
        Score score = Score.blank(new TestDefaultNames())
                .withTrack(0, Track.standardGuitar("Guitar 1").withChannel(sharedChannel))
                .withTrackAdded(Track.standardGuitar("Guitar 2").withChannel(sharedChannel));
        Editor editor = new Editor(score);

        editor.setVolume(0, 80);
        editor.undo();

        assertEquals(100, editor.score().track(0).channel().volume());
        assertEquals(100, editor.score().track(1).channel().volume());
    }

    @Test
    void mixerChangesCanBeUndone() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.setVolume(0, 40);
        editor.undo();

        assertEquals(Channel.DEFAULT_VOLUME, editor.score().track(0).channel().volume());
    }

    @Test
    void addingATrackCanBeUndone() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));

        editor.addTrack(Track.standardBass("Bass"));
        editor.undo();

        assertEquals(1, editor.score().trackCount());
        assertEquals(0, editor.cursor().track());
    }

    @Test
    void insertingAMeasureKeepsEveryTrackTheSameLength() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));

        editor.insertMeasure();

        assertEquals(2, editor.score().track(0).measureCount());
        assertEquals(2, editor.score().track(1).measureCount());
    }

    @Test
    void deletingAMeasureKeepsEveryTrackTheSameLength() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));
        editor.insertMeasure();

        editor.deleteMeasure();

        assertEquals(1, editor.score().track(0).measureCount());
        assertEquals(1, editor.score().track(1).measureCount());
    }

    @Test
    void runningPastTheEndAppendsAMeasureToEveryTrack() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));
        fillTheMeasure(editor);

        editor.moveRight();

        assertEquals(2, editor.score().track(0).measureCount());
        assertEquals(2, editor.score().track(1).measureCount());
        assertEquals(1, editor.cursor().measure());
    }

    @Test
    void aFreshScoreSelectsItsFirstTrack() {
        Editor editor = new Editor(Score.blank(new TestDefaultNames()));
        editor.addTrack(Track.standardBass("Bass"));

        editor.replaceScore(Score.blank(new TestDefaultNames()));

        assertEquals(0, editor.cursor().track());
        assertFalse(editor.canUndo());
    }

    private void fillTheMeasure(Editor editor) {
        for (int beat = 0; beat < 3; beat++) {
            editor.moveRight();
        }
    }
}

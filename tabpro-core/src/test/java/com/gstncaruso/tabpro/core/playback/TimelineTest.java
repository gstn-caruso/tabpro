package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimelineTest {

    @Test
    void aRestOnlyScoreHasBeatsButNoNotes() {
        Timeline timeline = Timeline.of(Score.blank());
        TrackTimeline track = timeline.tracks().get(0);
        assertTrue(track.notes().isEmpty());
        assertEquals(1, track.beats().size());
    }

    @Test
    void schedulesASingleNoteAtTickZeroWithItsDuration() {
        Score score = Score.blank().withTrack(0,
                Track.standardGuitar("Guitarra").withMeasure(0,
                        new Measure(TimeSignature.fourFour(),
                                List.of(Beat.of(Duration.quarter(), new Note(1, 0))))));

        TrackTimeline track = Timeline.of(score).tracks().get(0);

        assertEquals(1, track.notes().size());
        ScheduledNote note = track.notes().get(0);
        assertEquals(0, note.startTick());
        assertEquals(Duration.quarter().ticks(), note.durationTicks());
    }

    @Test
    void usesTheTuningToResolvePitches() {
        Score score = Score.blank().withTrack(0,
                Track.standardGuitar("Guitarra").withMeasure(0,
                        new Measure(TimeSignature.fourFour(),
                                List.of(Beat.of(Duration.quarter(), new Note(6, 3))))));

        ScheduledNote note = Timeline.of(score).tracks().get(0).notes().get(0);

        assertEquals(new Pitch(43), note.pitch());
    }

    @Test
    void schedulesChordNotesAtTheSameTick() {
        Score score = Score.blank().withTrack(0,
                Track.standardGuitar("Guitarra").withMeasure(0,
                        new Measure(TimeSignature.fourFour(),
                                List.of(Beat.of(Duration.quarter(), new Note(1, 0), new Note(2, 1))))));

        List<ScheduledNote> notes = Timeline.of(score).tracks().get(0).notes();

        assertEquals(2, notes.size());
        assertEquals(0, notes.get(0).startTick());
        assertEquals(0, notes.get(1).startTick());
    }

    @Test
    void advancesTicksAcrossBeatsAndMeasures() {
        Track track = Track.standardGuitar("Guitarra")
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.quarter(), new Note(1, 0)),
                                Beat.of(Duration.quarter(), new Note(1, 1)))))
                .withMeasureInsertedAt(1, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.quarter(), new Note(1, 2)))));
        Score score = Score.blank().withTrack(0, track);

        List<ScheduledNote> notes = Timeline.of(score).tracks().get(0).notes();

        long quarter = Duration.quarter().ticks();
        assertEquals(0, notes.get(0).startTick());
        assertEquals(quarter, notes.get(1).startTick());
        assertEquals(quarter * 2, notes.get(2).startTick());
    }

    @Test
    void schedulesABeatMarkerPerBeatIncludingRests() {
        Track track = Track.standardGuitar("Guitarra")
                .withMeasure(0, new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.quarter(), new Note(1, 0)),
                                Beat.rest(Duration.quarter()))));
        Score score = Score.blank().withTrack(0, track);

        List<ScheduledBeat> beats = Timeline.of(score).tracks().get(0).beats();

        assertEquals(List.of(new ScheduledBeat(0, 0, 0), new ScheduledBeat(Duration.quarter().ticks(), 0, 1)),
                beats);
    }

    @Test
    void carriesTempoAndTheChannelSettingsOfEachTrack() {
        Score score = Score.blank().withTempo(90);
        Track loud = score.track(0).withChannel(score.track(0).channel().withVolume(110).withPan(20));

        Timeline timeline = Timeline.of(score.withTrack(0, loud));

        assertEquals(90, timeline.tempoBpm());
        TrackTimeline track = timeline.tracks().get(0);
        assertEquals(25, track.program());
        assertEquals(110, track.volume());
        assertEquals(20, track.pan());
    }

    @Test
    void aMutedTrackKeepsItsNotesButIsScheduledSilent() {
        Score score = Score.blank().withTrack(0,
                Track.standardGuitar("Guitarra").withMeasure(0,
                        new Measure(TimeSignature.fourFour(),
                                List.of(Beat.of(Duration.quarter(), new Note(1, 0))))));
        Track muted = score.track(0).withChannel(score.track(0).channel().toggledMute());

        TrackTimeline track = Timeline.of(score.withTrack(0, muted)).tracks().get(0);

        assertEquals(0, track.volume());
        assertEquals(1, track.notes().size());
        assertEquals(1, track.beats().size());
    }

    @Test
    void aTrackThatIsNotSoloIsScheduledSilentWhileAnotherPlaysSolo() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));
        Track soloed = score.track(1).withChannel(score.track(1).channel().toggledSolo());

        Timeline timeline = Timeline.of(score.withTrack(1, soloed));

        assertEquals(0, timeline.tracks().get(0).volume());
        assertEquals(Channel.DEFAULT_VOLUME, timeline.tracks().get(1).volume());
    }

    @Test
    void keepsOneTimelinePerTrack() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        assertEquals(2, Timeline.of(score).tracks().size());
    }

    @Test
    void endTickIsTheEndOfTheLongestTrack() {
        Track shortTrack = Track.standardGuitar("Corta").withMeasure(0,
                new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0)))));
        Track longTrack = Track.standardGuitar("Larga").withMeasure(0,
                new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(new Duration(NoteValue.WHOLE, false), new Note(1, 0)))));
        Score score = new Score("", 120, List.of(shortTrack, longTrack));

        long endTick = Timeline.of(score).endTick();

        assertEquals(new Duration(NoteValue.WHOLE, false).ticks(), endTick);
    }
}

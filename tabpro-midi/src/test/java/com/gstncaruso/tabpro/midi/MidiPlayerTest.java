package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.Velocity;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PitchTrajectory;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MidiPlayerTest {

    private Sequencer sequencer;
    private MidiPlayer player;

    @BeforeEach
    void setUp() {
        try {
            sequencer = MidiSystem.getSequencer(false);
        } catch (MidiUnavailableException e) {
            Assumptions.assumeTrue(false, "sin sequencer MIDI");
            return;
        }
        player = new MidiPlayer(sequencer);
    }

    @AfterEach
    void tearDown() {
        if (player != null) {
            player.close();
        }
    }

    @Test
    void soundsASingleNoteThroughItsReceiver() {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();
        MidiPlayer withFakeSynth = new MidiPlayer(sequencer, () -> receiverInto(received));

        withFakeSynth.playNote(new Pitch(60), 25);

        assertTrue(
                received.stream().anyMatch(message ->
                        message.getCommand() == ShortMessage.NOTE_ON && message.getData1() == 60),
                "la nota no llego al sintetizador");
        withFakeSynth.close();
    }

    /**
     * El secuenciador tiene que sonar por el mismo receiver que la preview, para que un banco
     * SoundFont cargado ahi se escuche en la partitura entera y no solo al escribir una nota.
     */
    @Test
    void playingTheTimelineReachesTheSameReceiverAsThePreview() throws InterruptedException {
        List<ShortMessage> received = new CopyOnWriteArrayList<>();
        MidiPlayer withFakeSynth = new MidiPlayer(sequencer, () -> receiverInto(received));
        CountDownLatch latch = new CountDownLatch(1);

        withFakeSynth.play(shortTimeline(), new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
            }

            @Override
            public void playbackFinished() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(
                received.stream().anyMatch(message -> message.getCommand() == ShortMessage.NOTE_ON),
                "la partitura no sono por el receiver del banco de sonido");
        withFakeSynth.close();
    }

    @Test
    void isNotPlayingBeforeStart() {
        assertFalse(player.isPlaying());
    }

    @Test
    void isPlayingAfterPlay() {
        player.play(shortTimeline(), noOpListener());
        assertTrue(player.isPlaying());
    }

    @Test
    void stopsWhenAsked() {
        player.play(shortTimeline(), noOpListener());
        player.stop();
        assertFalse(player.isPlaying());
    }

    @Test
    void notifiesBeatsInOrder() throws InterruptedException {
        List<BeatPosition> received = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(4);
        player.play(shortTimeline(), new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
                received.add(position);
                latch.countDown();
            }

            @Override
            public void playbackFinished() {
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(
                new BeatPosition(0, 0, 0),
                new BeatPosition(0, 0, 1),
                new BeatPosition(0, 0, 2),
                new BeatPosition(0, 0, 3)), received);
    }

    @Test
    void notifiesWhenTheSequenceEnds() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        player.play(shortTimeline(), new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
            }

            @Override
            public void playbackFinished() {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void restartsFromTheBeginningOnASecondPlay() throws InterruptedException {
        CountDownLatch finishedLatch = new CountDownLatch(1);
        player.play(shortTimeline(), new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
            }

            @Override
            public void playbackFinished() {
                finishedLatch.countDown();
            }
        });
        assertTrue(finishedLatch.await(5, TimeUnit.SECONDS));

        CountDownLatch firstBeatLatch = new CountDownLatch(1);
        AtomicReference<BeatPosition> firstBeat = new AtomicReference<>();
        player.play(shortTimeline(), new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
                if (firstBeat.compareAndSet(null, position)) {
                    firstBeatLatch.countDown();
                }
            }

            @Override
            public void playbackFinished() {
            }
        });

        assertTrue(firstBeatLatch.await(5, TimeUnit.SECONDS));
        assertEquals(new BeatPosition(0, 0, 0), firstBeat.get());
    }

    @Test
    void aPortThatLimitsPitchVariationSilencesABendBeyondOneTone() {
        player.useLimitPitchVariation(1, true);

        player.play(timelineWithABigBendOnPort(1), noOpListener());

        assertTrue(pitchBendEvents(player.sequenceInPlay().getTracks()[1]).isEmpty());
    }

    @Test
    void withoutLimitingPitchVariationTheSamePortStillBends() {
        player.play(timelineWithABigBendOnPort(1), noOpListener());

        assertFalse(pitchBendEvents(player.sequenceInPlay().getTracks()[1]).isEmpty());
    }

    @Test
    void tracksOfAnotherPortDoNotReachTheMainSequence() {
        TrackTimeline enElPuertoUno = new TrackTimeline(25, 100, 64, false, 1, List.of(), List.of(), List.of());
        TrackTimeline enElPuertoDos = new TrackTimeline(30, 100, 64, false, 2, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(enElPuertoUno, enElPuertoDos));

        player.play(timeline, noOpListener());

        javax.sound.midi.Sequence mainSequence = player.sequenceInPlay();
        assertEquals(2, mainSequence.getTracks().length, "solo el conductor y la pista del puerto 1");
        assertEquals(25, programOf(mainSequence.getTracks()[1]));
    }

    @Test
    void aScoreWithoutTracksOnTheFirstPortStillPlaysWithoutFailing() {
        TrackTimeline enElPuertoDos = new TrackTimeline(25, 100, 64, false, 2, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(enElPuertoDos));

        player.play(timeline, noOpListener());

        assertTrue(player.isPlaying());
    }

    private Timeline timelineWithABigBendOnPort(int port) {
        PitchTrajectory bigBend = PitchTrajectory.ramp(0, 0.0, 960, 3.0);
        ScheduledNote note = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100), bigBend, false);
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, port, List.of(note), List.of(), List.of());
        return new Timeline(120, 960, List.of(trackTimeline));
    }

    private List<ShortMessage> pitchBendEvents(javax.sound.midi.Track track) {
        return java.util.stream.IntStream.range(0, track.size())
                .mapToObj(track::get)
                .map(javax.sound.midi.MidiEvent::getMessage)
                .filter(message -> message instanceof ShortMessage sm && sm.getCommand() == ShortMessage.PITCH_BEND)
                .map(message -> (ShortMessage) message)
                .toList();
    }

    private int programOf(javax.sound.midi.Track track) {
        return ((ShortMessage) track.get(0).getMessage()).getData1();
    }

    private static Receiver receiverInto(List<ShortMessage> received) {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                received.add((ShortMessage) message);
            }

            @Override
            public void close() {
            }
        };
    }

    private Timeline shortTimeline() {
        Duration sixteenth = new Duration(NoteValue.SIXTEENTH, false);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(sixteenth, new Note(1, 0)),
                Beat.of(sixteenth, new Note(1, 1)),
                Beat.of(sixteenth, new Note(1, 2)),
                Beat.of(sixteenth, new Note(1, 3))));
        Track track = Track.standardGuitar("Guitarra").withMeasure(0, measure);
        Score score = Score.blank().withTempo(600).withTrack(0, track);
        return Timeline.of(score);
    }

    private PlaybackListener noOpListener() {
        return new PlaybackListener() {
            @Override
            public void beatStarted(BeatPosition position) {
            }

            @Override
            public void playbackFinished() {
            }
        };
    }
}

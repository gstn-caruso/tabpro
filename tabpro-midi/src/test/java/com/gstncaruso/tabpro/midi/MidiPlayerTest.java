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
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Timeline;
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

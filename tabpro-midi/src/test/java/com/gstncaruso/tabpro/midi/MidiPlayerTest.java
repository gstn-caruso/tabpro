package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import com.gstncaruso.tabpro.core.playback.ScheduledBeat;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.ScheduledNote;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.core.playback.TrackTimeline;
import java.util.List;
import java.util.Optional;
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
        MidiPlayer withFakeSynth = new MidiPlayer(sequencer, port -> receiverInto(received));

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
        MidiPlayer withFakeSynth = new MidiPlayer(sequencer, port -> receiverInto(received));
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

    /**
     * El test que hace falta no es "el player recibio la orden de saltar" sino "despues de
     * saltar, lo que suena es el compas pedido": el primer compas es una nota larga que a este
     * tempo tardaria cuatro segundos en terminar sola; si el salto funciona, la nota del segundo
     * compas se escucha mucho antes de eso.
     */
    @Test
    void afterSeekingWhatSoundsIsTheRequestedMeasure() throws Exception {
        long measureTicks = 4L * Duration.TICKS_PER_QUARTER;
        ScheduledNote firstMeasure = new ScheduledNote(0, measureTicks, new Pitch(60));
        ScheduledNote secondMeasure = new ScheduledNote(measureTicks, measureTicks, new Pitch(72));
        TrackTimeline trackTimeline = new TrackTimeline(25, 100, 64, false, 1,
                List.of(firstMeasure, secondMeasure),
                List.of(new ScheduledBeat(0, 0, 0), new ScheduledBeat(measureTicks, 1, 0)), List.of());
        Timeline timeline = new Timeline(60, Duration.TICKS_PER_QUARTER, List.of(trackTimeline));

        CountDownLatch secondMeasureSounded = new CountDownLatch(1);
        player.open();
        sequencer.getTransmitter().setReceiver(new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
                if (message instanceof ShortMessage sm
                        && sm.getCommand() == ShortMessage.NOTE_ON && sm.getData1() == 72) {
                    secondMeasureSounded.countDown();
                }
            }

            @Override
            public void close() {
            }
        });

        player.play(timeline, noOpListener());
        player.seekTo(measureTicks);

        assertTrue(
                secondMeasureSounded.await(2, TimeUnit.SECONDS),
                "el segundo compas -de cuatro segundos de largo el primero- tendria que sonar bien antes");
    }

    /**
     * La reproduccion arma una secuencia por puerto MIDI, con un secuenciador por puerto: el
     * salto tiene que alcanzarlos a todos, no solo al principal, o el puerto secundario se queda
     * sonando donde estaba antes de saltar.
     */
    /**
     * El puerto secundario arma su propio secuenciador conectado al sintetizador del sistema, y
     * en una maquina sin placa de sonido -la CI, por ejemplo- ese secuenciador no existe y el
     * puerto queda mudo. Para que el test hable del salto y no de si la maquina tiene sonido, se
     * le inyecta la misma clase de secuenciador suelto que ya usa el puerto principal.
     */
    @Test
    void seekingReachesEverySequencerNotJustThePrimaryPort() {
        player = new MidiPlayer(sequencer, port -> silentReceiver(), MidiPlayerTest::unconnectedSequencer);
        TrackTimeline enElPuertoUno = new TrackTimeline(25, 100, 64, false, 1,
                List.of(new ScheduledNote(0, 4L * Duration.TICKS_PER_QUARTER, new Pitch(60))),
                List.of(new ScheduledBeat(0, 0, 0)), List.of());
        TrackTimeline enElPuertoDos = new TrackTimeline(30, 100, 64, false, 2,
                List.of(new ScheduledNote(0, 4L * Duration.TICKS_PER_QUARTER, new Pitch(60))),
                List.of(new ScheduledBeat(0, 0, 0)), List.of());
        Timeline timeline = new Timeline(120, Duration.TICKS_PER_QUARTER, List.of(enElPuertoUno, enElPuertoDos));
        long target = 2L * Duration.TICKS_PER_QUARTER;

        player.play(timeline, noOpListener());
        player.seekTo(target);
        player.stop();

        assertTrue(Math.abs(player.tickPositionOfPort(1) - target) < 100, "el puerto principal no salto");
        assertTrue(Math.abs(player.tickPositionOfPort(2) - target) < 100, "el puerto secundario no salto");
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

    /**
     * El camino sin ningun banco de sonido, con dos puertos usando cada uno su propio
     * sintetizador interno: el que mas importa probar de verdad, porque es el de casi todos los
     * usuarios (y el del CI, que no tiene ningun SoundFont instalado). Corre siempre, sin
     * Assumptions: apunta el banco directamente a "ningun archivo".
     */
    @Test
    void withoutAnySoundFontBothPortsStillPlayThroughTheirOwnInternalSynth() {
        SoundFontBank bank = new SoundFontBank(Optional.empty());
        MidiPlayer withBank = new MidiPlayer(sequencer, bank::receiverForPort);
        TrackTimeline enElPuertoUno = new TrackTimeline(25, 100, 64, false, 1, List.of(), List.of(), List.of());
        TrackTimeline enElPuertoDos = new TrackTimeline(30, 100, 64, false, 2, List.of(), List.of(), List.of());
        Timeline timeline = new Timeline(120, 960, List.of(enElPuertoUno, enElPuertoDos));

        assertDoesNotThrow(() -> withBank.play(timeline, noOpListener()));

        assertTrue(withBank.isPlaying());
        withBank.close();
        bank.close();
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

    /** Un secuenciador que no pide sintetizador: el unico que una maquina sin sonido puede dar. */
    private static Sequencer unconnectedSequencer() {
        try {
            return MidiSystem.getSequencer(false);
        } catch (MidiUnavailableException e) {
            return null;
        }
    }

    private static Receiver silentReceiver() {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
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

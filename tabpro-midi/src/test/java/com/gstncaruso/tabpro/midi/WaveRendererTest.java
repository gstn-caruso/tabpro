package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * El render fuera de tiempo real de "File > Export > Wave". No depende de ningun dispositivo
 * de audio real: Gervill sintetiza puramente en software, asi que corre headless.
 */
class WaveRendererTest {

    private static final int TICKS_PER_QUARTER = 480;

    private final WaveRenderer renderer = new WaveRenderer(WaveRendererTest::systemSynthesizer);

    @Test
    void rendersAPlayableWaveFileWithTheRequestedFormatAndDuration(@TempDir Path tempDir) throws Exception {
        Sequence sequence = twoQuarterNoteSequenceAt(120);
        Path path = tempDir.resolve("prueba.wav");

        renderer.render(sequence, path, AudioQuality.standard());

        assertTrue(Files.exists(path));
        try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
            assertEquals(44_100f, in.getFormat().getSampleRate());
            assertEquals(16, in.getFormat().getSampleSizeInBits());
            assertEquals(2, in.getFormat().getChannels());

            double seconds = in.getFrameLength() / in.getFormat().getSampleRate();
            assertEquals(1.0, seconds, 0.05, "dos negras a 120 bpm duran un segundo");

            assertTrue(hasSound(in), "el wave no puede ser puro silencio: las notas tienen que sonar");
        }
    }

    @Test
    void rendersWithTheRequestedSampleRateBitDepthAndChannels(@TempDir Path tempDir) throws Exception {
        Sequence sequence = twoQuarterNoteSequenceAt(120);
        Path path = tempDir.resolve("prueba.wav");
        AudioQuality quality = new AudioQuality(48_000, 24, 1);

        renderer.render(sequence, path, quality);

        try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
            assertEquals(48_000f, in.getFormat().getSampleRate());
            assertEquals(24, in.getFormat().getSampleSizeInBits());
            assertEquals(1, in.getFormat().getChannels());
        }
    }

    @Test
    void failsClearlyWhenTheSynthesizerCannotRenderOffline(@TempDir Path tempDir) throws Exception {
        Synthesizer notAnAudioSynthesizer = (Synthesizer) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {Synthesizer.class}, doNothing());
        WaveRenderer withoutOfflineSupport = new WaveRenderer(() -> notAnAudioSynthesizer);
        Sequence sequence = twoQuarterNoteSequenceAt(120);
        Path path = tempDir.resolve("prueba.wav");

        assertThrows(ScoreFileException.class, () -> withoutOfflineSupport.render(sequence, path, AudioQuality.standard()));
    }

    private static boolean hasSound(AudioInputStream in) throws Exception {
        byte[] buffer = in.readAllBytes();
        for (byte b : buffer) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }

    private static InvocationHandler doNothing() {
        return (proxy, method, args) -> null;
    }

    private static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Dos negras seguidas (do, mi) con guitarra: a 120 bpm duran un segundo exacto. */
    private static Sequence twoQuarterNoteSequenceAt(int bpm) throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_QUARTER);
        Track conductor = sequence.createTrack();
        conductor.add(new MidiEvent(new MetaMessage(0x51, tempoDataFor(bpm), 3), 0));

        Track notes = sequence.createTrack();
        notes.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 0, 25, 0), 0));
        notes.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 0));
        notes.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), TICKS_PER_QUARTER));
        notes.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, 100), TICKS_PER_QUARTER));
        notes.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, 0), 2L * TICKS_PER_QUARTER));
        conductor.add(new MidiEvent(new MetaMessage(0x2F, new byte[0], 0), 2L * TICKS_PER_QUARTER));
        return sequence;
    }

    private static byte[] tempoDataFor(int bpm) {
        int microsecondsPerQuarter = 60_000_000 / bpm;
        return new byte[] {
            (byte) (microsecondsPerQuarter >> 16), (byte) (microsecondsPerQuarter >> 8), (byte) microsecondsPerQuarter
        };
    }
}

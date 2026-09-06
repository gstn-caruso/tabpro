package com.gstncaruso.tabpro.format.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreExporter;
import com.gstncaruso.tabpro.midi.WaveRenderer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * FileExchange junta MidiScoreExporter (Score -&gt; Sequence) con WaveRenderer (Sequence -&gt;
 * WAVE): esto prueba que la union funciona de punta a punta, con el sintetizador real del JDK
 * (Gervill), que no necesita hardware de audio y corre headless.
 */
class FileExchangeTest {

    private final FileExchange exchange = new FileExchange(new WaveRenderer(FileExchangeTest::systemSynthesizer));

    @Test
    void exportsAWaveFileThatMatchesTheScoresDurationAndQuality(@TempDir Path tempDir) throws Exception {
        Score score = scoreOfTwoMeasuresOfQuarterNotes();
        Path path = tempDir.resolve("prueba.wav");
        AudioQuality quality = new AudioQuality(44_100, 16, 2);

        exchange.exportWave(score, path, quality);

        assertTrue(Files.exists(path));
        double expectedSeconds = new MidiScoreExporter().toSequence(score).getMicrosecondLength() / 1_000_000.0;
        try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
            assertEquals(44_100f, in.getFormat().getSampleRate());
            assertEquals(16, in.getFormat().getSampleSizeInBits());
            assertEquals(2, in.getFormat().getChannels());
            double actualSeconds = in.getFrameLength() / in.getFormat().getSampleRate();
            assertEquals(expectedSeconds, actualSeconds, 0.05, "el wave tiene que durar lo que dura la partitura");
        }
    }

    private static Score scoreOfTwoMeasuresOfQuarterNotes() {
        Beat quarter = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(quarter, quarter, quarter, quarter));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure, measure));
        return new Score("Prueba", 120, List.of(track));
    }

    private static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }
}

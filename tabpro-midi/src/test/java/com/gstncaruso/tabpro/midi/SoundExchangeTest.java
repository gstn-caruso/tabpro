package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SoundExchangeTest {

    private final SoundExchange exchange = new SoundExchange(new WaveRenderer(SoundExchangeTest::systemSynthesizer));

    @Test
    void exportsAWaveFileThatMatchesTheScoresDurationAndQuality(@TempDir Path tempDir) throws Exception {
        Score score = scoreOfTwoMeasuresOfQuarterNotes();
        Path path = tempDir.resolve("test.wav");
        AudioQuality quality = new AudioQuality(44_100, 16, 2);

        exchange.exportWave(score, path, quality);

        assertTrue(Files.exists(path));
        double expectedSeconds = new MidiScoreExporter().toSequence(score).getMicrosecondLength() / 1_000_000.0;
        try (AudioInputStream in = AudioSystem.getAudioInputStream(path.toFile())) {
            assertEquals(44_100f, in.getFormat().getSampleRate());
            assertEquals(16, in.getFormat().getSampleSizeInBits());
            assertEquals(2, in.getFormat().getChannels());
            double actualSeconds = in.getFrameLength() / in.getFormat().getSampleRate();
            assertEquals(expectedSeconds, actualSeconds, 0.05, "the wave has to last as long as the score");
        }
    }

    static Stream<Arguments> theOperationsThatBelongToOtherExchanges() {
        return Stream.of(
                operation(ScoreOperation.IMPORT_MIDI, () -> exchangeWithoutSound().importMidi(null)),
                operation(ScoreOperation.IMPORT_MIDI, () -> exchangeWithoutSound().midiTracksIn(null)),
                operation(ScoreOperation.IMPORT_MIDI, () -> exchangeWithoutSound()
                        .importMidiQuick(null, List.of(), false, Optional.empty(), Optional.empty(), false)),
                operation(ScoreOperation.IMPORT_MIDI, () -> exchangeWithoutSound()
                        .importMidiInto(null, null, List.of(), false, Optional.empty(), Optional.empty())),
                operation(ScoreOperation.IMPORT_MIDI,
                        () -> exchangeWithoutSound().importMidiTitleAndTimeSignatures(null, null)),
                operation(ScoreOperation.IMPORT_MIDI, () -> exchangeWithoutSound().midiTrackTimeline(null, List.of())),
                operation(ScoreOperation.IMPORT_ASCII, () -> exchangeWithoutSound().importAscii(null)),
                operation(ScoreOperation.EXPORT_ASCII, () -> exchangeWithoutSound().exportAscii(null, null)),
                operation(ScoreOperation.IMPORT_ASCII,
                        () -> exchangeWithoutSound().importAsciiInto(null, "", Optional.empty(), 4)),
                operation(ScoreOperation.EXPORT_ASCII, () -> exchangeWithoutSound().previewAscii(null, 80)),
                operation(ScoreOperation.EXPORT_ASCII, () -> exchangeWithoutSound().exportAscii(null, null, 80)),
                operation(ScoreOperation.IMPORT_MUSIC_XML, () -> exchangeWithoutSound().importMusicXml(null)),
                operation(ScoreOperation.EXPORT_MUSIC_XML, () -> exchangeWithoutSound().exportMusicXml(null, null)),
                operation(ScoreOperation.OPEN_GUITAR_PRO, () -> exchangeWithoutSound().importGuitarPro(null)),
                operation(ScoreOperation.OPEN_TAB_EDIT, () -> exchangeWithoutSound().importTabEdit(null)),
                operation(ScoreOperation.EXPORT_GUITAR_PRO, () -> exchangeWithoutSound().exportGuitarPro(null, null)),
                operation(ScoreOperation.EXPORT_GUITAR_PRO,
                        () -> exchangeWithoutSound().guitarProExportWarnings(null)),
                operation(ScoreOperation.IMPORT_POWER_TAB, () -> exchangeWithoutSound().importPowerTab(null)));
    }

    @ParameterizedTest
    @MethodSource
    void theOperationsThatBelongToOtherExchanges(ScoreOperation operation, Executable call) {
        ScoreFileException failure = assertThrows(ScoreFileException.class, call);

        assertEquals(ScoreFileProblem.NOT_SUPPORTED, failure.problem());
        assertEquals(List.of(operation), failure.arguments());
    }

    private static Arguments operation(ScoreOperation operation, Executable call) {
        return Arguments.of(operation, call);
    }

    private static SoundExchange exchangeWithoutSound() {
        return new SoundExchange(new WaveRenderer(() -> null));
    }

    private static Score scoreOfTwoMeasuresOfQuarterNotes() {
        Beat quarter = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(quarter, quarter, quarter, quarter));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure, measure));
        return new Score("Test", 120, List.of(track));
    }

    private static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }
}

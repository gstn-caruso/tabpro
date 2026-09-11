package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CombinedExchangeTest {

    private final List<String> notationCalls = new ArrayList<>();
    private final List<String> soundCalls = new ArrayList<>();
    private final ScoreExchange exchange = new CombinedExchange(notation(), sound());

    @Test
    void sendsTheSoundFormatsToTheSoundSide() {
        exchange.exportMidi(Score.blank(), Path.of("test.mid"));
        exchange.exportWave(Score.blank(), Path.of("test.wav"), AudioQuality.standard());

        assertEquals(List.of("exportMidi", "exportWave"), soundCalls);
        assertEquals(List.of(), notationCalls);
    }

    @Test
    void sendsTheNotationFormatsToTheNotationSide() {
        exchange.importMidi(Path.of("other.mid"));
        exchange.importGuitarPro(Path.of("other.gp5"));
        exchange.exportMusicXml(Score.blank(), Path.of("test.xml"));

        assertEquals(List.of("importMidi", "importGuitarPro", "exportMusicXml"), notationCalls);
        assertEquals(List.of(), soundCalls);
    }

    private ScoreExchange notation() {
        return new ScoreExchange() {
            @Override
            public Score importMidi(Path path) {
                notationCalls.add("importMidi");
                return Score.blank();
            }

            @Override
            public Score importGuitarPro(Path path) {
                notationCalls.add("importGuitarPro");
                return Score.blank();
            }

            @Override
            public void exportMusicXml(Score score, Path path) {
                notationCalls.add("exportMusicXml");
            }

            @Override
            public void exportMidi(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public List<MidiTrackInfo> midiTracksIn(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMidiQuick(
                    Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
                    Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
                    boolean useTwoChannelsPerTrack) {
                throw irrelevantForThisTest();
            }

            @Override
            public Track importMidiInto(
                    Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
                    Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportWave(Score score, Path path, AudioQuality quality) {
                throw irrelevantForThisTest();
            }

            @Override
            public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importAscii(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportAscii(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Track importAsciiInto(
                    Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
                throw irrelevantForThisTest();
            }

            @Override
            public String previewAscii(Track track, int columnsPerLine) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportAscii(Track track, Path path, int columnsPerLine) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMusicXml(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importTabEdit(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportGuitarPro(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public List<String> guitarProExportWarnings(Score score) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importPowerTab(Path path) {
                throw irrelevantForThisTest();
            }
        };
    }

    private ScoreExchange sound() {
        return new ScoreExchange() {
            @Override
            public void exportMidi(Score score, Path path) {
                soundCalls.add("exportMidi");
            }

            @Override
            public void exportWave(Score score, Path path, AudioQuality quality) {
                soundCalls.add("exportWave");
            }

            @Override
            public Score importMidi(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public List<MidiTrackInfo> midiTracksIn(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMidiQuick(
                    Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
                    Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
                    boolean useTwoChannelsPerTrack) {
                throw irrelevantForThisTest();
            }

            @Override
            public Track importMidiInto(
                    Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
                    Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importAscii(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportAscii(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Track importAsciiInto(
                    Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
                throw irrelevantForThisTest();
            }

            @Override
            public String previewAscii(Track track, int columnsPerLine) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportAscii(Track track, Path path, int columnsPerLine) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importMusicXml(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportMusicXml(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importGuitarPro(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importTabEdit(Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public void exportGuitarPro(Score score, Path path) {
                throw irrelevantForThisTest();
            }

            @Override
            public List<String> guitarProExportWarnings(Score score) {
                throw irrelevantForThisTest();
            }

            @Override
            public Score importPowerTab(Path path) {
                throw irrelevantForThisTest();
            }
        };
    }

    private static UnsupportedOperationException irrelevantForThisTest() {
        return new UnsupportedOperationException("a method this test does not exercise");
    }
}

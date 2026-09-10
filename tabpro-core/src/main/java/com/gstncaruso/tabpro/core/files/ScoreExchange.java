package com.gstncaruso.tabpro.core.files;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ScoreExchange {

    ScoreExchange NONE = new ScoreExchange() {
        @Override
        public Score importMidi(Path path) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public void exportMidi(Score score, Path path) {
            throw notSupported("la exportación a MIDI");
        }

        @Override
        public List<MidiTrackInfo> midiTracksIn(Path path) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public Score importMidiQuick(
                Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
                Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
                boolean useTwoChannelsPerTrack) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public Track importMidiInto(
                Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
                Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public void exportWave(Score score, Path path, AudioQuality quality) {
            throw notSupported("la exportación a WAVE");
        }

        @Override
        public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public Score importAscii(Path path) {
            throw notSupported("la importación de tablatura ASCII");
        }

        @Override
        public void exportAscii(Score score, Path path) {
            throw notSupported("la exportación a tablatura ASCII");
        }

        @Override
        public Track importAsciiInto(
                Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
            throw notSupported("la importación de tablatura ASCII");
        }

        @Override
        public String previewAscii(Track track, int columnsPerLine) {
            throw notSupported("la exportación a tablatura ASCII");
        }

        @Override
        public void exportAscii(Track track, Path path, int columnsPerLine) {
            throw notSupported("la exportación a tablatura ASCII");
        }

        @Override
        public Score importMusicXml(Path path) {
            throw notSupported("la importación de MusicXML");
        }

        @Override
        public void exportMusicXml(Score score, Path path) {
            throw notSupported("la exportación a MusicXML");
        }

        @Override
        public Score importGuitarPro(Path path) {
            throw notSupported("la apertura de archivos de Guitar Pro");
        }

        @Override
        public Score importTabEdit(Path path) {
            throw notSupported("la apertura de archivos de TablEdit");
        }

        @Override
        public void exportGuitarPro(Score score, Path path) {
            throw notSupported("la exportación a Guitar Pro");
        }

        @Override
        public List<String> guitarProExportWarnings(Score score) {
            throw notSupported("la exportación a Guitar Pro");
        }

        @Override
        public Score importPowerTab(Path path) {
            throw notSupported("la importación de archivos de PowerTab");
        }
    };

    Score importMidi(Path path);

    void exportMidi(Score score, Path path);

    List<MidiTrackInfo> midiTracksIn(Path path);

    Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
            boolean useTwoChannelsPerTrack);

    Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize);

    Score importMidiTitleAndTimeSignatures(Score target, Path path);

    void exportWave(Score score, Path path, AudioQuality quality);

    Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices);

    Score importAscii(Path path);

    void exportAscii(Score score, Path path);

    Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote);

    String previewAscii(Track track, int columnsPerLine);

    void exportAscii(Track track, Path path, int columnsPerLine);

    Score importMusicXml(Path path);

    void exportMusicXml(Score score, Path path);

    Score importGuitarPro(Path path);

    Score importTabEdit(Path path);

    void exportGuitarPro(Score score, Path path);

    List<String> guitarProExportWarnings(Score score);

    Score importPowerTab(Path path);

    static ScoreFileException notSupported(String what) {
        return new ScoreFileException(what + " todavía no está disponible.");
    }
}

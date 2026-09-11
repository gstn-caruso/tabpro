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
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public void exportMidi(Score score, Path path) {
            throw notSupported(ScoreOperation.EXPORT_MIDI);
        }

        @Override
        public List<MidiTrackInfo> midiTracksIn(Path path) {
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public Score importMidiQuick(
                Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
                Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
                boolean useTwoChannelsPerTrack) {
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public Track importMidiInto(
                Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
                Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public void exportWave(Score score, Path path, AudioQuality quality) {
            throw notSupported(ScoreOperation.EXPORT_WAVE);
        }

        @Override
        public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
            throw notSupported(ScoreOperation.IMPORT_MIDI);
        }

        @Override
        public Score importAscii(Path path) {
            throw notSupported(ScoreOperation.IMPORT_ASCII);
        }

        @Override
        public void exportAscii(Score score, Path path) {
            throw notSupported(ScoreOperation.EXPORT_ASCII);
        }

        @Override
        public Track importAsciiInto(
                Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
            throw notSupported(ScoreOperation.IMPORT_ASCII);
        }

        @Override
        public String previewAscii(Track track, int columnsPerLine) {
            throw notSupported(ScoreOperation.EXPORT_ASCII);
        }

        @Override
        public void exportAscii(Track track, Path path, int columnsPerLine) {
            throw notSupported(ScoreOperation.EXPORT_ASCII);
        }

        @Override
        public Score importMusicXml(Path path) {
            throw notSupported(ScoreOperation.IMPORT_MUSIC_XML);
        }

        @Override
        public void exportMusicXml(Score score, Path path) {
            throw notSupported(ScoreOperation.EXPORT_MUSIC_XML);
        }

        @Override
        public Score importGuitarPro(Path path) {
            throw notSupported(ScoreOperation.OPEN_GUITAR_PRO);
        }

        @Override
        public Score importTabEdit(Path path) {
            throw notSupported(ScoreOperation.OPEN_TAB_EDIT);
        }

        @Override
        public void exportGuitarPro(Score score, Path path) {
            throw notSupported(ScoreOperation.EXPORT_GUITAR_PRO);
        }

        @Override
        public List<String> guitarProExportWarnings(Score score) {
            throw notSupported(ScoreOperation.EXPORT_GUITAR_PRO);
        }

        @Override
        public Score importPowerTab(Path path) {
            throw notSupported(ScoreOperation.IMPORT_POWER_TAB);
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

    static ScoreFileException notSupported(ScoreOperation operation) {
        return ScoreFileException.notSupported(operation);
    }
}

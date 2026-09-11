package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ExportWarning;
import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class SoundExchange implements ScoreExchange {

    private final MidiScoreExporter midiExporter = new MidiScoreExporter();
    private final WaveRenderer waveRenderer;

    public SoundExchange(WaveRenderer waveRenderer) {
        this.waveRenderer = waveRenderer;
    }

    @Override
    public void exportMidi(Score score, Path path) {
        midiExporter.export(score, path);
    }

    @Override
    public void exportWave(Score score, Path path, AudioQuality quality) {
        waveRenderer.render(midiExporter.toSequence(score), path, quality);
    }

    @Override
    public Score importMidi(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public List<MidiTrackInfo> midiTracksIn(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize,
            boolean useTwoChannelsPerTrack) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> chordPositionQuantize, Optional<NoteValue> noteDurationQuantize) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MIDI);
    }

    @Override
    public Score importAscii(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_ASCII);
    }

    @Override
    public void exportAscii(Score score, Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_ASCII);
    }

    @Override
    public Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_ASCII);
    }

    @Override
    public String previewAscii(Track track, int columnsPerLine) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_ASCII);
    }

    @Override
    public void exportAscii(Track track, Path path, int columnsPerLine) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_ASCII);
    }

    @Override
    public Score importMusicXml(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_MUSIC_XML);
    }

    @Override
    public void exportMusicXml(Score score, Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_MUSIC_XML);
    }

    @Override
    public Score importGuitarPro(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.OPEN_GUITAR_PRO);
    }

    @Override
    public Score importTabEdit(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.OPEN_TAB_EDIT);
    }

    @Override
    public void exportGuitarPro(Score score, Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_GUITAR_PRO);
    }

    @Override
    public List<ExportWarning> guitarProExportWarnings(Score score) {
        throw ScoreExchange.notSupported(ScoreOperation.EXPORT_GUITAR_PRO);
    }

    @Override
    public Score importPowerTab(Path path) {
        throw ScoreExchange.notSupported(ScoreOperation.IMPORT_POWER_TAB);
    }
}

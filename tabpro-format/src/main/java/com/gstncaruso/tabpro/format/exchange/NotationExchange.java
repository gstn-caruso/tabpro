package com.gstncaruso.tabpro.format.exchange;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExporter;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImporter;
import com.gstncaruso.tabpro.format.exchange.ascii.RhythmStrategy;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreImporter;
import com.gstncaruso.tabpro.format.exchange.midi.MidiTrackSummary;
import com.gstncaruso.tabpro.format.exchange.musicxml.MusicXmlScoreExporter;
import com.gstncaruso.tabpro.format.exchange.musicxml.MusicXmlScoreImporter;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProExporter;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProFile;
import com.gstncaruso.tabpro.format.powertab.PowerTabFile;
import com.gstncaruso.tabpro.format.tabledit.TabEditFile;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * El lado notacional del intercambio: los formatos que guardan como esta escrita la partitura.
 * Leer un MIDI ajeno es traducir una notacion que no es la nuestra, asi que el importador de
 * MIDI vive aca; exportar sonido es rendir la partitura, y de eso se ocupa tabpro-midi.
 */
public final class NotationExchange implements ScoreExchange {

    private final MidiScoreImporter midiImporter = new MidiScoreImporter();
    private final AsciiTabImporter asciiImporter = new AsciiTabImporter();
    private final AsciiTabExporter asciiExporter = new AsciiTabExporter();
    private final GuitarProFile guitarPro = new GuitarProFile();
    private final TabEditFile tabEdit = new TabEditFile();
    private final GuitarProExporter guitarProExporter = new GuitarProExporter();
    private final PowerTabFile powerTab = new PowerTabFile();
    private final MusicXmlScoreImporter musicXmlImporter = new MusicXmlScoreImporter();
    private final MusicXmlScoreExporter musicXmlExporter = new MusicXmlScoreExporter();

    @Override
    public Score importMidi(Path path) {
        return midiImporter.importQuick(path);
    }

    /** El lado sonoro del intercambio, {@code tabpro-midi}, es quien exporta MIDI. */
    @Override
    public void exportMidi(Score score, Path path) {
        throw ScoreExchange.notSupported("la exportación a MIDI");
    }

    @Override
    public List<MidiTrackInfo> midiTracksIn(Path path) {
        return midiImporter.tracksIn(path).stream().map(NotationExchange::toMidiTrackInfo).toList();
    }

    @Override
    public Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision,
            boolean useTwoChannelsPerTrack) {
        return midiImporter.importQuick(
                path, selectedMidiTrackIndices, transposeDownOneOctave, precision, useTwoChannelsPerTrack);
    }

    @Override
    public Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        return midiImporter.importInto(target, path, midiTrackIndices, transposeDownOneOctave, precision);
    }

    @Override
    public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        return midiImporter.importTitleAndTimeSignatures(target, path);
    }

    @Override
    public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
        return midiImporter.timelineOf(path, midiTrackIndices);
    }

    /** El lado sonoro del intercambio, {@code tabpro-midi}, es quien renderiza WAVE. */
    @Override
    public void exportWave(Score score, Path path, AudioQuality quality) {
        throw ScoreExchange.notSupported("la exportación a WAVE");
    }

    private static MidiTrackInfo toMidiTrackInfo(MidiTrackSummary summary) {
        return new MidiTrackInfo(
                summary.index(), summary.name(), summary.percussion(), summary.program(), summary.channelNumber(),
                summary.noteCount());
    }

    @Override
    public Score importMusicXml(Path path) {
        return musicXmlImporter.importScore(path);
    }

    @Override
    public void exportMusicXml(Score score, Path path) {
        musicXmlExporter.export(score, path);
    }

    @Override
    public Score importGuitarPro(Path path) {
        return guitarPro.read(path);
    }

    @Override
    public Score importTabEdit(Path path) {
        return tabEdit.read(path);
    }

    @Override
    public void exportGuitarPro(Score score, Path path) {
        guitarProExporter.write(score, path);
    }

    @Override
    public List<String> guitarProExportWarnings(Score score) {
        return guitarProExporter.warningsFor(score);
    }

    @Override
    public Score importPowerTab(Path path) {
        return powerTab.read(path);
    }

    @Override
    public Score importAscii(Path path) {
        return asciiImporter.importScore(path, AsciiTabImportOptions.standard());
    }

    @Override
    public void exportAscii(Score score, Path path) {
        asciiExporter.export(score, path, AsciiTabExportOptions.standard());
    }

    @Override
    public Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
        RhythmStrategy rhythm = fixedRhythm.map(value -> RhythmStrategy.fixed(Duration.of(value)))
                .orElseGet(() -> RhythmStrategy.fromSpacing(intervalsPerQuarterNote));
        AsciiTabImportOptions options = AsciiTabImportOptions.standard().withRhythm(rhythm);
        return asciiImporter.importInto(target, text, options);
    }

    @Override
    public String previewAscii(Track track, int columnsPerLine) {
        return asciiExporter.export(track, new AsciiTabExportOptions(columnsPerLine));
    }

    @Override
    public void exportAscii(Track track, Path path, int columnsPerLine) {
        asciiExporter.export(track, path, new AsciiTabExportOptions(columnsPerLine));
    }
}

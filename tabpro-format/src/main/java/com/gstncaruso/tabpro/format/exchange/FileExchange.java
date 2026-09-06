package com.gstncaruso.tabpro.format.exchange;

import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExporter;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImporter;
import com.gstncaruso.tabpro.format.exchange.ascii.RhythmStrategy;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreExporter;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreImporter;
import com.gstncaruso.tabpro.format.exchange.midi.MidiTrackSummary;
import com.gstncaruso.tabpro.format.exchange.musicxml.MusicXmlScoreExporter;
import com.gstncaruso.tabpro.format.exchange.musicxml.MusicXmlScoreImporter;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProExporter;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProFile;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Los formatos de intercambio que ya sabe manejar tabpro, en un solo lugar. */
public final class FileExchange implements ScoreExchange {

    private final MidiScoreImporter midiImporter = new MidiScoreImporter();
    private final MidiScoreExporter midiExporter = new MidiScoreExporter();
    private final AsciiTabImporter asciiImporter = new AsciiTabImporter();
    private final AsciiTabExporter asciiExporter = new AsciiTabExporter();
    private final GuitarProFile guitarPro = new GuitarProFile();
    private final GuitarProExporter guitarProExporter = new GuitarProExporter();
    private final MusicXmlScoreImporter musicXmlImporter = new MusicXmlScoreImporter();
    private final MusicXmlScoreExporter musicXmlExporter = new MusicXmlScoreExporter();

    @Override
    public Score importMidi(Path path) {
        return midiImporter.importQuick(path);
    }

    @Override
    public void exportMidi(Score score, Path path) {
        midiExporter.export(score, path);
    }

    @Override
    public List<MidiTrackInfo> midiTracksIn(Path path) {
        return midiImporter.tracksIn(path).stream().map(FileExchange::toMidiTrackInfo).toList();
    }

    @Override
    public Score importMidiQuick(Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave) {
        return midiImporter.importQuick(path, selectedMidiTrackIndices, transposeDownOneOctave);
    }

    @Override
    public Track importMidiInto(Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave) {
        return midiImporter.importInto(target, path, midiTrackIndices, transposeDownOneOctave);
    }

    @Override
    public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        return midiImporter.importTitleAndTimeSignatures(target, path);
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
    public void exportGuitarPro(Score score, Path path) {
        guitarProExporter.write(score, path);
    }

    @Override
    public List<String> guitarProExportWarnings(Score score) {
        return guitarProExporter.warningsFor(score);
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
    public Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm) {
        RhythmStrategy rhythm = fixedRhythm.map(value -> RhythmStrategy.fixed(Duration.of(value))).orElseGet(RhythmStrategy::fromSpacing);
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

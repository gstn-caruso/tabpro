package com.gstncaruso.tabpro.format.exchange;

import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabExporter;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImportOptions;
import com.gstncaruso.tabpro.format.exchange.ascii.AsciiTabImporter;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreExporter;
import com.gstncaruso.tabpro.format.exchange.midi.MidiScoreImporter;
import com.gstncaruso.tabpro.format.guitarpro.GuitarProFile;
import java.nio.file.Path;

/** Los formatos de intercambio que ya sabe manejar tabpro, en un solo lugar. */
public final class FileExchange implements ScoreExchange {

    private final MidiScoreImporter midiImporter = new MidiScoreImporter();
    private final MidiScoreExporter midiExporter = new MidiScoreExporter();
    private final AsciiTabImporter asciiImporter = new AsciiTabImporter();
    private final AsciiTabExporter asciiExporter = new AsciiTabExporter();
    private final GuitarProFile guitarPro = new GuitarProFile();

    @Override
    public Score importMidi(Path path) {
        return midiImporter.importQuick(path);
    }

    @Override
    public void exportMidi(Score score, Path path) {
        midiExporter.export(score, path);
    }

    @Override
    public Score importGuitarPro(Path path) {
        return guitarPro.read(path);
    }

    @Override
    public Score importAscii(Path path) {
        return asciiImporter.importScore(path, AsciiTabImportOptions.standard());
    }

    @Override
    public void exportAscii(Score score, Path path) {
        asciiExporter.export(score, path, AsciiTabExportOptions.standard());
    }
}

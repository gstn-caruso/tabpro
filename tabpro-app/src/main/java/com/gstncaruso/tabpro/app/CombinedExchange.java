package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.MidiTrackInfo;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Los dos lados del intercambio en el unico puerto que ve la ventana: la notacion, que sabe
 * tabpro-format, y el sonido a archivo, que sabe tabpro-midi. Ninguno de los dos conoce al
 * otro; aca es donde se juntan, que es lo que hace la app.
 */
public final class CombinedExchange implements ScoreExchange {

    private final ScoreExchange notation;
    private final ScoreExchange sound;

    public CombinedExchange(ScoreExchange notation, ScoreExchange sound) {
        this.notation = notation;
        this.sound = sound;
    }

    @Override
    public void exportMidi(Score score, Path path) {
        sound.exportMidi(score, path);
    }

    @Override
    public void exportWave(Score score, Path path, AudioQuality quality) {
        sound.exportWave(score, path, quality);
    }

    @Override
    public Score importMidi(Path path) {
        return notation.importMidi(path);
    }

    @Override
    public List<MidiTrackInfo> midiTracksIn(Path path) {
        return notation.midiTracksIn(path);
    }

    @Override
    public Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        return notation.importMidiQuick(path, selectedMidiTrackIndices, transposeDownOneOctave, precision);
    }

    @Override
    public Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        return notation.importMidiInto(target, path, midiTrackIndices, transposeDownOneOctave, precision);
    }

    @Override
    public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        return notation.importMidiTitleAndTimeSignatures(target, path);
    }

    /**
     * Escuchar una pista del archivo MIDI antes de importarla es parte de importar: la arma
     * quien sabe leer esa notacion ajena, no quien exporta sonido.
     */
    @Override
    public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
        return notation.midiTrackTimeline(path, midiTrackIndices);
    }

    @Override
    public Score importAscii(Path path) {
        return notation.importAscii(path);
    }

    @Override
    public void exportAscii(Score score, Path path) {
        notation.exportAscii(score, path);
    }

    @Override
    public Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
        return notation.importAsciiInto(target, text, fixedRhythm, intervalsPerQuarterNote);
    }

    @Override
    public String previewAscii(Track track, int columnsPerLine) {
        return notation.previewAscii(track, columnsPerLine);
    }

    @Override
    public void exportAscii(Track track, Path path, int columnsPerLine) {
        notation.exportAscii(track, path, columnsPerLine);
    }

    @Override
    public Score importMusicXml(Path path) {
        return notation.importMusicXml(path);
    }

    @Override
    public void exportMusicXml(Score score, Path path) {
        notation.exportMusicXml(score, path);
    }

    @Override
    public Score importGuitarPro(Path path) {
        return notation.importGuitarPro(path);
    }

    @Override
    public Score importTabEdit(Path path) {
        return notation.importTabEdit(path);
    }
}

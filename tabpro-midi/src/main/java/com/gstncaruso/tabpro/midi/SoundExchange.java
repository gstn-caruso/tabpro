package com.gstncaruso.tabpro.midi;

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
 * El lado sonoro del intercambio: los formatos que guardan como suena la partitura, no como
 * esta escrita. Hoy son el MIDI que exporta el manual en "File &gt; Export &gt; Midi" y el WAVE
 * de "File &gt; Export &gt; Wave".
 *
 * <p>Vive aca, con la reproduccion, y no con los formatos de notacion, porque exportar sonido
 * es rendir la partitura igual que cuando se la escucha. Leer un MIDI ajeno es otra cosa —
 * traducir una notacion que no es la nuestra— y de eso se ocupa tabpro-format.
 */
public final class SoundExchange implements ScoreExchange {

    private final MidiScoreExporter midiExporter = new MidiScoreExporter();
    private final WaveRenderer waveRenderer;

    /**
     * El renderer recibe el sintetizador de afuera (ver {@link WaveRenderer}): quien arma el
     * SoundExchange decide con que sintetizador y banco de sonidos suena el WAVE.
     */
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

    /** El lado notacional del intercambio, {@code tabpro-format}, es quien lee MIDI ajeno. */
    @Override
    public Score importMidi(Path path) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public List<MidiTrackInfo> midiTracksIn(Path path) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
        throw ScoreExchange.notSupported("la importación de MIDI");
    }

    @Override
    public Score importAscii(Path path) {
        throw ScoreExchange.notSupported("la importación de tablatura ASCII");
    }

    @Override
    public void exportAscii(Score score, Path path) {
        throw ScoreExchange.notSupported("la exportación a tablatura ASCII");
    }

    @Override
    public Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
        throw ScoreExchange.notSupported("la importación de tablatura ASCII");
    }

    @Override
    public String previewAscii(Track track, int columnsPerLine) {
        throw ScoreExchange.notSupported("la exportación a tablatura ASCII");
    }

    @Override
    public void exportAscii(Track track, Path path, int columnsPerLine) {
        throw ScoreExchange.notSupported("la exportación a tablatura ASCII");
    }

    @Override
    public Score importMusicXml(Path path) {
        throw ScoreExchange.notSupported("la importación de MusicXML");
    }

    @Override
    public void exportMusicXml(Score score, Path path) {
        throw ScoreExchange.notSupported("la exportación a MusicXML");
    }

    @Override
    public Score importGuitarPro(Path path) {
        throw ScoreExchange.notSupported("la apertura de archivos de Guitar Pro");
    }

    @Override
    public Score importTabEdit(Path path) {
        throw ScoreExchange.notSupported("la apertura de archivos de TablEdit");
    }

    @Override
    public void exportGuitarPro(Score score, Path path) {
        throw ScoreExchange.notSupported("la exportación a Guitar Pro");
    }

    @Override
    public List<String> guitarProExportWarnings(Score score) {
        throw ScoreExchange.notSupported("la exportación a Guitar Pro");
    }

    /** El lado notacional del intercambio, {@code tabpro-format}, es quien importa PowerTab. */
    @Override
    public Score importPowerTab(Path path) {
        throw ScoreExchange.notSupported("la importación de archivos de PowerTab");
    }
}

package com.gstncaruso.tabpro.core.files;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Los formatos ajenos que el programa sabe leer y escribir, como los enumera el
 * manual en "Import a Score" y "Export a Score". Cada uno puede no estar
 * disponible, y entonces se dice por que.
 */
public interface ScoreExchange {

    /** Cuando no hay ningun formato de intercambio conectado. */
    ScoreExchange NONE = new ScoreExchange() {
    };

    default Score importMidi(Path path) {
        throw notSupported("la importación de MIDI");
    }

    default void exportMidi(Score score, Path path) {
        throw notSupported("la exportación a MIDI");
    }

    /** Las pistas del archivo MIDI, para elegirlas en la ventana de import. */
    default List<MidiTrackInfo> midiTracksIn(Path path) {
        throw notSupported("la importación de MIDI");
    }

    /**
     * El "import rápido" del manual, pero solo con las pistas elegidas en la ventana. precision
     * vacio es sin restringir la cuantización de la posición y la duración de las notas; presente
     * es la figura más fina admitida, como deja elegir el manual.
     */
    default Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision) {
        throw notSupported("la importación de MIDI");
    }

    /** El "import paso a paso": la o las pistas MIDI elegidas reemplazan los compases de target. */
    default Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> precision) {
        throw notSupported("la importación de MIDI");
    }

    /** El botón "importar título y cambios de compás" del import paso a paso de MIDI. */
    default Score importMidiTitleAndTimeSignatures(Score target, Path path) {
        throw notSupported("la importación de MIDI");
    }

    /**
     * El "File &gt; Export &gt; Wave" del manual: renderiza el audio de la partitura entera,
     * fuera de tiempo real, con la calidad elegida.
     */
    default void exportWave(Score score, Path path, AudioQuality quality) {
        throw notSupported("la exportación a WAVE");
    }

    /**
     * Lo que hay que reproducir para escuchar la o las pistas elegidas antes de importarlas, como
     * pide el manual: "it is possible to listen to them [the MIDI tracks] or to open another file".
     */
    default Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices) {
        throw notSupported("la importación de MIDI");
    }

    default Score importAscii(Path path) {
        throw notSupported("la importación de tablatura ASCII");
    }

    default void exportAscii(Score score, Path path) {
        throw notSupported("la exportación a tablatura ASCII");
    }

    /**
     * El import de la ventana de ASCII: cae sobre la pista activa. fixedRhythm vacio es el
     * "&lt;variable&gt;" del manual (el ritmo se deduce del espaciado, tomando en cuenta
     * intervalsPerQuarterNote -- la "segunda lista" para el espaciado entre dos negras);
     * presente es un ritmo fijo (e intervalsPerQuarterNote no se usa).
     */
    default Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote) {
        throw notSupported("la importación de tablatura ASCII");
    }

    /** La vista previa de la ventana de export de ASCII: el texto de una sola pista. */
    default String previewAscii(Track track, int columnsPerLine) {
        throw notSupported("la exportación a tablatura ASCII");
    }

    /** El botón Exportar de la ventana de export de ASCII: solo la pista activa. */
    default void exportAscii(Track track, Path path, int columnsPerLine) {
        throw notSupported("la exportación a tablatura ASCII");
    }

    default Score importMusicXml(Path path) {
        throw notSupported("la importación de MusicXML");
    }

    default void exportMusicXml(Score score, Path path) {
        throw notSupported("la exportación a MusicXML");
    }

    default Score importGuitarPro(Path path) {
        throw notSupported("la apertura de archivos de Guitar Pro");
    }

    default Score importTabEdit(Path path) {
        throw notSupported("la apertura de archivos de TablEdit");
    }

    /** "File &gt; Export &gt; Guitar Pro 4 Format" del manual. */
    default void exportGuitarPro(Score score, Path path) {
        throw notSupported("la exportación a Guitar Pro");
    }

    /**
     * Que se pierde si esta partitura en particular se exporta a Guitar Pro 4 (letra vieja,
     * segunda voz, autor de la música, etc.), para avisarle al usuario antes de exportar.
     */
    default List<String> guitarProExportWarnings(Score score) {
        throw notSupported("la exportación a Guitar Pro");
    }

    private static ScoreFileException notSupported(String what) {
        return new ScoreFileException(what + " todavía no está disponible.");
    }
}

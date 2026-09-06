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
 * manual en "Import a Score" y "Export a Score". Cada implementacion declara los
 * veinte metodos: los que soporta, y los que no con {@link #notSupported}. No
 * hay default que tire por ella, para que "no lo soporto" y "me lo olvide" no se
 * vean iguales.
 */
public interface ScoreExchange {

    /** Cuando no hay ningun formato de intercambio conectado. */
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
                Optional<NoteValue> precision) {
            throw notSupported("la importación de MIDI");
        }

        @Override
        public Track importMidiInto(
                Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
                Optional<NoteValue> precision) {
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

    /** Las pistas del archivo MIDI, para elegirlas en la ventana de import. */
    List<MidiTrackInfo> midiTracksIn(Path path);

    /**
     * El "import rápido" del manual, pero solo con las pistas elegidas en la ventana. precision
     * vacio es sin restringir la cuantización de la posición y la duración de las notas; presente
     * es la figura más fina admitida, como deja elegir el manual.
     */
    Score importMidiQuick(
            Path path, List<Integer> selectedMidiTrackIndices, boolean transposeDownOneOctave, Optional<NoteValue> precision);

    /** El "import paso a paso": la o las pistas MIDI elegidas reemplazan los compases de target. */
    Track importMidiInto(
            Track target, Path path, List<Integer> midiTrackIndices, boolean transposeDownOneOctave,
            Optional<NoteValue> precision);

    /** El botón "importar título y cambios de compás" del import paso a paso de MIDI. */
    Score importMidiTitleAndTimeSignatures(Score target, Path path);

    /**
     * El "File &gt; Export &gt; Wave" del manual: renderiza el audio de la partitura entera,
     * fuera de tiempo real, con la calidad elegida.
     */
    void exportWave(Score score, Path path, AudioQuality quality);

    /**
     * Lo que hay que reproducir para escuchar la o las pistas elegidas antes de importarlas, como
     * pide el manual: "it is possible to listen to them [the MIDI tracks] or to open another file".
     */
    Timeline midiTrackTimeline(Path path, List<Integer> midiTrackIndices);

    Score importAscii(Path path);

    void exportAscii(Score score, Path path);

    /**
     * El import de la ventana de ASCII: cae sobre la pista activa. fixedRhythm vacio es el
     * "&lt;variable&gt;" del manual (el ritmo se deduce del espaciado, tomando en cuenta
     * intervalsPerQuarterNote -- la "segunda lista" para el espaciado entre dos negras);
     * presente es un ritmo fijo (e intervalsPerQuarterNote no se usa).
     */
    Track importAsciiInto(Track target, String text, Optional<NoteValue> fixedRhythm, int intervalsPerQuarterNote);

    /** La vista previa de la ventana de export de ASCII: el texto de una sola pista. */
    String previewAscii(Track track, int columnsPerLine);

    /** El botón Exportar de la ventana de export de ASCII: solo la pista activa. */
    void exportAscii(Track track, Path path, int columnsPerLine);

    Score importMusicXml(Path path);

    void exportMusicXml(Score score, Path path);

    Score importGuitarPro(Path path);

    Score importTabEdit(Path path);

    /** "File &gt; Export &gt; Guitar Pro 4 Format" del manual. */
    void exportGuitarPro(Score score, Path path);

    /**
     * Que se pierde si esta partitura en particular se exporta a Guitar Pro 4 (letra vieja,
     * segunda voz, autor de la música, etc.), para avisarle al usuario antes de exportar.
     */
    List<String> guitarProExportWarnings(Score score);

    /** "File &gt; Import &gt; PowerTab" del manual. */
    Score importPowerTab(Path path);

    /** La excepción con la que una implementación declara, a mano, que no soporta un formato. */
    static ScoreFileException notSupported(String what) {
        return new ScoreFileException(what + " todavía no está disponible.");
    }
}

package com.gstncaruso.tabpro.core.files;

import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;

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

    default Score importAscii(Path path) {
        throw notSupported("la importación de tablatura ASCII");
    }

    default void exportAscii(Score score, Path path) {
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

    private static ScoreFileException notSupported(String what) {
        return new ScoreFileException(what + " todavía no está disponible.");
    }
}

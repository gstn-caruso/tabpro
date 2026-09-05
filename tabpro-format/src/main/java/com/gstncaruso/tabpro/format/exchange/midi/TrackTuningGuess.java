package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Locale;

/**
 * La afinacion que propone el "import rapido" cuando no hay tablatura en el archivo: se fija
 * en el nombre de la pista y, si no dice nada, en la familia de instrumento de General MIDI.
 */
final class TrackTuningGuess {

    /** Los programas 33 a 40 de General MIDI (1 a 128) son la familia de los bajos. */
    private static final int FIRST_BASS_PROGRAM = 32;
    private static final int LAST_BASS_PROGRAM = 39;

    private TrackTuningGuess() {
    }

    static Tuning forQuickImport(String trackName, int program) {
        return isBassLike(trackName, program) ? Tuning.standardBass() : Tuning.standard();
    }

    private static boolean isBassLike(String trackName, int program) {
        String lowerName = trackName.toLowerCase(Locale.ROOT);
        boolean nameSaysBass = lowerName.contains("bass") || lowerName.contains("bajo");
        boolean programSaysBass = program >= FIRST_BASS_PROGRAM && program <= LAST_BASS_PROGRAM;
        return nameSaysBass || programSaysBass;
    }
}

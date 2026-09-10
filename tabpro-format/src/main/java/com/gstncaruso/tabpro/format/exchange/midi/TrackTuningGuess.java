package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Locale;

final class TrackTuningGuess {

    /** General MIDI programs 33 to 40 (1 to 128) are the bass family. */
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

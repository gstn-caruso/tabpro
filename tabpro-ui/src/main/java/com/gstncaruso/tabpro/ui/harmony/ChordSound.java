package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.playback.Player;

/** El boton "escuchar" de la zona B: hace sonar cada cuerda que toca el diagrama. */
public final class ChordSound {

    private ChordSound() {
    }

    public static void play(ChordDiagram diagram, Tuning tuning, Player player, int program) {
        for (int string = 1; string <= diagram.stringCount(); string++) {
            if (diagram.isPlayed(string)) {
                player.playNote(tuning.pitchOfString(string).transposed(diagram.fretOfString(string)), program);
            }
        }
    }
}

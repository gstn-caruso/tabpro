package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import java.util.Map;

/**
 * La altura que realmente suena un armonico. El natural depende del nodo de
 * la cuerda al aire (el traste marca donde tocar, no la altura); los demas
 * se transportan desde la nota pisada.
 */
public final class HarmonicPitch {

    /** Cuanto suena un armonico natural por sobre la cuerda al aire, segun el traste del nodo. */
    private static final Map<Integer, Integer> NATURAL_INTERVALS = Map.of(
            12, 12,
            7, 19, 19, 19,
            5, 24, 24, 24,
            4, 28, 9, 28, 16, 28);

    /** Los armonicos que no son naturales se tocan pisando y suenan una octava mas arriba. */
    private static final int ARTIFICIAL_INTERVAL = 12;

    private HarmonicPitch() {
    }

    public static Pitch of(HarmonicType type, Pitch openString, Pitch fretted, int fret) {
        if (type == HarmonicType.NATURAL) {
            Integer interval = NATURAL_INTERVALS.get(fret);
            return interval == null ? fretted : openString.transposed(interval);
        }
        return fretted.transposed(ARTIFICIAL_INTERVAL);
    }
}

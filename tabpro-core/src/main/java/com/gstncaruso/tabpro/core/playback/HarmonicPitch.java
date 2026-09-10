package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import java.util.Map;

public final class HarmonicPitch {

    private static final Map<Integer, Integer> NATURAL_INTERVALS = Map.of(
            12, 12,
            7, 19, 19, 19,
            5, 24, 24, 24,
            4, 28, 9, 28, 16, 28);

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

package com.gstncaruso.tabpro.ui.tracks;

import java.awt.Color;
import java.util.List;

public final class TrackColors {

    private static final List<Color> PALETTE = List.of(
            new Color(0x407CF1),
            new Color(0xE5A44A),
            new Color(0x46A758),
            new Color(0xA65EE6),
            new Color(0x2FB8C6),
            new Color(0xE56AA8),
            new Color(0xC2B33F));

    public static final int COUNT = PALETTE.size();

    private TrackColors() {
    }

    public static Color of(int trackIndex) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex debe ser >= 0: " + trackIndex);
        }
        return PALETTE.get(trackIndex % COUNT);
    }
}

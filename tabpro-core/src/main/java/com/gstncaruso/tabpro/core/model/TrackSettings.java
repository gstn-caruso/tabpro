package com.gstncaruso.tabpro.core.model;

/** Lo que la ventana de propiedades define sobre una pista. */
public record TrackSettings(
        ScoreColor color,
        int capo,
        int fretCount,
        boolean percussion,
        boolean twelveString,
        boolean banjoFifthString,
        TrackDisplay display) {

    public static final int DEFAULT_FRET_COUNT = 24;

    public TrackSettings {
        if (capo < 0) {
            throw new IllegalArgumentException("la cejilla no puede ser negativa: " + capo);
        }
        if (fretCount < 1 || fretCount > Tuning.MAX_FRET) {
            throw new IllegalArgumentException("fretCount fuera de rango: " + fretCount);
        }
    }

    public static TrackSettings standard(ScoreColor color) {
        return new TrackSettings(color, 0, DEFAULT_FRET_COUNT, false, false, false, TrackDisplay.standard());
    }

    public static TrackSettings percussion(ScoreColor color) {
        return new TrackSettings(color, 0, DEFAULT_FRET_COUNT, true, false, false,
                TrackDisplay.standard().withTuningLegend(false));
    }

    public TrackSettings withColor(ScoreColor color) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withCapo(int capo) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withFretCount(int fretCount) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withPercussion(boolean percussion) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withTwelveString(boolean twelveString) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withBanjoFifthString(boolean banjoFifthString) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }

    public TrackSettings withDisplay(TrackDisplay display) {
        return new TrackSettings(color, capo, fretCount, percussion, twelveString, banjoFifthString, display);
    }
}

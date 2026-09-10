package com.gstncaruso.tabpro.core.model;

public record TrackSettings(
        ScoreColor color,
        int capo,
        int fretCount,
        boolean percussion,
        boolean twelveString,
        boolean banjoFifthString,
        TrackDisplay display,
        boolean forceChannels11to16) {

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
        return new TrackSettings(color, 0, DEFAULT_FRET_COUNT, false, false, false, TrackDisplay.standard(), false);
    }

    public static TrackSettings percussion(ScoreColor color) {
        return new TrackSettings(color, 0, DEFAULT_FRET_COUNT, true, false, false, TrackDisplay.standard(), false);
    }

    public TrackSettings withColor(ScoreColor color) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withCapo(int capo) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withFretCount(int fretCount) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withPercussion(boolean percussion) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withTwelveString(boolean twelveString) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withBanjoFifthString(boolean banjoFifthString) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withDisplay(TrackDisplay display) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }

    public TrackSettings withForceChannels11to16(boolean forceChannels11to16) {
        return new TrackSettings(
                color, capo, fretCount, percussion, twelveString, banjoFifthString, display, forceChannels11to16);
    }
}

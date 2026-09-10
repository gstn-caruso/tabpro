package com.gstncaruso.tabpro.core.tuning;

import com.gstncaruso.tabpro.core.model.Pitch;

public record DetectedPitch(double frequencyHz, double clarity) {

    public static final double MIN_CLARITY = 0.6;

    public boolean isAudible() {
        return frequencyHz > 0 && clarity >= MIN_CLARITY;
    }

    public int nearestMidiNumber() {
        return (int) Math.round(midiNumberOf(frequencyHz));
    }

    public int centsFrom(Pitch target) {
        return (int) Math.round((midiNumberOf(frequencyHz) - target.midiNumber()) * 100);
    }

    /** A4 is 440 Hz and MIDI note number 69. */
    private static double midiNumberOf(double frequencyHz) {
        return 69 + 12 * Math.log(frequencyHz / 440.0) / Math.log(2);
    }

    public static double frequencyOf(Pitch pitch) {
        return 440.0 * Math.pow(2, (pitch.midiNumber() - 69) / 12.0);
    }
}

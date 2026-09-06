package com.gstncaruso.tabpro.core.tuning;

import com.gstncaruso.tabpro.core.model.Pitch;

/** Lo que se escuchó: una frecuencia y cuán clara venía. */
public record DetectedPitch(double frequencyHz, double clarity) {

    /** Debajo de esta claridad lo que llega es ruido, no una cuerda. */
    public static final double MIN_CLARITY = 0.6;

    public boolean isAudible() {
        return frequencyHz > 0 && clarity >= MIN_CLARITY;
    }

    /** La nota MIDI más cercana a lo que suena. */
    public int nearestMidiNumber() {
        return (int) Math.round(midiNumberOf(frequencyHz));
    }

    /** Cuántas centésimas de semitono separa lo que suena de esa nota. */
    public int centsFrom(Pitch target) {
        return (int) Math.round((midiNumberOf(frequencyHz) - target.midiNumber()) * 100);
    }

    /** La 4ta octava de La son 440 Hz y el número MIDI 69. */
    private static double midiNumberOf(double frequencyHz) {
        return 69 + 12 * Math.log(frequencyHz / 440.0) / Math.log(2);
    }

    public static double frequencyOf(Pitch pitch) {
        return 440.0 * Math.pow(2, (pitch.midiNumber() - 69) / 12.0);
    }
}

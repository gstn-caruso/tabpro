package com.gstncaruso.tabpro.core.model.effects;

public record BendPoint(int position, int quarterTones, int vibrato) {

    public static final int LAST_POSITION = 60;

    public static final int MAX_QUARTER_TONES = 12;

    public static final int MAX_VIBRATO = 3;

    public BendPoint {
        if (position < 0 || position > LAST_POSITION) {
            throw new IllegalArgumentException("position must be between 0 and " + LAST_POSITION + ": " + position);
        }
        if (quarterTones < -MAX_QUARTER_TONES || quarterTones > MAX_QUARTER_TONES) {
            throw new IllegalArgumentException("quarterTones out of range: " + quarterTones);
        }
        if (vibrato < 0 || vibrato > MAX_VIBRATO) {
            throw new IllegalArgumentException("vibrato must be between 0 and " + MAX_VIBRATO + ": " + vibrato);
        }
    }

    public static BendPoint at(int position, int quarterTones) {
        return new BendPoint(position, quarterTones, 0);
    }

    public double semitones() {
        return quarterTones / 2.0;
    }

    public double fractionOfTheNote() {
        return position / (double) LAST_POSITION;
    }
}

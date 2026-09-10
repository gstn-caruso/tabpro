package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.TimeSignature;

public final class MeasureDurationText {

    private MeasureDurationText() {
    }

    public static String of(Measure measure) {
        TimeSignature timeSignature = measure.timeSignature();
        double actualBeats = (double) measure.durationTicks() / ticksPerBeat(timeSignature);
        return format(actualBeats) + "/" + timeSignature.beats();
    }

    private static long ticksPerBeat(TimeSignature timeSignature) {
        return Duration.TICKS_PER_QUARTER * 4L / timeSignature.beatUnit();
    }

    private static String format(double beats) {
        double rounded = Math.round(beats * 100) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}

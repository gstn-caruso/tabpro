package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.Locale;

/** Cuanto dura realmente un compas contra lo que pide su medida, en beats con tres decimales, como en Guitar Pro 5. */
public final class MeasureBeatsRatioText {

    private MeasureBeatsRatioText() {
    }

    public static String of(Measure measure) {
        TimeSignature timeSignature = measure.timeSignature();
        double actualBeats = (double) measure.durationTicks() / ticksPerBeat(timeSignature);
        return String.format(Locale.ROOT, "%.3f : %.3f", actualBeats, (double) timeSignature.beats());
    }

    private static long ticksPerBeat(TimeSignature timeSignature) {
        return Duration.TICKS_PER_QUARTER * 4L / timeSignature.beatUnit();
    }
}

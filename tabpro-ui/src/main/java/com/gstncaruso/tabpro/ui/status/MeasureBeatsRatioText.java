package com.gstncaruso.tabpro.ui.status;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.Locale;

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

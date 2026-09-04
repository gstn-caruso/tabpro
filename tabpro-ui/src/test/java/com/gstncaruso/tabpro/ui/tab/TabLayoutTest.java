package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import org.junit.jupiter.api.Test;

class TabLayoutTest {

    private Track trackOf(Measure... measures) {
        return new Track("Guitarra", Tuning.standard(), 25, List.of(measures));
    }

    private Measure measureOfBeats(Duration duration, int count) {
        List<Beat> beats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            beats.add(Beat.rest(duration));
        }
        return new Measure(TimeSignature.fourFour(), beats);
    }

    private Measure measureOfQuarterBeats(int count) {
        return measureOfBeats(Duration.quarter(), count);
    }

    @Test
    void placesASingleMeasureOnTheFirstLine() {
        Track track = trackOf(measureOfQuarterBeats(1));
        TabLayout layout = TabLayout.of(track, 400);

        assertEquals(1, layout.lineCount());
        assertEquals(new Rectangle(16, 40, 76, 60), layout.measureBounds(0));
    }
}

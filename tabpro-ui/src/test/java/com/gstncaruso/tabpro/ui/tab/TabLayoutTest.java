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

    @Test
    void givesAQuarterBeatItsFixedWidth() {
        assertEquals(44, TabLayout.beatWidth(Duration.quarter()));
    }

    @Test
    void makesLongerDurationsWider() {
        assertEquals(80, TabLayout.beatWidth(Duration.quarter().longer().longer()));
        assertEquals(60, TabLayout.beatWidth(Duration.quarter().longer()));
        assertEquals(34, TabLayout.beatWidth(Duration.quarter().shorter()));
        assertEquals(28, TabLayout.beatWidth(Duration.quarter().shorter().shorter()));
        assertEquals(24, TabLayout.beatWidth(Duration.quarter().shorter().shorter().shorter()));
        assertEquals(22, TabLayout.beatWidth(Duration.quarter().shorter().shorter().shorter().shorter()));
    }

    @Test
    void makesDottedDurationsWiderThanPlain() {
        assertEquals(50, TabLayout.beatWidth(Duration.quarter().toggledDot()));
    }

    @Test
    void startsBeatsAfterTheMeasureLeftPadding() {
        Track track = trackOf(measureOfQuarterBeats(1));
        TabLayout layout = TabLayout.of(track, 400);

        assertEquals(new Rectangle(40, 40, 44, 60), layout.beatBounds(0, 0));
    }

    @Test
    void laysOutMeasuresLeftToRightOnOneLine() {
        Track track = trackOf(measureOfQuarterBeats(1), measureOfQuarterBeats(1));
        TabLayout layout = TabLayout.of(track, 400);

        assertEquals(1, layout.lineCount());
        assertEquals(new Rectangle(16, 40, 76, 60), layout.measureBounds(0));
        assertEquals(new Rectangle(92, 40, 76, 60), layout.measureBounds(1));
    }
}

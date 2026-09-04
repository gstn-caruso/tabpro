package com.gstncaruso.tabpro.ui.tab;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;

public final class TabLayout {

    public static final int LEFT_MARGIN = 16;
    public static final int TOP_MARGIN = 40;
    public static final int LINE_GAP = 40;
    public static final int STRING_SPACING = 12;
    public static final int MEASURE_LEFT_PADDING = 24;
    public static final int MEASURE_RIGHT_PADDING = 8;

    private final List<Rectangle> measureBounds;
    private final List<List<Rectangle>> beatBounds;

    private TabLayout(List<Rectangle> measureBounds, List<List<Rectangle>> beatBounds) {
        this.measureBounds = measureBounds;
        this.beatBounds = beatBounds;
    }

    public static TabLayout of(Track track, int availableWidth) {
        int staffHeight = (track.tuning().stringCount() - 1) * STRING_SPACING;
        List<Rectangle> measureBounds = new ArrayList<>();
        List<List<Rectangle>> beatBounds = new ArrayList<>();
        int x = LEFT_MARGIN;
        int y = TOP_MARGIN;
        for (Measure measure : track.measures()) {
            List<Rectangle> beats = new ArrayList<>();
            int beatX = x + MEASURE_LEFT_PADDING;
            for (Beat beat : measure.beats()) {
                int width = beatWidth(beat.duration());
                beats.add(new Rectangle(beatX, y, width, staffHeight));
                beatX += width;
            }
            int measureWidth = MEASURE_LEFT_PADDING + (beatX - x - MEASURE_LEFT_PADDING) + MEASURE_RIGHT_PADDING;
            measureBounds.add(new Rectangle(x, y, measureWidth, staffHeight));
            beatBounds.add(beats);
            x += measureWidth;
        }
        return new TabLayout(measureBounds, beatBounds);
    }

    public static int beatWidth(Duration duration) {
        int base = switch (duration.value()) {
            case WHOLE -> 80;
            case HALF -> 60;
            case QUARTER -> 44;
            case EIGHTH -> 34;
            case SIXTEENTH -> 28;
            case THIRTY_SECOND -> 24;
            case SIXTY_FOURTH -> 22;
        };
        return duration.dotted() ? base + 6 : base;
    }

    public int lineCount() {
        return 1;
    }

    public Rectangle measureBounds(int measure) {
        return measureBounds.get(measure);
    }

    public Rectangle beatBounds(int measure, int beat) {
        return beatBounds.get(measure).get(beat);
    }
}

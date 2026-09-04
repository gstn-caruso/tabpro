package com.gstncaruso.tabpro.ui.tab;

import java.awt.Rectangle;
import java.util.List;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Track;

public final class TabLayout {

    public static final int LEFT_MARGIN = 16;
    public static final int TOP_MARGIN = 40;
    public static final int LINE_GAP = 40;
    public static final int STRING_SPACING = 12;
    public static final int MEASURE_LEFT_PADDING = 24;
    public static final int MEASURE_RIGHT_PADDING = 8;

    private final List<Rectangle> measureBounds;

    private TabLayout(List<Rectangle> measureBounds) {
        this.measureBounds = measureBounds;
    }

    public static TabLayout of(Track track, int availableWidth) {
        int staffHeight = (track.tuning().stringCount() - 1) * STRING_SPACING;
        int width = MEASURE_LEFT_PADDING + beatWidth(track.measure(0).beat(0).duration()) + MEASURE_RIGHT_PADDING;
        Rectangle bounds = new Rectangle(LEFT_MARGIN, TOP_MARGIN, width, staffHeight);
        return new TabLayout(List.of(bounds));
    }

    public static int beatWidth(Duration duration) {
        return 44;
    }

    public int lineCount() {
        return 1;
    }

    public Rectangle measureBounds(int measure) {
        return measureBounds.get(measure);
    }
}

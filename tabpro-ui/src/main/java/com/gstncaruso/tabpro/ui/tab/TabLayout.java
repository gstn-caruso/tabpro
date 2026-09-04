package com.gstncaruso.tabpro.ui.tab;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final int lineCount;
    private final int lineHeight;

    private TabLayout(List<Rectangle> measureBounds, List<List<Rectangle>> beatBounds, int lineCount, int lineHeight) {
        this.measureBounds = measureBounds;
        this.beatBounds = beatBounds;
        this.lineCount = lineCount;
        this.lineHeight = lineHeight;
    }

    public static TabLayout of(Track track, int availableWidth) {
        int staffHeight = (track.tuning().stringCount() - 1) * STRING_SPACING;
        int lineHeight = TOP_MARGIN + staffHeight + LINE_GAP;
        List<Rectangle> measureBounds = new ArrayList<>();
        List<List<Rectangle>> beatBounds = new ArrayList<>();
        int line = 0;
        int x = LEFT_MARGIN;
        boolean lineHasMeasure = false;
        for (Measure measure : track.measures()) {
            int measureWidth = measureWidth(measure);
            if (lineHasMeasure && x + measureWidth > availableWidth) {
                line++;
                x = LEFT_MARGIN;
                lineHasMeasure = false;
            }
            int y = line * lineHeight + TOP_MARGIN;
            measureBounds.add(new Rectangle(x, y, measureWidth, staffHeight));
            List<Rectangle> beats = new ArrayList<>();
            int beatX = x + MEASURE_LEFT_PADDING;
            for (Beat beat : measure.beats()) {
                int width = beatWidth(beat.duration());
                beats.add(new Rectangle(beatX, y, width, staffHeight));
                beatX += width;
            }
            beatBounds.add(beats);
            x += measureWidth;
            lineHasMeasure = true;
        }
        return new TabLayout(measureBounds, beatBounds, line + 1, lineHeight);
    }

    private static int measureWidth(Measure measure) {
        int total = MEASURE_LEFT_PADDING + MEASURE_RIGHT_PADDING;
        for (Beat beat : measure.beats()) {
            total += beatWidth(beat.duration());
        }
        return total;
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
        return lineCount;
    }

    public int totalHeight() {
        return lineCount * lineHeight;
    }

    public Rectangle measureBounds(int measure) {
        return measureBounds.get(measure);
    }

    public Rectangle beatBounds(int measure, int beat) {
        return beatBounds.get(measure).get(beat);
    }

    public int stringY(int measure, int string) {
        return measureBounds(measure).y + (string - 1) * STRING_SPACING;
    }

    public Optional<Hit> hitTest(int x, int y) {
        for (int measure = 0; measure < beatBounds.size(); measure++) {
            List<Rectangle> beats = beatBounds.get(measure);
            for (int beat = 0; beat < beats.size(); beat++) {
                Rectangle bounds = beats.get(beat);
                Rectangle expanded = new Rectangle(
                        bounds.x, bounds.y - STRING_SPACING / 2, bounds.width, bounds.height + STRING_SPACING);
                if (expanded.contains(x, y)) {
                    return Optional.of(new Hit(measure, beat, nearestString(measure, y)));
                }
            }
        }
        return Optional.empty();
    }

    private int nearestString(int measure, int y) {
        int stringCount = measureBounds(measure).height / STRING_SPACING + 1;
        int nearest = 1;
        int bestDistance = Integer.MAX_VALUE;
        for (int string = 1; string <= stringCount; string++) {
            int distance = Math.abs(y - stringY(measure, string));
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = string;
            }
        }
        return nearest;
    }

    public record Hit(int measure, int beat, int string) {}
}

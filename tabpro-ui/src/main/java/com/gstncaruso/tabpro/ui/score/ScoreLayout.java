package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Donde va cada cosa en la partitura: los compases se reparten en sistemas segun el ancho
 * disponible y, dentro de cada sistema, las pistas se apilan una debajo de la otra con su
 * pentagrama arriba y su tablatura abajo. Una columna de compas mide lo mismo para todas las
 * pistas, asi el compas tres de la guitarra empieza donde empieza el compas tres del bajo.
 */
public final class ScoreLayout {

    public static final int LEFT_MARGIN = 20;
    public static final int RIGHT_MARGIN = 20;
    public static final int TOP_MARGIN = 14;
    public static final int BOTTOM_MARGIN = 20;

    public static final int TRACK_LABEL_HEIGHT = 17;
    public static final int STAFF_LINE_SPACING = 8;
    public static final int STAFF_HEIGHT = 4 * STAFF_LINE_SPACING;
    public static final int STAFF_HEADROOM = 34;
    public static final int STAFF_TO_TAB_GAP = 42;
    public static final int STRING_SPACING = 12;
    public static final int TRACK_GAP = 20;
    public static final int SYSTEM_GAP = 26;

    public static final int MEASURE_LEFT_PADDING = 26;
    public static final int MEASURE_RIGHT_PADDING = 10;
    public static final int MIN_MEASURE_WIDTH = 76;
    /** Lo que se reserva al arranque de cada sistema para la clave y la indicacion de compas. */
    public static final int SYSTEM_HEAD_WIDTH = 54;

    private final Score score;
    private final int[] columnWidth;
    private final int[] headWidth;
    private final int[] columnX;
    private final int[] system;
    private final int[] blockTop;
    private final int blockHeightTotal;
    private final int systemCount;
    private final List<List<List<Rectangle>>> beatBounds;

    private ScoreLayout(
            Score score,
            int[] columnWidth,
            int[] headWidth,
            int[] columnX,
            int[] system,
            int[] blockTop,
            int blockHeightTotal,
            int systemCount,
            List<List<List<Rectangle>>> beatBounds) {
        this.score = score;
        this.columnWidth = columnWidth;
        this.headWidth = headWidth;
        this.columnX = columnX;
        this.system = system;
        this.blockTop = blockTop;
        this.blockHeightTotal = blockHeightTotal;
        this.systemCount = systemCount;
        this.beatBounds = beatBounds;
    }

    public static ScoreLayout of(Score score, int availableWidth) {
        int measureCount = score.measureCount();
        int[] columnWidth = columnWidths(score, measureCount);
        int usableWidth = Math.max(MIN_MEASURE_WIDTH, availableWidth - LEFT_MARGIN - RIGHT_MARGIN);

        int[] columnX = new int[measureCount];
        int[] headWidth = new int[measureCount];
        int[] system = new int[measureCount];
        int currentSystem = 0;
        int x = LEFT_MARGIN;
        for (int measure = 0; measure < measureCount; measure++) {
            boolean startsASystem = x == LEFT_MARGIN;
            if (!startsASystem && x + columnWidth[measure] > LEFT_MARGIN + usableWidth) {
                currentSystem++;
                x = LEFT_MARGIN;
                startsASystem = true;
            }
            headWidth[measure] = startsASystem ? SYSTEM_HEAD_WIDTH : 0;
            columnX[measure] = x;
            system[measure] = currentSystem;
            x += columnWidth[measure] + headWidth[measure];
        }

        int[] blockTop = new int[score.trackCount()];
        int stacked = 0;
        for (int track = 0; track < score.trackCount(); track++) {
            blockTop[track] = stacked;
            stacked += blockHeight(score.track(track)) + TRACK_GAP;
        }
        int blockHeightTotal = Math.max(0, stacked - TRACK_GAP);

        return new ScoreLayout(
                score,
                columnWidth,
                headWidth,
                columnX,
                system,
                blockTop,
                blockHeightTotal,
                measureCount == 0 ? 1 : currentSystem + 1,
                beatBoundsOf(score, columnX, headWidth, columnWidth));
    }

    private static int[] columnWidths(Score score, int measureCount) {
        int[] widths = new int[measureCount];
        for (int measure = 0; measure < measureCount; measure++) {
            int widest = MIN_MEASURE_WIDTH;
            for (Track track : score.tracks()) {
                if (measure < track.measureCount()) {
                    widest = Math.max(widest, naturalWidth(track.measure(measure)));
                }
            }
            widths[measure] = widest;
        }
        return widths;
    }

    private static int naturalWidth(Measure measure) {
        int total = MEASURE_LEFT_PADDING + MEASURE_RIGHT_PADDING;
        for (Beat beat : measure.beats()) {
            total += beatWidth(beat.duration());
        }
        return total;
    }

    public static int beatWidth(Duration duration) {
        int base = switch (duration.value()) {
            case WHOLE -> 86;
            case HALF -> 64;
            case QUARTER -> 46;
            case EIGHTH -> 34;
            case SIXTEENTH -> 28;
            case THIRTY_SECOND -> 24;
            case SIXTY_FOURTH -> 22;
        };
        return duration.dotted() ? base + 6 : base;
    }

    private static int blockHeight(Track track) {
        return TRACK_LABEL_HEIGHT + STAFF_HEADROOM + STAFF_HEIGHT + STAFF_TO_TAB_GAP + tabHeightOf(track);
    }

    private static int tabHeightOf(Track track) {
        return (track.tuning().stringCount() - 1) * STRING_SPACING;
    }

    /**
     * Las figuras de un compas se estiran para llenar su columna, que puede ser mas ancha de lo
     * que la pista pide porque otra pista mete mas notas en el mismo compas. Cada beat se lleva
     * una tajada proporcional a su figura, y el ultimo cierra justo contra la barra.
     */
    private static List<List<List<Rectangle>>> beatBoundsOf(
            Score score, int[] columnX, int[] headWidth, int[] columnWidth) {
        List<List<List<Rectangle>>> perTrack = new ArrayList<>();
        for (Track track : score.tracks()) {
            List<List<Rectangle>> perMeasure = new ArrayList<>();
            for (int measure = 0; measure < track.measureCount(); measure++) {
                perMeasure.add(stretchedBeats(
                        track.measure(measure), columnX[measure] + headWidth[measure], columnWidth[measure]));
            }
            perTrack.add(perMeasure);
        }
        return perTrack;
    }

    private static List<Rectangle> stretchedBeats(Measure measure, int columnX, int columnWidth) {
        int available = columnWidth - MEASURE_LEFT_PADDING - MEASURE_RIGHT_PADDING;
        int natural = measure.beats().stream().mapToInt(beat -> beatWidth(beat.duration())).sum();
        int start = columnX + MEASURE_LEFT_PADDING;

        List<Rectangle> bounds = new ArrayList<>();
        int consumedNatural = 0;
        int consumed = 0;
        for (Beat beat : measure.beats()) {
            consumedNatural += beatWidth(beat.duration());
            int end = (int) Math.round((double) available * consumedNatural / natural);
            bounds.add(new Rectangle(start + consumed, 0, end - consumed, 0));
            consumed = end;
        }
        return bounds;
    }

    public int measureCount() {
        return columnWidth.length;
    }

    public int systemCount() {
        return systemCount;
    }

    public int systemOf(int measure) {
        return system[measure];
    }

    public boolean hasMeasure(int track, int measure) {
        return measure >= 0 && measure < score.track(track).measureCount();
    }

    public int measureX(int measure) {
        return columnX[measure];
    }

    public int measureWidth(int measure) {
        return columnWidth[measure] + headWidth[measure];
    }

    /** Cuanto de la columna se lleva la clave y la indicacion de compas al arrancar un sistema. */
    public int headWidth(int measure) {
        return headWidth[measure];
    }

    public boolean startsASystem(int measure) {
        return headWidth[measure] > 0;
    }

    public int totalHeight() {
        return TOP_MARGIN + systemCount * (blockHeightTotal + SYSTEM_GAP) - SYSTEM_GAP + BOTTOM_MARGIN;
    }

    public int trackTop(int track, int measure) {
        int systemY = TOP_MARGIN + systemOf(measure) * (blockHeightTotal + SYSTEM_GAP);
        return systemY + blockTop[track];
    }

    public int trackHeight(int track) {
        return blockHeight(score.track(track));
    }

    public int staffTop(int track, int measure) {
        return trackTop(track, measure) + TRACK_LABEL_HEIGHT + STAFF_HEADROOM;
    }

    public int staffBottom(int track, int measure) {
        return staffTop(track, measure) + STAFF_HEIGHT;
    }

    public int staffLineY(int track, int measure, int line) {
        return staffBottom(track, measure) - line * STAFF_LINE_SPACING;
    }

    /** Y del grado indicado del pentagrama: 0 es la linea inferior y cada grado sube media linea. */
    public int stepY(int track, int measure, int step) {
        return staffBottom(track, measure) - step * (STAFF_LINE_SPACING / 2);
    }

    public int tabTop(int track, int measure) {
        return staffBottom(track, measure) + STAFF_TO_TAB_GAP;
    }

    public int tabBottom(int track, int measure) {
        return tabTop(track, measure) + tabHeightOf(score.track(track));
    }

    public int stringY(int track, int measure, int string) {
        return tabTop(track, measure) + (string - 1) * STRING_SPACING;
    }

    public Rectangle beatBounds(int track, int measure, int beat) {
        Rectangle horizontal = beatBounds.get(track).get(measure).get(beat);
        int top = tabTop(track, measure);
        return new Rectangle(horizontal.x, top, horizontal.width, tabBottom(track, measure) - top);
    }

    public Rectangle staffBeatBounds(int track, int measure, int beat) {
        Rectangle horizontal = beatBounds.get(track).get(measure).get(beat);
        int top = staffTop(track, measure);
        return new Rectangle(horizontal.x, top, horizontal.width, STAFF_HEIGHT);
    }

    public Rectangle measureBounds(int track, int measure) {
        int top = staffTop(track, measure);
        return new Rectangle(columnX[measure], top, measureWidth(measure), tabBottom(track, measure) - top);
    }

    public Optional<Hit> hitTest(int x, int y) {
        for (int track = 0; track < score.trackCount(); track++) {
            for (int measure = 0; measure < score.track(track).measureCount(); measure++) {
                if (!withinBlock(track, measure, x, y)) {
                    continue;
                }
                return Optional.of(new Hit(track, measure, nearestBeat(track, measure, x), nearestString(track, measure, y)));
            }
        }
        return Optional.empty();
    }

    private boolean withinBlock(int track, int measure, int x, int y) {
        int top = trackTop(track, measure);
        return x >= columnX[measure]
                && x < columnX[measure] + measureWidth(measure)
                && y >= top
                && y < top + trackHeight(track);
    }

    private int nearestBeat(int track, int measure, int x) {
        List<Rectangle> beats = beatBounds.get(track).get(measure);
        for (int beat = 0; beat < beats.size(); beat++) {
            Rectangle bounds = beats.get(beat);
            if (x < bounds.x + bounds.width) {
                return beat;
            }
        }
        return beats.size() - 1;
    }

    private int nearestString(int track, int measure, int y) {
        int stringCount = score.track(track).tuning().stringCount();
        int nearest = 1;
        int bestDistance = Integer.MAX_VALUE;
        for (int string = 1; string <= stringCount; string++) {
            int distance = Math.abs(y - stringY(track, measure, string));
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = string;
            }
        }
        return nearest;
    }

    public record Hit(int track, int measure, int beat, int string) {}
}

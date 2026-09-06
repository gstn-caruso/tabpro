package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
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

    /**
     * Los numeros de la tablatura se dibujan centrados sobre su cuerda, asi que
     * la mitad de un digito cae por debajo de la ultima linea y la pista tiene
     * que reservar ese lugar o el numero queda cortado al pie de la hoja.
     */
    public static final int TAB_BOTTOM_PADDING = STRING_SPACING;
    public static final int SYSTEM_GAP = 26;

    public static final int MEASURE_LEFT_PADDING = 26;
    public static final int MEASURE_RIGHT_PADDING = 10;
    public static final int MIN_MEASURE_WIDTH = 76;
    /** Lo que se reserva al arranque de cada sistema para la clave y la indicacion de compas. */
    public static final int SYSTEM_HEAD_WIDTH = 54;
    /** Lo que se reserva en medio de un sistema cuando cambia la armadura o el compas. */
    public static final int SIGNATURE_CHANGE_WIDTH = 30;

    private final Score score;
    private final VisibleTracks visibleTracks;
    private final VisibleNotations visibleNotations;
    private final int[] columnWidth;
    private final int[] headWidth;
    private final int[] columnX;
    private final int[] system;
    private final boolean[] systemStart;
    private final boolean[] signatureChange;
    private final int[] blockTop;
    private final int blockHeightTotal;
    private final int systemCount;
    private final List<List<List<Rectangle>>> beatBounds;
    private final boolean showsDynamicNotes;

    private ScoreLayout(
            Score score,
            VisibleTracks visibleTracks,
            VisibleNotations visibleNotations,
            int[] columnWidth,
            int[] headWidth,
            int[] columnX,
            int[] system,
            boolean[] systemStart,
            boolean[] signatureChange,
            int[] blockTop,
            int blockHeightTotal,
            int systemCount,
            List<List<List<Rectangle>>> beatBounds,
            boolean showsDynamicNotes) {
        this.score = score;
        this.visibleTracks = visibleTracks;
        this.visibleNotations = visibleNotations;
        this.columnWidth = columnWidth;
        this.headWidth = headWidth;
        this.columnX = columnX;
        this.system = system;
        this.systemStart = systemStart;
        this.signatureChange = signatureChange;
        this.blockTop = blockTop;
        this.blockHeightTotal = blockHeightTotal;
        this.systemCount = systemCount;
        this.beatBounds = beatBounds;
        this.showsDynamicNotes = showsDynamicNotes;
    }

    public static ScoreLayout of(Score score, int availableWidth) {
        return of(score, availableWidth, VisibleTracks.all());
    }

    public static ScoreLayout of(Score score, int availableWidth, VisibleTracks visibleTracks) {
        return of(score, availableWidth, visibleTracks, VisibleNotations.both());
    }

    public static ScoreLayout of(
            Score score, int availableWidth, VisibleTracks visibleTracks, VisibleNotations visibleNotations) {
        return of(score, availableWidth, visibleTracks, visibleNotations, false);
    }

    /**
     * @param showsDynamicNotes si esta puesto "Ver > Notas con dinamica [F11]": la cabeza de la
     *         nota se pinta con el gradiente de {@link ScoreColors#forDynamic} en vez de la
     *         tinta pareja de siempre.
     */
    public static ScoreLayout of(
            Score score, int availableWidth, VisibleTracks visibleTracks, VisibleNotations visibleNotations,
            boolean showsDynamicNotes) {
        int measureCount = score.measureCount();
        int[] columnWidth = columnWidths(score, measureCount);
        int usableWidth = Math.max(MIN_MEASURE_WIDTH, availableWidth - LEFT_MARGIN - RIGHT_MARGIN);

        int[] columnX = new int[measureCount];
        int[] headWidth = new int[measureCount];
        int[] system = new int[measureCount];
        boolean[] systemStart = new boolean[measureCount];
        boolean[] signatureChange = new boolean[measureCount];
        int currentSystem = 0;
        int x = LEFT_MARGIN;
        for (int measure = 0; measure < measureCount; measure++) {
            boolean alreadyAtLineStart = x == LEFT_MARGIN;
            boolean startsASystem = alreadyAtLineStart
                    || breaksBefore(score, visibleTracks, measure, x, columnWidth[measure], usableWidth);
            if (!alreadyAtLineStart && startsASystem) {
                currentSystem++;
                x = LEFT_MARGIN;
            }
            boolean changesSignature = !startsASystem && measure > 0 && signatureChangedAt(score, measure);
            headWidth[measure] = startsASystem ? SYSTEM_HEAD_WIDTH : (changesSignature ? SIGNATURE_CHANGE_WIDTH : 0);
            columnX[measure] = x;
            system[measure] = currentSystem;
            systemStart[measure] = startsASystem;
            signatureChange[measure] = changesSignature;
            x += columnWidth[measure] + headWidth[measure];
        }

        int[] blockTop = new int[score.trackCount()];
        int stacked = 0;
        for (int track = 0; track < score.trackCount(); track++) {
            blockTop[track] = stacked;
            if (visibleTracks.shows(track)) {
                stacked += blockHeight(score.track(track), visibleNotations) + TRACK_GAP;
            }
        }
        int blockHeightTotal = Math.max(0, stacked - TRACK_GAP);

        return new ScoreLayout(
                score,
                visibleTracks,
                visibleNotations,
                columnWidth,
                headWidth,
                columnX,
                system,
                systemStart,
                signatureChange,
                blockTop,
                blockHeightTotal,
                measureCount == 0 ? 1 : currentSystem + 1,
                beatBoundsOf(score, columnX, headWidth, columnWidth),
                showsDynamicNotes);
    }

    /**
     * Si este compas tiene que arrancar un sistema nuevo, dado que todavia no arranco uno solo
     * por estar al principio de la linea. "Compas > Salto de linea" deja elegir por compas:
     * forzado siempre corta ahi aunque sobre ancho; impedido nunca corta ahi, y si el ancho lo
     * pedia el sistema actual se estira y el corte se corre al compas siguiente que si pueda
     * arrancar uno; automatico es el comportamiento de siempre, por ancho disponible.
     */
    private static boolean breaksBefore(
            Score score, VisibleTracks visibleTracks, int measure, int x, int width, int usableWidth) {
        LineBreak lineBreak = lineBreakAt(score, visibleTracks, measure);
        if (lineBreak == LineBreak.FORCED) {
            return true;
        }
        if (lineBreak == LineBreak.PREVENTED) {
            return false;
        }
        return x + width > LEFT_MARGIN + usableWidth;
    }

    /**
     * De donde sale el salto de linea de un compas: fuera de la vista multipista vale solo para
     * la pista activa, que puede tener su propia organizacion de sistemas; en la vista
     * multipista todas comparten la misma, la que arrastra la primera pista (igual que el resto
     * de los atributos del compas, que siempre son los mismos en todas las pistas).
     */
    private static LineBreak lineBreakAt(Score score, VisibleTracks visibleTracks, int measure) {
        if (visibleTracks.multitrack()) {
            return score.attributesOf(measure).lineBreak();
        }
        Track track = score.track(visibleTracks.activeTrack());
        int clamped = Math.clamp(measure, 0, track.measureCount() - 1);
        return track.attributesOf(clamped).lineBreak();
    }

    /** Si la armadura o el compas de este comienzo difieren de los del compas anterior. */
    private static boolean signatureChangedAt(Score score, int measure) {
        boolean timeChanged = !score.timeSignatureOf(measure).equals(score.timeSignatureOf(measure - 1));
        boolean keyChanged = !score.attributesOf(measure).keySignature().equals(score.attributesOf(measure - 1).keySignature());
        return timeChanged || keyChanged;
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

    /**
     * El alto de una pista, con la franja de la notacion que no se dibuja encogida a cero. Sin
     * pentagrama la tablatura sube a su lugar; sin tablatura el aire que la separaba queda
     * abajo del pentagrama, que es donde van la digitacion y la letra.
     */
    private static int blockHeight(Track track, VisibleNotations notations) {
        int staff = notations.showsStandardNotationOf(track) ? STAFF_HEIGHT + STAFF_TO_TAB_GAP : 0;
        int tab = notations.showsTablatureOf(track) ? tabHeightOf(track) + TAB_BOTTOM_PADDING : 0;
        return TRACK_LABEL_HEIGHT + STAFF_HEADROOM + staff + tab;
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
        return systemStart[measure];
    }

    /** Si en medio de un sistema cambia la armadura o el compas, y hay que volver a escribirlos. */
    public boolean hasSignatureChange(int measure) {
        return signatureChange[measure];
    }

    public int totalHeight() {
        return TOP_MARGIN + systemCount * (blockHeightTotal + SYSTEM_GAP) - SYSTEM_GAP + BOTTOM_MARGIN;
    }

    /** El techo de un sistema entero, con todas sus pistas apiladas adentro. */
    public int systemTop(int system) {
        return TOP_MARGIN + system * (blockHeightTotal + SYSTEM_GAP);
    }

    /** Cuanto mide de alto un sistema, con todas las pistas ya apiladas. */
    public int systemHeight() {
        return blockHeightTotal;
    }

    public int trackTop(int track, int measure) {
        int systemY = TOP_MARGIN + systemOf(measure) * (blockHeightTotal + SYSTEM_GAP);
        return systemY + blockTop[track];
    }

    public int trackHeight(int track) {
        return shows(track) ? blockHeight(score.track(track), visibleNotations) : 0;
    }

    public boolean showsStandardNotation(int track) {
        return visibleNotations.showsStandardNotationOf(score.track(track));
    }

    public boolean showsTablature(int track) {
        return visibleNotations.showsTablatureOf(score.track(track));
    }

    /** "Ver > Notas con dinamica [F11]": si la cabeza de la nota va con el gradiente de {@link ScoreColors#forDynamic}. */
    public boolean showsDynamicNotes() {
        return showsDynamicNotes;
    }

    /** Si esta pista se dibuja: la vista multipista y la mesa de mezcla deciden cuales se ven. */
    public boolean shows(int track) {
        return visibleTracks.shows(track);
    }

    /**
     * La primera pista que se ve. Lo que vale para el compas entero y no para una pista
     * —repeticiones, direcciones, marcadores— se dibuja una sola vez, sobre esa.
     */
    public int firstShownTrack() {
        for (int track = 0; track < score.trackCount(); track++) {
            if (shows(track)) {
                return track;
            }
        }
        return 0;
    }

    public int staffTop(int track, int measure) {
        return trackTop(track, measure) + TRACK_LABEL_HEIGHT + STAFF_HEADROOM;
    }

    public int staffBottom(int track, int measure) {
        return staffTop(track, measure) + (showsStandardNotation(track) ? STAFF_HEIGHT : 0);
    }

    public int staffLineY(int track, int measure, int line) {
        return staffBottom(track, measure) - line * STAFF_LINE_SPACING;
    }

    /** Y del grado indicado del pentagrama: 0 es la linea inferior y cada grado sube media linea. */
    public int stepY(int track, int measure, int step) {
        return staffBottom(track, measure) - step * (STAFF_LINE_SPACING / 2);
    }

    public int tabTop(int track, int measure) {
        return staffBottom(track, measure) + (showsStandardNotation(track) ? STAFF_TO_TAB_GAP : 0);
    }

    public int tabBottom(int track, int measure) {
        return tabTop(track, measure) + (showsTablature(track) ? tabHeightOf(score.track(track)) : 0);
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
            if (!shows(track)) {
                continue;
            }
            for (int measure = 0; measure < score.track(track).measureCount(); measure++) {
                if (!withinBlock(track, measure, x, y)) {
                    continue;
                }
                return Optional.of(new Hit(track, measure, nearestBeat(track, measure, x), nearestString(track, measure, x, y)));
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

    /**
     * La cuerda mas cercana al clic. Arriba de la tablatura se busca entre las notas del beat
     * mas cercano cual queda mas cerca en el pentagrama; en la tablatura, la cuerda mas cercana
     * por distancia vertical entre lineas.
     */
    private int nearestString(int track, int measure, int x, int y) {
        if (y < tabTop(track, measure)) {
            return nearestStringOnStaff(track, measure, x, y);
        }
        return nearestStringOnTab(track, measure, y);
    }

    private int nearestStringOnTab(int track, int measure, int y) {
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

    private int nearestStringOnStaff(int track, int measure, int x, int y) {
        Track trackModel = score.track(track);
        Beat beat = trackModel.measure(measure).beat(nearestBeat(track, measure, x));
        if (beat.notes().isEmpty()) {
            return nearestStringOnTab(track, measure, y);
        }
        Clef clef = Clef.forTuning(trackModel.tuning());
        int nearest = beat.notes().get(0).string();
        int bestDistance = Integer.MAX_VALUE;
        for (Note note : beat.notes()) {
            StaffPosition position = StaffPosition.of(trackModel.pitchOf(note), clef);
            int distance = Math.abs(y - stepY(track, measure, position.step()));
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = note.string();
            }
        }
        return nearest;
    }

    public record Hit(int track, int measure, int beat, int string) {}
}

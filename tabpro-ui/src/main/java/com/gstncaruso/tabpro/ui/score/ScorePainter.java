package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Notation;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Optional;

/**
 * Dibuja la partitura entera: cada pista con su pentagrama arriba y su tablatura abajo, una
 * debajo de la otra, sistema por sistema.
 */
public final class ScorePainter {

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private ScorePainter() {
    }

    public static void paint(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor, Playhead playhead) {
        paint(g, layout, score, cursor, playhead, Optional.empty());
    }

    public static void paint(
            Graphics2D g, ScoreLayout layout, Score score, Cursor cursor, Playhead playhead,
            Optional<Selection> selection) {
        paint(g, layout, score, cursor, playhead, selection, Optional.of(cursor.voice()), true);
    }

    /** Igual que {@link #paint}, pero puede saltear el fondo oscuro: el Modo Pagina dibuja sobre
     * una hoja que ya esta pintada, con los colores que le pasa el {@link PaperGraphics}. */
    static void paint(
            Graphics2D g, ScoreLayout layout, Score score, Cursor cursor, Playhead playhead,
            Optional<Selection> selection, Optional<VoicePart> highlightedVoice, boolean paintBackground) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (paintBackground) {
            paintBackground(g);
        }

        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            if (layout.shows(trackIndex)) {
                paintTrack(g, layout, score, trackIndex, cursor, highlightedVoice);
            }
        }
        paintPlayingLines(g, layout, score, playhead);
        selection.ifPresent(sel -> paintSelection(g, layout, score, sel));
        if (showsTheEditingCursor(score, cursor)) {
            paintCursor(g, layout, score, cursor);
            paintCorrespondingMark(g, layout, score, cursor);
        }
    }

    /**
     * La hoja impresa o exportada no lleva cursor de edicion: se lo pide con un
     * cursor que apunta fuera de la partitura.
     */
    private static boolean showsTheEditingCursor(Score score, Cursor cursor) {
        return cursor.track() >= 0 && cursor.track() < score.trackCount();
    }

    private static void paintBackground(Graphics2D g) {
        Rectangle clip = g.getClipBounds();
        g.setColor(ScoreColors.BACKGROUND);
        g.fill(clip == null ? new Rectangle(0, 0, 4000, 4000) : clip);
    }

    private static void paintTrack(
            Graphics2D g, ScoreLayout layout, Score score, int trackIndex, Cursor cursor,
            Optional<VoicePart> highlightedVoice) {
        Track track = score.track(trackIndex);
        Clef clef = Clef.forTuning(track.tuning());
        TrackDisplay display = track.settings().display();
        boolean standardNotation = layout.showsStandardNotation(trackIndex);
        boolean tablature = layout.showsTablature(trackIndex);
        boolean selected = cursor.track() == trackIndex;

        for (int measureIndex = 0; measureIndex < track.measureCount(); measureIndex++) {
            boolean beingEdited = selected && cursor.measure() == measureIndex;
            paintIncompleteMeasureBackground(g, layout, track, trackIndex, measureIndex, beingEdited);

            if (standardNotation) {
                StaffPainter.paintStaffLines(g, layout, trackIndex, measureIndex);
            }
            if (tablature) {
                TabPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            }
            if (layout.startsASystem(measureIndex)) {
                paintTrackLabel(g, layout, track, trackIndex, measureIndex, selected);
                if (standardNotation) {
                    StaffPainter.paintClef(g, layout, clef, trackIndex, measureIndex);
                    StaffPainter.paintTimeSignature(g, layout, track, trackIndex, measureIndex,
                            layout.measureX(measureIndex) + ScoreLayout.SYSTEM_HEAD_WIDTH - 20);
                }
                if (tablature) {
                    TabPainter.paintTabMark(g, layout, track, trackIndex, measureIndex);
                    if (display.tuningLegend()) {
                        TabPainter.paintTuningLegend(g, layout, track, trackIndex, measureIndex);
                    }
                }
            }
            TabPainter.paintMeasureNumber(g, layout, track, trackIndex, measureIndex);
            if (standardNotation) {
                StaffPainter.paintMeasure(g, layout, track, clef, trackIndex, measureIndex, highlightedVoice);
            }
            if (tablature) {
                if (track.isPercussion()) {
                    PercussionPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
                } else {
                    TabPainter.paintFrets(g, layout, track, trackIndex, measureIndex);
                    TabNotationPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
                }
                TabSymbolPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
                if (display.rhythmOnTablature()) {
                    TabPainter.paintRhythm(g, layout, track, trackIndex, measureIndex);
                }
            }
            ChordDiagramPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            ParameterChangePainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            BarStructurePainter.paintPerTrack(g, layout, track, clef, trackIndex, measureIndex);
            if (trackIndex == layout.firstShownTrack()) {
                BarStructurePainter.paintScoreWide(g, layout, track, trackIndex, measureIndex);
            }
        }
        LyricsPainter.paintTrack(g, layout, score, trackIndex);
    }

    private static void paintTrackLabel(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, boolean selected) {
        g.setFont(LABEL_FONT);
        g.setColor(selected ? ScoreColors.INK : ScoreColors.LABEL);
        int y = layout.trackTop(trackIndex, measureIndex) + ScoreLayout.TRACK_LABEL_HEIGHT - 4;
        int x = layout.measureX(measureIndex);
        if (selected) {
            g.setColor(ScoreColors.ACCENT);
            g.fillRect(x - 8, y - 9, 3, 11);
            g.setColor(ScoreColors.INK);
        }
        g.drawString(track.name(), x, y);
    }

    /**
     * La linea de reproduccion se dibuja al final, encima de toda la musica: si se pintara antes
     * que las notas, cualquier cabeza, plica o numero de traste que cayera en su columna la
     * taparia por completo.
     */
    private static void paintPlayingLines(Graphics2D g, ScoreLayout layout, Score score, Playhead playhead) {
        soundingNow(layout, score, playhead)
                .ifPresent(position -> paintPlaying(g, layout, position.track(), position));
    }

    /**
     * Todas las pistas suenan a la vez, asi que la reproduccion esta en un solo lugar y le toca
     * una sola linea. Como cada pista parte el compas distinto -negras en la guitarra, una
     * redonda en el bajo- el arranque del beat que suena cae en una x distinta segun la pista;
     * la que vale es la que arranco mas tarde, que es la mas cercana al instante que se oye.
     */
    private static Optional<BeatPosition> soundingNow(ScoreLayout layout, Score score, Playhead playhead) {
        Optional<BeatPosition> latest = Optional.empty();
        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            if (!layout.shows(trackIndex)) {
                continue;
            }
            Optional<BeatPosition> here = playhead.on(trackIndex);
            if (here.isPresent() && (latest.isEmpty() || startsLater(layout, here.get(), latest.get()))) {
                latest = here;
            }
        }
        return latest;
    }

    private static boolean startsLater(ScoreLayout layout, BeatPosition one, BeatPosition other) {
        return startOf(layout, one) > startOf(layout, other);
    }

    private static int startOf(ScoreLayout layout, BeatPosition position) {
        return layout.beatBounds(position.track(), position.measure(), position.beat()).x;
    }

    /**
     * Una linea vertical fina que senala donde va la reproduccion, en vez de un bloque que tapa
     * la musica. Cruza el sistema entero de punta a punta -no solo la pista que esta sonando-
     * porque todas las pistas del sistema suenan juntas.
     */
    private static void paintPlaying(Graphics2D g, ScoreLayout layout, int trackIndex, BeatPosition position) {
        Rectangle beat = layout.beatBounds(trackIndex, position.measure(), position.beat());
        int top = layout.systemTop(layout.systemOf(position.measure()));
        int bottom = top + layout.systemHeight();
        g.setColor(ScoreColors.PLAYING);
        // fillRect en vez de drawLine: una linea trazada de un pixel de ancho cae justo en el
        // limite entre dos columnas y el antialiasing la reparte mitad y mitad, dejandola
        // desteñida. Un rectangulo de una columna cae adentro de un pixel entero y sale nitida.
        g.fillRect(beat.x, top, 1, bottom - top);
    }

    /** El compas que no suma lo que su medida pide se tine de rojo, salvo el que se esta editando. */
    private static void paintIncompleteMeasureBackground(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, boolean beingEdited) {
        Measure measure = track.measure(measureIndex);
        if (measure.isComplete() || beingEdited) {
            return;
        }
        Rectangle bounds = layout.measureBounds(trackIndex, measureIndex);
        Color warn = ScoreColors.INCOMPLETE_MEASURE;
        g.setColor(new Color(warn.getRed(), warn.getGreen(), warn.getBlue(), 40));
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private static void paintSelection(Graphics2D g, ScoreLayout layout, Score score, Selection selection) {
        Track track = score.track(selection.track());
        g.setColor(ScoreColors.SELECTION);
        for (int measure = selection.fromMeasure();
                measure <= selection.toMeasure() && measure < track.measureCount(); measure++) {
            // Un compas entero se pinta de punta a punta, no solo donde caen los beats de la voz
            // principal: el manual dice que las acciones valen para las dos voces, y el compas
            // siempre deja un margen (cabecera, padding) que ningun beat pisa.
            if (selection.wholeMeasures()) {
                Rectangle bounds = layout.measureBounds(selection.track(), measure);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                continue;
            }
            int beatCount = track.measure(measure).beats().size();
            for (int beat = 0; beat < beatCount; beat++) {
                if (!selection.covers(measure, beat)) {
                    continue;
                }
                Rectangle bounds = layout.beatBounds(selection.track(), measure, beat);
                int top = layout.staffTop(selection.track(), measure);
                g.fillRect(bounds.x, top, bounds.width, layout.tabBottom(selection.track(), measure) - top);
            }
        }
    }

    /**
     * El cursor de edicion: una linea vertical fina y roja en el arranque del beat actual, que
     * cruza el pentagrama y la tablatura de la pista que se esta editando. A diferencia de la
     * linea de reproduccion -que cruza el sistema entero porque todas las pistas suenan juntas-
     * esta es de una sola pista, porque solo se edita una a la vez.
     *
     * <p>El cuadradito que marca donde esta parado el cursor va en la notacion activa -manual,
     * linea 769: la tablatura si se edita ahi, la cabeza de la nota en el pentagrama si se edita
     * en el pentagrama-; ver {@link #paintCorrespondingMark} para la marca gris, que va siempre
     * en la otra.
     */
    private static void paintCursor(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        int x = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat()).x;
        int top = layout.staffTop(cursor.track(), cursor.measure());
        int bottom = layout.tabBottom(cursor.track(), cursor.measure());
        g.setColor(ScoreColors.CURSOR);
        // fillRect en vez de drawLine: la misma razon que en paintPlaying, para que el
        // antialiasing no reparta la linea entre dos columnas y la deje desteñida.
        g.fillRect(x, top, 1, bottom - top);
        if (cursor.notation() == Notation.STANDARD) {
            paintCursorNote(g, layout, score, cursor);
        } else {
            paintCursorString(g, layout, cursor, x);
        }
    }

    /** La linea sola no dice en que cuerda esta parado el cursor: a esa altura se la ensancha. */
    private static void paintCursorString(Graphics2D g, ScoreLayout layout, Cursor cursor, int x) {
        int y = layout.stringY(cursor.track(), cursor.measure(), cursor.string());
        g.fillRect(x - 2, y - 2, 5, 5);
    }

    /**
     * El cursor sobre la cabeza de la nota, cuando se edita en el pentagrama: la misma altura que
     * agregaria Enter -la de la nota que ya suena en la cuerda del cursor, o la de la cuerda al
     * aire si esta en silencio-, para que el cuadradito muestre de verdad donde va a caer la
     * proxima nota.
     */
    private static void paintCursorNote(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        Track track = score.track(cursor.track());
        Measure measure = track.measure(cursor.measure());
        Beat beat = measure.voice(cursor.voice()).beats().get(cursor.beat());
        Note pointer = beat.noteOn(cursor.string()).orElseGet(() -> new Note(cursor.string(), 0));
        Clef clef = Clef.forTuning(track.tuning());
        int octaveShift = measure.attributes().octaveMark().staffStepShift();
        StaffPosition position = StaffPainter.positionOf(track, clef, pointer, octaveShift);
        int y = layout.stepY(cursor.track(), cursor.measure(), position.step());
        Rectangle bounds = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
        g.fillRect(bounds.x + 1, y - 5, bounds.width - 2, 10);
    }

    /** La marca del cursor en la OTRA notacion (manual, linea 769-770): gris, y solo si hay una
     * nota real que marcar -a diferencia del cuadradito del cursor, que siempre se ve. */
    private static void paintCorrespondingMark(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        if (cursor.notation() == Notation.STANDARD) {
            paintCorrespondingString(g, layout, score, cursor);
        } else {
            paintCorrespondingNote(g, layout, score, cursor);
        }
    }

    /** La nota que corresponde al cursor en la otra notacion, marcada con un rectangulo gris. */
    private static void paintCorrespondingNote(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        Track track = score.track(cursor.track());
        if (cursor.measure() >= track.measureCount()) {
            return;
        }
        Measure measure = track.measure(cursor.measure());
        var beats = measure.voice(cursor.voice()).beats();
        if (cursor.beat() >= beats.size()) {
            return;
        }
        Beat beat = beats.get(cursor.beat());
        beat.noteOn(cursor.string()).ifPresent(note -> {
            Clef clef = Clef.forTuning(track.tuning());
            // La misma cuenta que StaffPainter para ubicar la cabeza -8va/8vb/15ma/15mb
            // incluido-, no una copia: si la formula vive en dos lados, vuelven a separarse.
            int octaveShift = measure.attributes().octaveMark().staffStepShift();
            StaffPosition position = StaffPainter.positionOf(track, clef, note, octaveShift);
            int y = layout.stepY(cursor.track(), cursor.measure(), position.step());
            Rectangle bounds = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
            g.setColor(ScoreColors.CORRESPONDING_NOTE);
            g.fillRect(bounds.x + 1, y - 5, bounds.width - 2, 10);
        });
    }

    /** La cuerda que corresponde al cursor cuando se edita en el pentagrama: el numero de traste
     * que ya se ve siempre en la tablatura, pero marcado de gris en vez de en el color del cursor
     * -la marca cambio de lado, como pide el manual-. Solo si hay una nota real en esa cuerda. */
    private static void paintCorrespondingString(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        Track track = score.track(cursor.track());
        if (cursor.measure() >= track.measureCount()) {
            return;
        }
        Measure measure = track.measure(cursor.measure());
        var beats = measure.voice(cursor.voice()).beats();
        if (cursor.beat() >= beats.size()) {
            return;
        }
        Beat beat = beats.get(cursor.beat());
        beat.noteOn(cursor.string()).ifPresent(note -> {
            int x = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat()).x;
            int y = layout.stringY(cursor.track(), cursor.measure(), cursor.string());
            g.setColor(ScoreColors.CORRESPONDING_NOTE);
            g.fillRect(x - 2, y - 2, 5, 5);
        });
    }
}

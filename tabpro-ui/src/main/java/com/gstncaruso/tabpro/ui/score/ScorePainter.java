package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.BasicStroke;
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
        paint(g, layout, score, cursor, playhead, selection, true);
    }

    /** Igual que {@link #paint}, pero puede saltear el fondo oscuro: lo usa el Modo Pagina para
     * dibujar solo la tinta sobre un lienzo transparente que despues se invierte y se pega sobre
     * la hoja clara (ver {@link PaperRenderer}). */
    static void paint(
            Graphics2D g, ScoreLayout layout, Score score, Cursor cursor, Playhead playhead,
            Optional<Selection> selection, boolean paintBackground) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (paintBackground) {
            paintBackground(g);
        }

        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            paintTrack(g, layout, score, trackIndex, cursor, playhead);
        }
        selection.ifPresent(sel -> paintSelection(g, layout, score, sel));
        if (showsTheEditingCursor(score, cursor)) {
            paintCursor(g, layout, cursor);
            paintCorrespondingNote(g, layout, score, cursor);
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
            Graphics2D g, ScoreLayout layout, Score score, int trackIndex, Cursor cursor, Playhead playhead) {
        Track track = score.track(trackIndex);
        Clef clef = Clef.forTuning(track.tuning());
        TrackDisplay display = track.settings().display();
        boolean selected = cursor.track() == trackIndex;

        playhead.on(trackIndex).ifPresent(position -> paintPlaying(g, layout, trackIndex, position));

        for (int measureIndex = 0; measureIndex < track.measureCount(); measureIndex++) {
            boolean beingEdited = selected && cursor.measure() == measureIndex;
            paintIncompleteMeasureBackground(g, layout, track, trackIndex, measureIndex, beingEdited);

            if (display.standardNotation()) {
                StaffPainter.paintStaffLines(g, layout, trackIndex, measureIndex);
            }
            if (display.tablature()) {
                TabPainter.paintMeasure(g, layout, track, trackIndex, measureIndex);
            }
            if (layout.startsASystem(measureIndex)) {
                paintTrackLabel(g, layout, track, trackIndex, measureIndex, selected);
                if (display.standardNotation()) {
                    StaffPainter.paintClef(g, layout, clef, trackIndex, measureIndex);
                    StaffPainter.paintTimeSignature(g, layout, track, trackIndex, measureIndex,
                            layout.measureX(measureIndex) + ScoreLayout.SYSTEM_HEAD_WIDTH - 20);
                }
                if (display.tablature()) {
                    TabPainter.paintTabMark(g, layout, track, trackIndex, measureIndex);
                    if (display.tuningLegend()) {
                        TabPainter.paintTuningLegend(g, layout, track, trackIndex, measureIndex);
                    }
                }
            }
            TabPainter.paintMeasureNumber(g, layout, track, trackIndex, measureIndex);
            if (display.standardNotation()) {
                StaffPainter.paintMeasure(g, layout, track, clef, trackIndex, measureIndex, cursor.voice());
            }
            if (display.tablature()) {
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
            BarStructurePainter.paintPerTrack(g, layout, track, clef, trackIndex, measureIndex);
            if (trackIndex == 0) {
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

    private static void paintPlaying(Graphics2D g, ScoreLayout layout, int trackIndex, BeatPosition position) {
        Rectangle beat = layout.beatBounds(trackIndex, position.measure(), position.beat());
        int top = layout.staffTop(trackIndex, position.measure());
        g.setColor(ScoreColors.PLAYING);
        g.fillRect(beat.x, top, beat.width, layout.tabBottom(trackIndex, position.measure()) - top);
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

    private static void paintCursor(Graphics2D g, ScoreLayout layout, Cursor cursor) {
        Rectangle beat = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
        int y = layout.stringY(cursor.track(), cursor.measure(), cursor.string())
                - ScoreLayout.STRING_SPACING / 2 + 1;
        g.setColor(cursorColor());
        g.setStroke(new BasicStroke(2));
        g.drawRect(beat.x + 1, y, beat.width - 2, ScoreLayout.STRING_SPACING - 2);
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
            StaffPosition position = StaffPosition.of(track.tuning().pitchOf(note), clef);
            int y = layout.stepY(cursor.track(), cursor.measure(), position.step());
            Rectangle bounds = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
            g.setColor(ScoreColors.CORRESPONDING_NOTE);
            g.fillRect(bounds.x + 1, y - 5, bounds.width - 2, 10);
        });
    }

    private static Color cursorColor() {
        return ScoreColors.CURSOR;
    }
}

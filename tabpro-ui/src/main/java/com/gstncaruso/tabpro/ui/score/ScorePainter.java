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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ScorePainter {

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
        if (showsTheEditingCursor(score, cursor)) {
            paintCursorTrail(g, layout, cursor);
        }
        paintPlayingLines(g, layout, score, playhead);
        selection.ifPresent(sel -> paintSelection(g, layout, score, sel));
        if (showsTheEditingCursor(score, cursor)) {
            paintCursor(g, layout, score, cursor);
            paintCorrespondingMark(g, layout, score, cursor);
        }
    }

    private static void paintCursorTrail(Graphics2D g, ScoreLayout layout, Cursor cursor) {
        int x = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat()).x;
        int top = layout.systemTop(layout.systemOf(cursor.measure()));
        int bottom = top + layout.systemHeight();
        g.setColor(ScoreColors.CURSOR_DIMMED);
        g.fillRect(x, top, 1, bottom - top);
    }

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
        Rectangle clip = g.getClipBounds();
        int firstVisibleSystem = clip == null ? 0 : layout.systemAt(clip.y);
        int lastVisibleSystem = clip == null ? layout.systemCount() - 1 : layout.systemAt(clip.y + clip.height);
        int firstMeasureIndex = layout.firstMeasureOfSystem(firstVisibleSystem);
        int lastMeasureIndex = Math.min(layout.lastMeasureOfSystem(lastVisibleSystem), track.measureCount() - 1);

        for (int measureIndex = firstMeasureIndex; measureIndex <= lastMeasureIndex; measureIndex++) {
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
                if (measureIndex == 0) {
                    ParameterChangePainter.paintInitialTempo(g, layout, track, trackIndex, measureIndex, score.tempo());
                }
            }
        }
        LyricsPainter.paintTrack(g, layout, score, trackIndex);
    }

    private static void paintTrackLabel(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, boolean selected) {
        g.setFont(ScoreFonts.TRACK_LABEL_FONT);
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

    private static void paintPlayingLines(Graphics2D g, ScoreLayout layout, Score score, Playhead playhead) {
        soundingNow(layout, score, playhead)
                .ifPresent(position -> paintPlaying(g, layout, position.track(), position));
    }

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

    private static void paintPlaying(Graphics2D g, ScoreLayout layout, int trackIndex, BeatPosition position) {
        Rectangle beat = layout.beatBounds(trackIndex, position.measure(), position.beat());
        int top = layout.systemTop(layout.systemOf(position.measure()));
        int bottom = top + layout.systemHeight();
        g.setColor(ScoreColors.PLAYING);
        // fillRect instead of drawLine: a one-pixel-wide stroked line falls exactly on the
        // boundary between two columns, and antialiasing splits it half and half, leaving it
        // faded. A one-column-wide fill falls inside a whole pixel and comes out crisp.
        g.fillRect(beat.x, top, 1, bottom - top);
    }

    static final Color INCOMPLETE_MEASURE_TINT = new Color(
            ScoreColors.INCOMPLETE_MEASURE.getRed(),
            ScoreColors.INCOMPLETE_MEASURE.getGreen(),
            ScoreColors.INCOMPLETE_MEASURE.getBlue(),
            40);

    private static void paintIncompleteMeasureBackground(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, boolean beingEdited) {
        Measure measure = track.measure(measureIndex);
        if (measure.isComplete() || beingEdited) {
            return;
        }
        Rectangle bounds = layout.measureBounds(trackIndex, measureIndex);
        g.setColor(INCOMPLETE_MEASURE_TINT);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(ScoreColors.INCOMPLETE_MEASURE);
        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    private static void paintSelection(Graphics2D g, ScoreLayout layout, Score score, Selection selection) {
        Track track = score.track(selection.track());
        Map<Integer, Rectangle> areaPerSystem = new LinkedHashMap<>();
        for (int measure = selection.fromMeasure();
                measure <= selection.toMeasure() && measure < track.measureCount(); measure++) {
            int system = layout.systemOf(measure);
            if (selection.wholeMeasures()) {
                grow(areaPerSystem, system, layout.measureBounds(selection.track(), measure));
                continue;
            }
            int beatCount = track.measure(measure).beats().size();
            int top = layout.staffTop(selection.track(), measure);
            int bottom = layout.tabBottom(selection.track(), measure);
            for (int beat = 0; beat < beatCount; beat++) {
                if (!selection.covers(measure, beat)) {
                    continue;
                }
                Rectangle bounds = layout.beatBounds(selection.track(), measure, beat);
                grow(areaPerSystem, system, new Rectangle(bounds.x, top, bounds.width, bottom - top));
            }
        }
        areaPerSystem.values().forEach(area -> paintSelectedArea(g, area));
    }

    private static void grow(Map<Integer, Rectangle> areaPerSystem, int system, Rectangle addition) {
        areaPerSystem.merge(system, addition, Rectangle::union);
    }

    private static void paintSelectedArea(Graphics2D g, Rectangle bounds) {
        g.setColor(ScoreColors.SELECTION);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(ScoreColors.SELECTION_BORDER);
        g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }

    private static void paintCursor(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        int x = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat()).x;
        int top = layout.staffTop(cursor.track(), cursor.measure());
        int bottom = layout.tabBottom(cursor.track(), cursor.measure());
        g.setColor(ScoreColors.CURSOR);
        // fillRect instead of drawLine: same reason as in paintPlaying, so antialiasing does
        // not split the line between two columns and leave it faded.
        g.fillRect(x, top, 1, bottom - top);
        if (cursor.notation() == Notation.STANDARD) {
            paintCursorNote(g, layout, score, cursor);
        } else {
            paintCursorString(g, layout, cursor, x);
        }
    }

    private static void paintCursorString(Graphics2D g, ScoreLayout layout, Cursor cursor, int x) {
        int y = layout.stringY(cursor.track(), cursor.measure(), cursor.string());
        g.fillRect(x - 2, y - 2, 5, 5);
    }

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

    private static void paintCorrespondingMark(Graphics2D g, ScoreLayout layout, Score score, Cursor cursor) {
        if (cursor.notation() == Notation.STANDARD) {
            paintCorrespondingString(g, layout, score, cursor);
        } else {
            paintCorrespondingNote(g, layout, score, cursor);
        }
    }

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
            int octaveShift = measure.attributes().octaveMark().staffStepShift();
            StaffPosition position = StaffPainter.positionOf(track, clef, note, octaveShift);
            int y = layout.stepY(cursor.track(), cursor.measure(), position.step());
            Rectangle bounds = layout.beatBounds(cursor.track(), cursor.measure(), cursor.beat());
            g.setColor(ScoreColors.CORRESPONDING_NOTE);
            g.fillRect(bounds.x + 1, y - 5, bounds.width - 2, 10);
        });
    }

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

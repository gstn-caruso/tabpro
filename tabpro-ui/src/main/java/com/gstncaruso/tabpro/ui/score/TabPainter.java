package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.notation.Beaming;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** La tablatura de una pista: sus cuerdas, sus barras de compas y los trastes escritos encima. */
final class TabPainter {

    private static final Font FRET_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private TabPainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Rectangle column = new Rectangle(
                layout.measureX(measureIndex), 0, layout.measureWidth(measureIndex), 0);
        int stringCount = track.tuning().stringCount();

        g.setColor(ScoreColors.STAFF_LINE);
        for (int string = 1; string <= stringCount; string++) {
            int y = layout.stringY(trackIndex, measureIndex, string);
            g.drawLine(column.x, y, column.x + column.width, y);
        }

        int top = layout.stringY(trackIndex, measureIndex, 1);
        int bottom = layout.stringY(trackIndex, measureIndex, stringCount);
        g.setColor(ScoreColors.BAR_LINE);
        g.drawLine(column.x, top, column.x, bottom);
        g.drawLine(column.x + column.width, top, column.x + column.width, bottom);
    }

    static void paintFrets(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            for (Note note : beat.notes()) {
                paintFret(g, layout, trackIndex, measureIndex, beatIndex, note);
            }
        }
    }

    private static void paintFret(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.stringY(trackIndex, measureIndex, note.string());
        String fret = fretText(note);

        g.setFont(FRET_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(fret);

        clearBehindTheDigits(g, centerX, y, textWidth);

        g.setColor(ScoreColors.INK);
        g.drawString(fret, centerX - textWidth / 2, y + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    /** La nota muerta se escribe X y la fantasma entre parentesis, como pide el manual. */
    private static String fretText(Note note) {
        if (note.has(Ornament.DEAD)) {
            return "X";
        }
        String digits = String.valueOf(note.fret());
        return note.has(Ornament.GHOST) ? "(" + digits + ")" : digits;
    }

    private static void clearBehindTheDigits(Graphics2D g, int centerX, int y, int textWidth) {
        int width = textWidth + 5;
        int height = ScoreLayout.STRING_SPACING - 2;
        g.setColor(ScoreColors.BACKGROUND);
        g.fillRect(centerX - width / 2, y - height / 2, width, height);
    }

    /** La marca "TAB" que abre cada sistema, al estilo de las tablaturas impresas. */
    static void paintTabMark(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        int stringCount = track.tuning().stringCount();
        int top = layout.stringY(trackIndex, measureIndex, 1);
        int bottom = layout.stringY(trackIndex, measureIndex, stringCount);
        int letterHeight = (bottom - top) / 3;

        g.setColor(ScoreColors.LABEL);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, letterHeight)));
        FontMetrics metrics = g.getFontMetrics();
        String[] letters = {"T", "A", "B"};
        int x = layout.measureX(measureIndex) + 10;
        for (int letter = 0; letter < letters.length; letter++) {
            int y = top + letterHeight * letter + (letterHeight + metrics.getAscent()) / 2 - 2;
            g.drawString(letters[letter], x, y);
        }
    }

    /** La leyenda de afinacion: el nombre de cada cuerda al aire, de punta a punta de la tab. */
    static void paintTuningLegend(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        int stringCount = track.tuning().stringCount();
        g.setColor(ScoreColors.LABEL);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        int x = layout.measureX(measureIndex) + 26;
        for (int string = 1; string <= stringCount; string++) {
            String name = com.gstncaruso.tabpro.core.notation.PitchName.of(track.tuning().pitchOfString(string)).text();
            int y = layout.stringY(trackIndex, measureIndex, string) + 3;
            g.drawString(name, x, y);
        }
    }

    /** El ritmo de cada golpe dibujado como una plica suelta arriba de la tablatura, para las
     * pistas que piden ver la figura sin abrir el pentagrama. */
    static void paintRhythm(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            if (beat.duration().value() == NoteValue.WHOLE) {
                continue;
            }
            Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
            int centerX = bounds.x + bounds.width / 2;
            int baseY = layout.tabTop(trackIndex, measureIndex) - 4;
            int topY = baseY - 14;

            g.setColor(ScoreColors.LABEL);
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(centerX, baseY, centerX, topY);
            int flags = Beaming.beamCount(beat.duration().value());
            for (int flag = 0; flag < flags; flag++) {
                int y = topY + flag * 3;
                g.drawLine(centerX, y, centerX + 5, y + 3);
            }
        }
    }

    static void paintMeasureNumber(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        boolean complete = track.measure(measureIndex).isComplete();
        Color color = complete ? ScoreColors.MUTED_INK : ScoreColors.WARNING;
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.drawString(
                String.valueOf(measureIndex + 1),
                layout.measureX(measureIndex) + 3,
                layout.staffTop(trackIndex, measureIndex) - 6);
    }
}

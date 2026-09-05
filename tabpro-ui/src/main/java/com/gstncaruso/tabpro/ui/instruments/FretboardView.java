package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.JComponent;

/** El mastil, con las notas del beat en el que estas parado marcadas donde se pisan. */
public final class FretboardView extends JComponent {

    public static final int FRETS = Note.MAX_FRET;
    public static final int PREFERRED_HEIGHT = 118;

    private static final int SIDE_MARGIN = 10;
    private static final int OPEN_COLUMN = 26;
    private static final int TOP_MARGIN = 12;
    private static final int NUMBERS_HEIGHT = 15;
    private static final Set<Integer> SINGLE_INLAYS = Set.of(3, 5, 7, 9, 15, 17, 19, 21);
    private static final Set<Integer> DOUBLE_INLAYS = Set.of(12, 24);

    private Tuning tuning = Tuning.standard();
    private Beat beat = Beat.rest(com.gstncaruso.tabpro.core.model.Duration.quarter());

    public FretboardView() {
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(0, PREFERRED_HEIGHT));
    }

    public void show(Tuning tuning, Beat beat) {
        this.tuning = tuning;
        this.beat = beat;
        repaint();
    }

    public int stringCount() {
        return tuning.stringCount();
    }

    public int nutX() {
        return SIDE_MARGIN + OPEN_COLUMN;
    }

    public int fretCenterX(int fret) {
        if (fret == 0) {
            return SIDE_MARGIN + OPEN_COLUMN / 2;
        }
        return (int) Math.round(nutX() + (fret - 0.5) * fretWidth());
    }

    public int stringY(int string) {
        return TOP_MARGIN + (int) Math.round((string - 1) * stringGap());
    }

    /** La nota que se pisa en ese punto del mastil, o nada si el punto cae afuera. */
    public Optional<Note> noteAt(int x, int y) {
        return stringAt(y).flatMap(string -> fretAt(x).map(fret -> new Note(string, fret)));
    }

    private Optional<Integer> stringAt(int y) {
        int string = (int) Math.round((y - TOP_MARGIN) / stringGap()) + 1;
        if (string < 1 || string > stringCount()) {
            return Optional.empty();
        }
        boolean onTheNeck = Math.abs(y - stringY(string)) <= stringGap() / 2;
        return onTheNeck ? Optional.of(string) : Optional.empty();
    }

    private Optional<Integer> fretAt(int x) {
        if (x < SIDE_MARGIN) {
            return Optional.empty();
        }
        if (x < nutX()) {
            return Optional.of(0);
        }
        int fret = (int) Math.floor((x - nutX()) / fretWidth()) + 1;
        return fret > FRETS ? Optional.empty() : Optional.of(fret);
    }

    private double fretWidth() {
        return (double) (getWidth() - SIDE_MARGIN - nutX()) / FRETS;
    }

    private double stringGap() {
        int usable = getHeight() - TOP_MARGIN - NUMBERS_HEIGHT - TOP_MARGIN;
        return stringCount() <= 1 ? 0 : (double) usable / (stringCount() - 1);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        paintNeck(g);
        paintInlays(g);
        paintFretWires(g);
        paintStrings(g);
        paintFretNumbers(g);
        paintPressedNotes(g);
    }

    private void paintNeck(Graphics2D g) {
        int top = stringY(1) - (int) (stringGap() / 2);
        int bottom = stringY(stringCount()) + (int) (stringGap() / 2);
        g.setColor(InstrumentColors.NECK);
        g.fillRect(nutX(), top, getWidth() - SIDE_MARGIN - nutX(), bottom - top);
        g.setColor(InstrumentColors.NECK_EDGE);
        g.drawRect(nutX(), top, getWidth() - SIDE_MARGIN - nutX(), bottom - top);
    }

    private void paintInlays(Graphics2D g) {
        int middle = (stringY(1) + stringY(stringCount())) / 2;
        int radius = 4;
        g.setColor(InstrumentColors.INLAY);
        for (int fret = 1; fret <= FRETS; fret++) {
            int x = fretCenterX(fret);
            if (SINGLE_INLAYS.contains(fret)) {
                g.fillOval(x - radius, middle - radius, radius * 2, radius * 2);
            } else if (DOUBLE_INLAYS.contains(fret)) {
                int offset = (int) stringGap();
                g.fillOval(x - radius, middle - offset - radius, radius * 2, radius * 2);
                g.fillOval(x - radius, middle + offset - radius, radius * 2, radius * 2);
            }
        }
    }

    private void paintFretWires(Graphics2D g) {
        int top = stringY(1) - (int) (stringGap() / 2);
        int bottom = stringY(stringCount()) + (int) (stringGap() / 2);

        g.setColor(InstrumentColors.FRET_WIRE);
        g.setStroke(new BasicStroke(1));
        for (int fret = 1; fret <= FRETS; fret++) {
            int x = (int) Math.round(nutX() + fret * fretWidth());
            g.drawLine(x, top, x, bottom);
        }

        g.setColor(InstrumentColors.NUT);
        g.setStroke(new BasicStroke(3));
        g.drawLine(nutX(), top, nutX(), bottom);
    }

    private void paintStrings(Graphics2D g) {
        g.setColor(InstrumentColors.STRING);
        for (int string = 1; string <= stringCount(); string++) {
            float thickness = 0.8f + (string - 1) * 0.22f;
            g.setStroke(new BasicStroke(thickness));
            int y = stringY(string);
            g.drawLine(nutX(), y, getWidth() - SIDE_MARGIN, y);
        }
    }

    private void paintFretNumbers(Graphics2D g) {
        g.setColor(InstrumentColors.FRET_NUMBER);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        FontMetrics metrics = g.getFontMetrics();
        int y = getHeight() - 4;
        for (int fret : List.of(3, 5, 7, 9, 12, 15, 17, 19, 21, 24)) {
            String text = String.valueOf(fret);
            g.drawString(text, fretCenterX(fret) - metrics.stringWidth(text) / 2, y);
        }
    }

    private void paintPressedNotes(Graphics2D g) {
        int radius = Math.max(7, (int) (stringGap() * 0.44));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        FontMetrics metrics = g.getFontMetrics();

        for (Note note : beat.notes()) {
            if (note.string() > stringCount()) {
                continue;
            }
            int x = fretCenterX(note.fret());
            int y = stringY(note.string());
            g.setColor(InstrumentColors.PRESSED);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            String name = PitchName.of(tuning.pitchOf(note)).text();
            g.setColor(InstrumentColors.PRESSED_INK);
            g.drawString(
                    name,
                    x - metrics.stringWidth(name) / 2,
                    y + (metrics.getAscent() - metrics.getDescent()) / 2);
        }
    }
}

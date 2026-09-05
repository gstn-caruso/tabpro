package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import javax.swing.JComponent;

/** El teclado, con las teclas del beat en el que estas parado hundidas. */
public final class KeyboardView extends JComponent {

    /** Do1: mas grave que la cuerda mas grave de un bajo de cinco cuerdas. */
    public static final int LOWEST = 24;
    /** Do7: mas agudo que el traste 24 de la primera cuerda de una guitarra. */
    public static final int HIGHEST = 96;
    public static final int PREFERRED_HEIGHT = 92;

    private static final int SIDE_MARGIN = 10;
    private static final int TOP_MARGIN = 8;
    private static final int BOTTOM_MARGIN = 8;
    private static final double BLACK_KEY_WIDTH = 0.62;
    private static final double BLACK_KEY_HEIGHT = 0.62;
    private static final Set<Integer> WHITE_PITCH_CLASSES = Set.of(0, 2, 4, 5, 7, 9, 11);

    private Tuning tuning = Tuning.standard();
    private Beat beat = Beat.rest(Duration.quarter());

    public KeyboardView() {
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

    public static boolean isWhite(int midiNumber) {
        return WHITE_PITCH_CLASSES.contains(Math.floorMod(midiNumber, 12));
    }

    public Optional<Rectangle> keyBounds(int midiNumber) {
        if (midiNumber < LOWEST || midiNumber > HIGHEST) {
            return Optional.empty();
        }
        return Optional.of(isWhite(midiNumber) ? whiteKeyBounds(midiNumber) : blackKeyBounds(midiNumber));
    }

    /** La tecla que hay en ese punto: las negras primero, que estan encima de las blancas. */
    public OptionalInt keyAt(int x, int y) {
        OptionalInt black = keyAt(x, y, false);
        return black.isPresent() ? black : keyAt(x, y, true);
    }

    private OptionalInt keyAt(int x, int y, boolean white) {
        for (int key : keysInRange(white)) {
            if (keyBounds(key).orElseThrow().contains(x, y)) {
                return OptionalInt.of(key);
            }
        }
        return OptionalInt.empty();
    }

    private Rectangle whiteKeyBounds(int midiNumber) {
        double width = whiteKeyWidth();
        int x = (int) Math.round(SIDE_MARGIN + whiteKeysBefore(midiNumber) * width);
        int right = (int) Math.round(SIDE_MARGIN + (whiteKeysBefore(midiNumber) + 1) * width);
        return new Rectangle(x, TOP_MARGIN, right - x, keyboardHeight());
    }

    private Rectangle blackKeyBounds(int midiNumber) {
        Rectangle before = whiteKeyBounds(midiNumber - 1);
        int width = (int) Math.round(before.width * BLACK_KEY_WIDTH);
        return new Rectangle(
                before.x + before.width - width / 2,
                TOP_MARGIN,
                width,
                (int) Math.round(keyboardHeight() * BLACK_KEY_HEIGHT));
    }

    private int whiteKeysBefore(int midiNumber) {
        int count = 0;
        for (int key = LOWEST; key < midiNumber; key++) {
            if (isWhite(key)) {
                count++;
            }
        }
        return count;
    }

    private int whiteKeyCount() {
        return whiteKeysBefore(HIGHEST + 1);
    }

    private double whiteKeyWidth() {
        return (double) (getWidth() - 2 * SIDE_MARGIN) / whiteKeyCount();
    }

    private int keyboardHeight() {
        return getHeight() - TOP_MARGIN - BOTTOM_MARGIN;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        Set<Integer> pressed = pressedKeys();
        paintKeys(g, pressed, true);
        paintKeys(g, pressed, false);
    }

    private void paintKeys(Graphics2D g, Set<Integer> pressed, boolean white) {
        for (int key : keysInRange(white)) {
            Rectangle bounds = keyBounds(key).orElseThrow();
            g.setColor(colorOf(key, pressed, white));
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setColor(InstrumentColors.KEY_EDGE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private java.awt.Color colorOf(int key, Set<Integer> pressed, boolean white) {
        if (pressed.contains(key)) {
            return InstrumentColors.PRESSED;
        }
        return white ? InstrumentColors.WHITE_KEY : InstrumentColors.BLACK_KEY;
    }

    private List<Integer> keysInRange(boolean white) {
        List<Integer> keys = new ArrayList<>();
        for (int key = LOWEST; key <= HIGHEST; key++) {
            if (isWhite(key) == white) {
                keys.add(key);
            }
        }
        return keys;
    }

    private Set<Integer> pressedKeys() {
        Set<Integer> pressed = new HashSet<>();
        for (Note note : beat.notes()) {
            if (note.string() <= tuning.stringCount()) {
                pressed.add(tuning.pitchOf(note).midiNumber());
            }
        }
        return pressed;
    }
}

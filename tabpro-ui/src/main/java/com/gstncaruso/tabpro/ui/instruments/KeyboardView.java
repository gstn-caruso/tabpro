package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

public final class KeyboardView extends JComponent implements AccessibleControl {

    public static final int LOWEST = 21;
    public static final int HIGHEST = 108;
    public static final int PREFERRED_HEIGHT = 92;

    private static final int SIDE_MARGIN = 10;
    private static final int TOP_MARGIN = 8;
    private static final int BOTTOM_MARGIN = 8;
    private static final double BLACK_KEY_WIDTH = 0.62;
    private static final double BLACK_KEY_HEIGHT = 0.62;
    private static final Set<Integer> WHITE_PITCH_CLASSES = Set.of(0, 2, 4, 5, 7, 9, 11);
    private static final double MARK_RADIUS_RATIO = 0.32;
    private static final int MARK_RADIUS_MIN = 2;
    private static final double MARK_MARGIN_RATIO = 0.04;
    private static final int MARK_MARGIN_MIN = 1;
    private static final int BEVEL_THICKNESS = 2;

    private BeatLocation location = defaultLocation();
    private KeyboardDisplayMode displayMode = KeyboardDisplayMode.ONLY_BEAT;
    private Optional<Scale> scale = Optional.empty();
    private OptionalInt hovered = OptionalInt.empty();
    private int caretKey = LOWEST;
    private IntConsumer onCaretActivated = key -> {
    };
    private boolean showsFocusRing;

    public KeyboardView() {
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, PREFERRED_HEIGHT));
        setMinimumSize(new Dimension(0, PREFERRED_HEIGHT));
        setToolTipText(Texts.get("views.KeyboardView.name"));
        getAccessibleContext().setAccessibleName(Texts.get("views.KeyboardView.name"));
        trackTheMouse();
        installKeyboardShortcuts();
        installFocusRing();
        updateCaretAccessibleDescription();
    }

    private void updateCaretAccessibleDescription() {
        getAccessibleContext().setAccessibleDescription(PitchName.of(new Pitch(caretKey)).textWithOctave());
    }

    private void installFocusRing() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showsFocusRing = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                showsFocusRing = false;
                repaint();
            }
        });
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        bindCaretMove(inputMap, actionMap, "RIGHT", 1);
        bindCaretMove(inputMap, actionMap, "LEFT", -1);
        bindCaretActivation(inputMap, actionMap, "ENTER");
        bindCaretActivation(inputMap, actionMap, "SPACE");
    }

    private void bindCaretActivation(InputMap inputMap, ActionMap actionMap, String keyStroke) {
        String name = "keyboard.activate." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCaretActivated.accept(caretKey);
            }
        });
    }

    public void onCaretActivated(IntConsumer listener) {
        this.onCaretActivated = listener;
    }

    private void bindCaretMove(InputMap inputMap, ActionMap actionMap, String keyStroke, int delta) {
        String name = "keyboard.caret." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                caretKey = Math.max(LOWEST, Math.min(HIGHEST, caretKey + delta));
                updateCaretAccessibleDescription();
                repaint();
            }
        });
    }

    public OptionalInt caretKey() {
        return OptionalInt.of(caretKey);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }

    private static BeatLocation defaultLocation() {
        return new BeatLocation(Track.standardGuitar(Texts.get("defaults.guitarTrack")), 0, VoicePart.LEAD, 0);
    }

    public void show(BeatLocation location) {
        this.location = location;
        repaint();
    }

    public void setDisplayMode(KeyboardDisplayMode displayMode) {
        this.displayMode = displayMode;
        repaint();
    }

    public KeyboardDisplayMode displayMode() {
        return displayMode;
    }

    public void setScale(Scale scale) {
        this.scale = Optional.ofNullable(scale);
        repaint();
    }

    public OptionalInt hoveredKey() {
        return hovered;
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

    private void trackTheMouse() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hovered = keyAt(e.getX(), e.getY());
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = OptionalInt.empty();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        KeyMarks marks = currentMarks();
        paintKeys(g, marks, true);
        paintKeys(g, marks, false);
        paintHover(g);
        if (showsFocusRing) {
            paintCaret(g);
        }
    }

    private void paintCaret(Graphics2D g) {
        keyBounds(caretKey).ifPresent(bounds -> {
            g.setColor(focusRingColor());
            g.setStroke(new BasicStroke(2f));
            g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
        });
    }

    private Color focusRingColor() {
        Color fromLookAndFeel = UIManager.getColor("Component.focusColor");
        return fromLookAndFeel != null ? fromLookAndFeel : InstrumentColors.HOVER;
    }

    private void paintKeys(Graphics2D g, KeyMarks marks, boolean white) {
        Color base = white ? InstrumentColors.WHITE_KEY : InstrumentColors.BLACK_KEY;
        for (int key : keysInRange(white)) {
            Rectangle bounds = keyBounds(key).orElseThrow();
            g.setColor(base);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setColor(InstrumentColors.KEY_EDGE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            paintKeyBevel(g, bounds, base);
            marks.kindOf(key).ifPresent(kind -> paintMarkDot(g, bounds, kind));
        }
    }

    private void paintKeyBevel(Graphics2D g, Rectangle bounds, Color base) {
        g.setColor(base.brighter());
        g.fillRect(bounds.x, bounds.y, bounds.width, BEVEL_THICKNESS);
        g.setColor(base.darker());
        g.fillRect(bounds.x, bounds.y + bounds.height - BEVEL_THICKNESS, bounds.width, BEVEL_THICKNESS);
    }

    private void paintMarkDot(Graphics2D g, Rectangle bounds, MarkKind kind) {
        int radius = Math.max(MARK_RADIUS_MIN, (int) Math.round(bounds.width * MARK_RADIUS_RATIO));
        int marginBottom = Math.max(MARK_MARGIN_MIN, (int) Math.round(bounds.height * MARK_MARGIN_RATIO));
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height - marginBottom - radius;
        g.setColor(kind == MarkKind.PRIMARY ? InstrumentColors.PRESSED : InstrumentColors.CONTEXT);
        g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private void paintHover(Graphics2D g) {
        hovered.ifPresent(key -> keyBounds(key).ifPresent(bounds -> {
            g.setColor(InstrumentColors.HOVER);
            g.setStroke(new BasicStroke(1.6f));
            g.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
        }));
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

    private KeyMarks currentMarks() {
        return displayMode.marks(location, scale);
    }
}

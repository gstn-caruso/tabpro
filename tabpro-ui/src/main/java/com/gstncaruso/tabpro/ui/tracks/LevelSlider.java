package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * Un parametro de sonido dibujado como deslizador horizontal, tal como el volumen y el paneo en
 * la mesa de mezcla de Guitar Pro: una barra que se rellena segun el nivel y una caja numerica
 * que viaja sobre el cursor. Cuando el color de relleno y el de la parte vacia coinciden, la
 * barra se ve pintada de punta a punta y solo se mueve la caja -asi se lee el paneo, centrado en
 * el medio del rango.
 */
public final class LevelSlider extends JComponent implements AccessibleControl {

    private static final Color BADGE_BACKGROUND = ScoreColors.PAGE_PAPER;
    private static final Color BADGE_TEXT = ScoreColors.PAGE_INK;

    private final int min;
    private final int max;
    private final Color fillColor;
    private final Color trackColor;
    private int value;
    private Runnable onUserChange = () -> {
    };
    private boolean showsFocusRing;

    public LevelSlider(int min, int max, int value, Color fillColor, Color trackColor) {
        this.min = min;
        this.max = max;
        this.value = clamp(value);
        this.fillColor = fillColor;
        this.trackColor = trackColor;
        setToolTipText(String.valueOf(this.value));
        installKeyboardShortcuts();
        installFocusRing();
        addMouseListener(jumpToClick());
        addMouseMotionListener(followDrag());
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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(96, 20);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int trackHeight = Math.max(2, height / 3);
        int trackY = (height - trackHeight) / 2;
        int fillWidth = (int) Math.round((width - 1) * fraction());

        g.setColor(trackColor);
        g.fillRect(0, trackY, width, trackHeight);
        g.setColor(fillColor);
        g.fillRect(0, trackY, fillWidth, trackHeight);

        paintBadge(g, width, height);

        if (showsFocusRing) {
            g.setColor(focusRingColor());
            g.drawRect(0, 0, width - 1, height - 1);
        }
    }

    private void paintBadge(Graphics2D g, int width, int height) {
        String text = String.valueOf(value);
        FontMetrics metrics = g.getFontMetrics();
        int badgeWidth = metrics.stringWidth(text) + 10;
        int badgeHeight = height - 2;
        int centerX = (int) Math.round((width - 1) * fraction());
        int badgeX = Math.max(0, Math.min(width - badgeWidth, centerX - badgeWidth / 2));
        int badgeY = (height - badgeHeight) / 2;

        g.setColor(BADGE_BACKGROUND);
        g.fillRect(badgeX, badgeY, badgeWidth, badgeHeight);
        g.setColor(BADGE_TEXT);
        g.drawRect(badgeX, badgeY, badgeWidth - 1, badgeHeight - 1);
        int textX = badgeX + (badgeWidth - metrics.stringWidth(text)) / 2;
        int textY = badgeY + (badgeHeight + metrics.getAscent() - metrics.getDescent()) / 2;
        g.drawString(text, textX, textY);
    }

    private double fraction() {
        return (value - min) / (double) (max - min);
    }

    private Color focusRingColor() {
        Color fromLookAndFeel = UIManager.getColor("Component.focusColor");
        return fromLookAndFeel != null ? fromLookAndFeel : fillColor;
    }

    private MouseAdapter jumpToClick() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setValue(valueAt(e.getX()));
                onUserChange.run();
            }
        };
    }

    private MouseMotionAdapter followDrag() {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setValue(valueAt(e.getX()));
                onUserChange.run();
            }
        };
    }

    private int valueAt(int x) {
        double fraction = x / (double) (getWidth() - 1);
        return min + (int) Math.round(fraction * (max - min));
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        bindStep(inputMap, actionMap, "RIGHT", 1);
        bindStep(inputMap, actionMap, "UP", 1);
        bindStep(inputMap, actionMap, "LEFT", -1);
        bindStep(inputMap, actionMap, "DOWN", -1);
        bindStep(inputMap, actionMap, "PAGE_UP", 10);
        bindStep(inputMap, actionMap, "PAGE_DOWN", -10);
        bindTo(inputMap, actionMap, "HOME", () -> min);
        bindTo(inputMap, actionMap, "END", () -> max);
    }

    private void bindStep(InputMap inputMap, ActionMap actionMap, String keyStroke, int step) {
        bindTo(inputMap, actionMap, keyStroke, () -> value + step);
    }

    private void bindTo(InputMap inputMap, ActionMap actionMap, String keyStroke, java.util.function.IntSupplier target) {
        String name = "levelSlider.goto." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setValue(target.getAsInt());
                onUserChange.run();
            }
        });
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int clamped = clamp(value);
        if (clamped != this.value) {
            this.value = clamped;
            setToolTipText(String.valueOf(this.value));
            repaint();
        }
    }

    public void onUserChange(Runnable listener) {
        this.onUserChange = listener;
    }

    private int clamp(int candidate) {
        return Math.max(min, Math.min(max, candidate));
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleLevelSlider();
        }
        return accessibleContext;
    }

    private final class AccessibleLevelSlider extends AccessibleJComponent implements AccessibleValue {
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SLIDER;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return value;
        }

        @Override
        public boolean setCurrentAccessibleValue(Number number) {
            setValue(number.intValue());
            onUserChange.run();
            return true;
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return min;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return max;
        }
    }
}

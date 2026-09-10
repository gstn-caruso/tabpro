package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class BendGridPanel extends JComponent implements AccessibleControl {

    private static final int MIN_QUARTER_TONES = -BendPoint.MAX_QUARTER_TONES;
    private static final int MAX_QUARTER_TONES = BendPoint.MAX_QUARTER_TONES;
    private static final int ROWS = MAX_QUARTER_TONES - MIN_QUARTER_TONES;

    private final BendCurveEditor editor;
    private int caretPosition;
    private int caretQuarterTones;
    private boolean showsFocusRing;

    public BendGridPanel(BendCurveEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(360, 180));
        setToolTipText("Grilla del bend");
        getAccessibleContext().setAccessibleName("Grilla del bend");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int position = positionOf(event.getX());
                int quarterTones = quarterTonesOf(event.getY());
                if (SwingUtilities.isRightMouseButton(event)) {
                    editor.addVibratoAt(position);
                } else {
                    editor.clickAt(position, quarterTones);
                }
                repaint();
            }
        });
        installKeyboardShortcuts();
        installFocusRing();
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

    public int caretPosition() {
        return caretPosition;
    }

    public int caretQuarterTones() {
        return caretQuarterTones;
    }

    private void installKeyboardShortcuts() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        bindCaretMove(inputMap, actionMap, "RIGHT", 1, 0);
        bindCaretMove(inputMap, actionMap, "LEFT", -1, 0);
        bindCaretMove(inputMap, actionMap, "UP", 0, 1);
        bindCaretMove(inputMap, actionMap, "DOWN", 0, -1);
        bindCaretActivation(inputMap, actionMap, "ENTER", () -> editor.clickAt(caretPosition, caretQuarterTones));
        bindCaretActivation(inputMap, actionMap, "SPACE", () -> editor.addVibratoAt(caretPosition));
    }

    private void bindCaretActivation(InputMap inputMap, ActionMap actionMap, String keyStroke, Runnable action) {
        String name = "bendgrid.activate." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
                repaint();
            }
        });
    }

    private void bindCaretMove(InputMap inputMap, ActionMap actionMap, String keyStroke, int positionDelta, int quarterTonesDelta) {
        String name = "bendgrid.caret." + keyStroke;
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                caretPosition = Math.max(0, Math.min(BendPoint.LAST_POSITION, caretPosition + positionDelta));
                caretQuarterTones = Math.max(MIN_QUARTER_TONES, Math.min(MAX_QUARTER_TONES, caretQuarterTones + quarterTonesDelta));
                repaint();
            }
        });
    }

    private int positionOf(int x) {
        double fraction = clamp01(x / (double) getWidth());
        return (int) Math.round(fraction * BendPoint.LAST_POSITION);
    }

    private int quarterTonesOf(int y) {
        double fraction = clamp01(y / (double) getHeight());
        return (int) Math.round(MAX_QUARTER_TONES - fraction * ROWS);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UIManager.getColor("TextField.background"));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(gridLineColor());
        int zeroY = yOf(0);
        g.drawLine(0, zeroY, getWidth(), zeroY);

        g.setColor(curveColor());
        var points = editor.points();
        for (int index = 0; index < points.size() - 1; index++) {
            var from = points.get(index);
            var to = points.get(index + 1);
            g.drawLine(xOf(from.position()), yOf(from.quarterTones()), xOf(to.position()), yOf(to.quarterTones()));
        }
        for (var point : points) {
            int x = xOf(point.position());
            int y = yOf(point.quarterTones());
            g.fillOval(x - 3, y - 3, 6, 6);
            if (point.vibrato() > 0) {
                g.drawOval(x - 5 - point.vibrato(), y - 5 - point.vibrato(), (5 + point.vibrato()) * 2, (5 + point.vibrato()) * 2);
            }
        }
        if (showsFocusRing) {
            paintCaretRing(g);
        }
    }

    private void paintCaretRing(Graphics2D g) {
        int x = xOf(caretPosition);
        int y = yOf(caretQuarterTones);
        g.setColor(focusRingColor());
        g.drawOval(x - 6, y - 6, 12, 12);
    }

    private Color focusRingColor() {
        Color base = UIManager.getColor("Component.focusColor");
        return base != null ? base : curveColor();
    }

    private int xOf(int position) {
        return (int) Math.round(position / (double) BendPoint.LAST_POSITION * getWidth());
    }

    private int yOf(int quarterTones) {
        double fraction = (MAX_QUARTER_TONES - quarterTones) / (double) ROWS;
        return (int) Math.round(fraction * getHeight());
    }

    private Color gridLineColor() {
        Color base = UIManager.getColor("Separator.foreground");
        return base != null ? base : Color.GRAY;
    }

    private Color curveColor() {
        Color base = UIManager.getColor("Component.focusColor");
        return base != null ? base : Color.ORANGE;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.CANVAS;
                }
            };
        }
        return accessibleContext;
    }
}

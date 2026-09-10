package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import java.awt.Color;
import java.awt.event.ActionEvent;
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

/**
 * Un parametro de sonido dibujado como deslizador horizontal, tal como el volumen y el paneo en
 * la mesa de mezcla de Guitar Pro: una barra que se rellena segun el nivel y una caja numerica
 * que viaja sobre el cursor. Cuando el color de relleno y el de la parte vacia coinciden, la
 * barra se ve pintada de punta a punta y solo se mueve la caja -asi se lee el paneo, centrado en
 * el medio del rango.
 */
public final class LevelSlider extends JComponent implements AccessibleControl {

    private final int min;
    private final int max;
    private final Color fillColor;
    private final Color trackColor;
    private int value;
    private Runnable onUserChange = () -> {
    };

    public LevelSlider(int min, int max, int value, Color fillColor, Color trackColor) {
        this.min = min;
        this.max = max;
        this.value = clamp(value);
        this.fillColor = fillColor;
        this.trackColor = trackColor;
        installKeyboardShortcuts();
        addMouseListener(jumpToClick());
        addMouseMotionListener(followDrag());
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

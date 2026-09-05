package com.gstncaruso.tabpro.ui.tab;

import com.gstncaruso.tabpro.core.editing.Editor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public final class KeyboardEditing {

    private final Editor editor;
    private final FretDigits digits;

    public KeyboardEditing(Editor editor, FretDigits digits) {
        this.editor = editor;
        this.digits = digits;
    }

    public Map<KeyStroke, Runnable> bindings() {
        Map<KeyStroke, Runnable> bindings = new LinkedHashMap<>();
        bindings.put(KeyStroke.getKeyStroke("RIGHT"), editor::moveRight);
        bindings.put(KeyStroke.getKeyStroke("LEFT"), editor::moveLeft);
        bindings.put(KeyStroke.getKeyStroke("UP"), editor::moveUp);
        bindings.put(KeyStroke.getKeyStroke("DOWN"), editor::moveDown);
        bindings.put(KeyStroke.getKeyStroke("HOME"), editor::moveToMeasureStart);
        bindings.put(KeyStroke.getKeyStroke("END"), editor::moveToMeasureEnd);
        bindings.put(KeyStroke.getKeyStroke("INSERT"), editor::insertBeat);
        bindings.put(KeyStroke.getKeyStroke("DELETE"), editor::deleteBeat);
        bindings.put(KeyStroke.getKeyStroke("ctrl INSERT"), editor::insertMeasure);
        bindings.put(KeyStroke.getKeyStroke("ctrl DELETE"), editor::deleteMeasure);
        bindings.put(KeyStroke.getKeyStroke("BACK_SPACE"), editor::clearNote);
        bindings.put(KeyStroke.getKeyStroke("ctrl Z"), editor::undo);
        bindings.put(KeyStroke.getKeyStroke("ctrl Y"), editor::redo);
        return bindings;
    }

    public void keyTyped(char c) {
        if (Character.isDigit(c)) {
            editor.setFret(digits.fretFor(c));
            return;
        }
        digits.reset();
        switch (c) {
            case '+' -> editor.lengthenDuration();
            case '-' -> editor.shortenDuration();
            case '.' -> editor.toggleDot();
            case 'r', 'R' -> editor.clearBeat();
            default -> {
            }
        }
    }

    public void install(JComponent component) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = component.getActionMap();
        for (Map.Entry<KeyStroke, Runnable> entry : bindings().entrySet()) {
            KeyStroke keyStroke = entry.getKey();
            Runnable action = entry.getValue();
            String name = keyStroke.toString();
            inputMap.put(keyStroke, name);
            actionMap.put(name, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            });
        }
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                KeyboardEditing.this.keyTyped(e.getKeyChar());
            }
        });
    }
}

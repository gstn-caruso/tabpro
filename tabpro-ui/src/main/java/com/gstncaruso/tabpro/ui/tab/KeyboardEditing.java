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

/**
 * Lo que se escribe con el teclado sobre la partitura y no pasa por los menus:
 * los digitos de los trastes y el movimiento del cursor. Todo lo que tiene
 * atajo en un menu vive en el catalogo de comandos, para no tenerlo dos veces.
 */
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
        bindings.put(KeyStroke.getKeyStroke("BACK_SPACE"), editor::clearNote);
        bindings.put(KeyStroke.getKeyStroke("TAB"), editor::toggleNotation);
        bindings.replaceAll((keyStroke, action) -> resettingDigitsBefore(action));
        return bindings;
    }

    private Runnable resettingDigitsBefore(Runnable action) {
        return () -> {
            digits.reset();
            action.run();
        };
    }

    /**
     * Los digitos escriben el numero de la pista: el traste en una pista de cuerdas, el sonido
     * MIDI en una de percusion. Dos seguidos forman el numero de dos cifras que la pista acepte.
     */
    public void keyTyped(char c) {
        if (Character.isDigit(c)) {
            editor.setFret(digits.fretFor(c, editor.currentTrack()::acceptsTypedNumber));
            return;
        }
        // El manual lista el puntillo como "* or .": el "." ya es el acelerador de note.dot,
        // asi que el "*" se resuelve aca para no repetir un atajo entre dos comandos.
        if (c == '*') {
            editor.toggleDot();
        }
        digits.reset();
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

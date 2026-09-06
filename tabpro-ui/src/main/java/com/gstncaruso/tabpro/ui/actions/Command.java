package com.gstncaruso.tabpro.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/** Una accion de la aplicacion, con su nombre, su atajo y su icono. */
public final class Command extends AbstractAction {

    private final Runnable body;

    private Command(String label, Runnable body) {
        super(label);
        this.body = body;
        putValue(SHORT_DESCRIPTION, label);
    }

    public static Command named(String label, Runnable body) {
        return new Command(label, body);
    }

    public Command withAccelerator(String accelerator) {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
        return this;
    }

    public Command withIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
        return this;
    }

    public Command describedAs(String description) {
        putValue(SHORT_DESCRIPTION, description);
        return this;
    }

    /**
     * Cambia el nombre de un comando ya armado, como el paso a paso del manual que dice "Nota
     * siguiente" parado y "Compás siguiente" durante la reproducción. Al extender
     * AbstractAction, esto dispara el PropertyChangeEvent que ya actualiza solo cualquier
     * JMenuItem o JButton armado con este comando.
     */
    public Command renameTo(String label) {
        putValue(NAME, label);
        return this;
    }

    /**
     * Para las acciones que se muestran con un casillero (JCheckBoxMenuItem): arranca tildado, y
     * Swing se encarga de mantener el casillero y esta bandera sincronizados en los dos sentidos.
     */
    public Command checkedByDefault() {
        putValue(SELECTED_KEY, Boolean.TRUE);
        return this;
    }

    public String label() {
        return (String) getValue(NAME);
    }

    public Icon icon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public String description() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public KeyStroke accelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }

    /** El texto del atajo tal como se lee en el manual, para mostrarlo en una ayuda. */
    public String acceleratorText() {
        KeyStroke stroke = accelerator();
        if (stroke == null) {
            return "";
        }
        String modifiers = java.awt.event.InputEvent.getModifiersExText(stroke.getModifiers());
        String key = java.awt.event.KeyEvent.getKeyText(stroke.getKeyCode());
        return modifiers.isEmpty() ? key : modifiers + "+" + key;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        body.run();
    }
}

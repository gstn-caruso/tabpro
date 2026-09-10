package com.gstncaruso.tabpro.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

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
     * Extending AbstractAction, this fires the PropertyChangeEvent that already updates any
     * JMenuItem or JButton wired to this command on its own.
     */
    public Command renameTo(String label) {
        putValue(NAME, label);
        return this;
    }

    /**
     * For actions shown with a checkbox (JCheckBoxMenuItem): starts checked, and Swing keeps the
     * checkbox and this flag synchronized both ways.
     */
    public Command checkedByDefault() {
        putValue(SELECTED_KEY, Boolean.TRUE);
        return this;
    }

    /**
     * For when the checkbox has to follow a change that did not come from toggling the control
     * itself but shares this same command: Swing already keeps any JToggleButton or
     * JCheckBoxMenuItem wired to it synchronized both ways.
     */
    public void setChecked(boolean checked) {
        putValue(SELECTED_KEY, checked);
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

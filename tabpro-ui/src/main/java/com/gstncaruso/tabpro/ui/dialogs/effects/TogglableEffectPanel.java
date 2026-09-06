package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Un efecto que puede estar o no estar: la casilla de arriba dice si se aplica,
 * y el contenido de abajo es el panel propio de ese efecto.
 */
public final class TogglableEffectPanel<T extends JComponent> extends JPanel {

    private final JCheckBox active = new JCheckBox("Activo");
    private final T content;

    public TogglableEffectPanel(boolean initiallyActive, T content) {
        super(new BorderLayout(0, DialogStyle.GAP_S));
        this.content = content;
        active.setSelected(initiallyActive);
        add(active, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public boolean isActive() {
        return active.isSelected();
    }

    public T content() {
        return content;
    }
}

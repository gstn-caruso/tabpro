package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/** El rasgueo: hacia donde barre, que tan rapido y si es un rasgueado de guitarra. */
public final class StrokePanel extends FormPanel {

    private final JComboBox<StrokeDirection> direction = new JComboBox<>(StrokeDirection.values());
    private final JComboBox<NoteValue> speed = new JComboBox<>(NoteValue.values());
    private final JCheckBox rasgueado = new JCheckBox("Rasgueado");

    public StrokePanel(Stroke initial) {
        direction.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.label()));

        addRow("Direccion", direction);
        addRow("Velocidad", speed);
        addFullWidthRow(rasgueado);

        apply(initial);
    }

    public void apply(Stroke stroke) {
        direction.setSelectedItem(stroke.direction());
        speed.setSelectedItem(stroke.speed());
        rasgueado.setSelected(stroke.rasgueado());
    }

    public Stroke toStroke() {
        return new Stroke((StrokeDirection) direction.getSelectedItem(), (NoteValue) speed.getSelectedItem(), rasgueado.isSelected());
    }
}

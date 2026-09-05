package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.dialogs.style.ColorSwatchButton;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JTextField;

/** El nombre y el color de un marcador. */
public final class MarkerPanel extends FormPanel {

    private final JTextField name = new JTextField();
    private final ColorSwatchButton color;

    public MarkerPanel(Marker initial) {
        color = new ColorSwatchButton(initial.color());
        addRow("Nombre", name);
        addRow("Color", color);
        apply(initial);
    }

    public void apply(Marker marker) {
        name.setText(marker.name());
        color.apply(marker.color());
    }

    public Marker toMarker() {
        return new Marker(name.getText(), color.toScoreColor());
    }
}

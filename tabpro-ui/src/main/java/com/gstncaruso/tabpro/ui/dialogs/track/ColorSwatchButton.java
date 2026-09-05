package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/** Un boton que muestra el color de la pista y lo deja cambiar con el selector del sistema. */
final class ColorSwatchButton extends JButton {

    private ScoreColor color;

    ColorSwatchButton(ScoreColor initial) {
        setPreferredSize(new Dimension(48, 22));
        apply(initial);
        addActionListener(event -> pickColor());
    }

    private void pickColor() {
        Color chosen = JColorChooser.showDialog(this, "Color de la pista", toAwtColor());
        if (chosen != null) {
            apply(new ScoreColor(chosen.getRed(), chosen.getGreen(), chosen.getBlue()));
        }
    }

    void apply(ScoreColor color) {
        this.color = color;
        setBackground(toAwtColor());
        setOpaque(true);
    }

    ScoreColor toScoreColor() {
        return color;
    }

    private Color toAwtColor() {
        return new Color(color.red(), color.green(), color.blue());
    }
}

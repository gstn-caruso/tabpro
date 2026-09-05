package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/** Un boton que muestra un color y lo deja cambiar con el selector del sistema. */
public final class ColorSwatchButton extends JButton {

    private ScoreColor color;

    public ColorSwatchButton(ScoreColor initial) {
        setPreferredSize(new Dimension(48, 22));
        apply(initial);
        addActionListener(event -> pickColor());
    }

    private void pickColor() {
        Color chosen = JColorChooser.showDialog(this, "Elegir color", toAwtColor());
        if (chosen != null) {
            apply(new ScoreColor(chosen.getRed(), chosen.getGreen(), chosen.getBlue()));
        }
    }

    public void apply(ScoreColor color) {
        this.color = color;
        setBackground(toAwtColor());
        setOpaque(true);
    }

    public ScoreColor toScoreColor() {
        return color;
    }

    private Color toAwtColor() {
        return new Color(color.red(), color.green(), color.blue());
    }
}

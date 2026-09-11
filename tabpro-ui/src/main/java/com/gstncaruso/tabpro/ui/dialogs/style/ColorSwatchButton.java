package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JColorChooser;

public final class ColorSwatchButton extends JButton {

    private ScoreColor color;

    public ColorSwatchButton(ScoreColor initial) {
        setPreferredSize(new Dimension(48, 22));
        getAccessibleContext().setAccessibleName(Texts.get("edit_dialogs.shared.color"));
        setToolTipText(Texts.get("edit_dialogs.shared.color"));
        apply(initial);
        addActionListener(event -> pickColor());
    }

    private void pickColor() {
        Color chosen = JColorChooser.showDialog(this, Texts.get("edit_dialogs.ColorSwatchButton.pick"), toAwtColor());
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

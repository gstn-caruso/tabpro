package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public final class ButtonBar extends JPanel {

    private final JButton acceptButton;
    private final JButton cancelButton;

    private ButtonBar(String acceptLabel) {
        super(new FlowLayout(FlowLayout.RIGHT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        acceptButton = DialogStyle.flatButton(acceptLabel);
        cancelButton = DialogStyle.flatButton(Texts.get("common.cancel"));
        add(cancelButton);
        add(acceptButton);
    }

    public static ButtonBar acceptCancel() {
        return new ButtonBar(Texts.get("common.accept"));
    }

    public static ButtonBar acceptCancel(String acceptLabel) {
        return new ButtonBar(acceptLabel);
    }

    public JButton acceptButton() {
        return acceptButton;
    }

    public JButton cancelButton() {
        return cancelButton;
    }
}

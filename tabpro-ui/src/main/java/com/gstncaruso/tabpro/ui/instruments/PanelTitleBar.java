package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.ui.icons.Icons;
import com.gstncaruso.tabpro.ui.theme.Palette;
import com.gstncaruso.tabpro.ui.theme.UiSizing;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class PanelTitleBar extends JPanel {

    private final JLabel label;
    private final JButton close;

    PanelTitleBar(String title, JComponent controls, String closeAccessibleName) {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(Palette.raisedPanel());

        label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, (float) UiSizing.scaled(10)));
        label.setForeground(Palette.text());
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 2, 0));

        close = new JButton(Icons.closePanel());
        close.getAccessibleContext().setAccessibleName(closeAccessibleName);
        close.setToolTipText(closeAccessibleName);
        close.setForeground(Palette.text());

        add(label, BorderLayout.WEST);
        add(controls, BorderLayout.CENTER);
        add(close, BorderLayout.EAST);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setBackground(Palette.raisedPanel());
        if (label != null) {
            label.setForeground(Palette.text());
        }
        if (close != null) {
            close.setForeground(Palette.text());
        }
    }

    void onClose(Runnable action) {
        close.addActionListener(e -> action.run());
    }
}

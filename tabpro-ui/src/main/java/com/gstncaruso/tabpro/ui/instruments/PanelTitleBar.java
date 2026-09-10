package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.ui.icons.Icons;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class PanelTitleBar extends JPanel {

    private final JButton close;

    PanelTitleBar(String title, JComponent controls, String closeAccessibleName) {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(ScoreColors.TITLE_BAR);

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setForeground(ScoreColors.TITLE_BAR_INK);
        label.setBorder(BorderFactory.createEmptyBorder(3, 12, 2, 0));

        close = new JButton(Icons.closePanel());
        close.getAccessibleContext().setAccessibleName(closeAccessibleName);
        close.setToolTipText(closeAccessibleName);
        close.setForeground(ScoreColors.TITLE_BAR_INK);

        add(label, BorderLayout.WEST);
        add(controls, BorderLayout.CENTER);
        add(close, BorderLayout.EAST);
    }

    void onClose(Runnable action) {
        close.addActionListener(e -> action.run());
    }
}

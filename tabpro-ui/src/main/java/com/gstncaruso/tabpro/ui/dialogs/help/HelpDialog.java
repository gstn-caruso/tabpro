package com.gstncaruso.tabpro.ui.dialogs.help;

import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** La ayuda que el manual pone en F1: la lista de atajos, agrupada como su capitulo Reference. */
public final class HelpDialog {

    private static final int VISIBLE_HEIGHT = 460;

    private HelpDialog() {
    }

    public static void show(Component parent, Commands commands) {
        JScrollPane scrolling = new JScrollPane(shortcutsOf(commands));
        scrolling.setBorder(BorderFactory.createEmptyBorder());
        scrolling.getVerticalScrollBar().setUnitIncrement(16);
        scrolling.setPreferredSize(new Dimension(420, VISIBLE_HEIGHT));
        DialogShell.show(parent, "Ayuda de tabpro", scrolling);
    }

    private static JComponent shortcutsOf(Commands commands) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                DialogStyle.GAP_M, DialogStyle.GAP_M, DialogStyle.GAP_M, DialogStyle.GAP_M));
        GridBagConstraints at = new GridBagConstraints();
        at.gridx = 0;
        at.gridy = 0;
        at.anchor = GridBagConstraints.WEST;
        at.insets = new Insets(0, 0, 2, DialogStyle.GAP_M);

        for (ShortcutList.Group group : ShortcutList.of(commands)) {
            at.gridwidth = 2;
            at.insets = new Insets(at.gridy == 0 ? 0 : DialogStyle.GAP_M, 0, DialogStyle.GAP_S, 0);
            panel.add(title(group.title()), at);
            at.gridy++;
            at.gridwidth = 1;
            for (ShortcutList.Entry entry : group.entries()) {
                at.gridx = 0;
                at.insets = new Insets(0, 0, 2, DialogStyle.GAP_M);
                panel.add(new JLabel(entry.label()), at);
                at.gridx = 1;
                panel.add(shortcut(entry.shortcut()), at);
                at.gridy++;
            }
            at.gridx = 0;
        }
        return panel;
    }

    private static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        return label;
    }

    private static JLabel shortcut(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, label.getFont().getSize()));
        return label;
    }
}

package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public final class DialogShell {

    static final int WINDOW_CHROME_HEIGHT = 80;

    private DialogShell() {
    }

    static int availableContentHeight(int screenHeight, int southHeight) {
        return screenHeight - southHeight - WINDOW_CHROME_HEIGHT;
    }

    public static boolean ask(Component parent, String title, JComponent content) {
        return ask(parent, title, content, Texts.get("common.accept"), null);
    }

    public static boolean ask(Component parent, String title, JComponent content, String acceptLabel) {
        return ask(parent, title, content, acceptLabel, null);
    }

    public static boolean ask(Component parent, String title, JComponent content, JComponent initialFocus) {
        return ask(parent, title, content, Texts.get("common.accept"), initialFocus);
    }

    public static boolean ask(
            Component parent, String title, JComponent content, String acceptLabel, JComponent initialFocus) {
        return ask(parent, title, content, null, acceptLabel, initialFocus);
    }

    public static boolean ask(
            Component parent, String title, JComponent content, JComponent extraButtons,
            String acceptLabel, JComponent initialFocus) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        ButtonBar buttons = ButtonBar.acceptCancel(acceptLabel);
        boolean[] accepted = {false};

        buttons.acceptButton().addActionListener(event -> {
            accepted[0] = true;
            dialog.dispose();
        });
        buttons.cancelButton().addActionListener(event -> dialog.dispose());

        dialog.getRootPane().setDefaultButton(buttons.acceptButton());
        dialog.getRootPane().registerKeyboardAction(
                event -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JRootPane.WHEN_IN_FOCUSED_WINDOW);

        if (initialFocus != null) {
            dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowGainedFocus(java.awt.event.WindowEvent event) {
                    initialFocus.requestFocusInWindow();
                }
            });
        }

        JComponent south = southOf(extraButtons, buttons);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(fittedToScreen(content, south), BorderLayout.CENTER);
        dialog.getContentPane().add(south, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return accepted[0];
    }

    private static JComponent southOf(JComponent extraButtons, JComponent buttons) {
        if (extraButtons == null) {
            return buttons;
        }
        JComponent south = new javax.swing.JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(extraButtons, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        return south;
    }

    private static JComponent fittedToScreen(JComponent content, JComponent southBar) {
        Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
        int availableHeight = availableContentHeight(screen.height, southBar.getPreferredSize().height);
        return fitToAvailableHeight(content, availableHeight);
    }

    static JComponent fitToAvailableHeight(JComponent content, int availableHeight) {
        if (content.getPreferredSize().height <= availableHeight) {
            return content;
        }
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new java.awt.Dimension(
                content.getPreferredSize().width + scroll.getVerticalScrollBar().getPreferredSize().width,
                availableHeight));
        return scroll;
    }

    public static void show(Component parent, String title, JComponent content) {
        show(parent, title, closer -> content);
    }

    public static void show(Component parent, String title, java.util.function.Function<Runnable, JComponent> content) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        javax.swing.JButton close = DialogStyle.flatButton(Texts.get("common.close"));
        close.addActionListener(event -> dialog.dispose());
        javax.swing.JPanel bar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        bar.add(close);

        dialog.getRootPane().setDefaultButton(close);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(fittedToScreen(content.apply(dialog::dispose), bar), BorderLayout.CENTER);
        dialog.getContentPane().add(bar, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}

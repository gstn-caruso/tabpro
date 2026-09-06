package com.gstncaruso.tabpro.ui.dialogs.style;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Envuelve un panel de contenido con una barra Aceptar/Cancelar y lo muestra como
 * dialogo modal. No sabe nada del modelo que edita cada ventana: solo pinta.
 */
public final class DialogShell {

    private DialogShell() {
    }

    public static boolean ask(Component parent, String title, JComponent content) {
        return ask(parent, title, content, "Aceptar");
    }

    public static boolean ask(Component parent, String title, JComponent content, String acceptLabel) {
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

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(content, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return accepted[0];
    }

    /** Para ventanas sin Cancelar, como los reportes de un asistente: solo Cerrar. */
    public static void show(Component parent, String title, JComponent content) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        javax.swing.JButton close = DialogStyle.flatButton("Cerrar");
        close.addActionListener(event -> dialog.dispose());
        javax.swing.JPanel bar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        bar.add(close);

        dialog.getRootPane().setDefaultButton(close);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(content, BorderLayout.CENTER);
        dialog.getContentPane().add(bar, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}

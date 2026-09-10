package com.gstncaruso.tabpro.ui.dialogs.style;

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

/**
 * Envuelve un panel de contenido con una barra Aceptar/Cancelar y lo muestra como
 * dialogo modal. No sabe nada del modelo que edita cada ventana: solo pinta.
 */
public final class DialogShell {

    /** Barra de titulo y bordes del sistema operativo, mas un margen de aire contra el borde de pantalla. */
    static final int WINDOW_CHROME_HEIGHT = 80;

    private DialogShell() {
    }

    /** Lo que le queda al contenido despues de restarle a la pantalla la barra de botones y el chrome de la ventana. */
    static int availableContentHeight(int screenHeight, int southHeight) {
        return screenHeight - southHeight - WINDOW_CHROME_HEIGHT;
    }

    public static boolean ask(Component parent, String title, JComponent content) {
        return ask(parent, title, content, "Aceptar", null);
    }

    public static boolean ask(Component parent, String title, JComponent content, String acceptLabel) {
        return ask(parent, title, content, acceptLabel, null);
    }

    /** Como {@link #ask(Component, String, JComponent)}, pero arranca con el foco en {@code initialFocus}. */
    public static boolean ask(Component parent, String title, JComponent content, JComponent initialFocus) {
        return ask(parent, title, content, "Aceptar", initialFocus);
    }

    public static boolean ask(
            Component parent, String title, JComponent content, String acceptLabel, JComponent initialFocus) {
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

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(fittedToScreen(content, buttons), BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return accepted[0];
    }

    private static JComponent fittedToScreen(JComponent content, JComponent southBar) {
        Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
        int availableHeight = availableContentHeight(screen.height, southBar.getPreferredSize().height);
        return fitToAvailableHeight(content, availableHeight);
    }

    /**
     * La regla general: un dialogo nunca es mas alto que el area util de la pantalla. El alto
     * disponible se recibe como parametro (nunca leido de {@code GraphicsEnvironment} aca adentro)
     * para poder probarlo sin depender de un display real.
     */
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

    /** Para ventanas sin Cancelar, como los reportes de un asistente: solo Cerrar. */
    public static void show(Component parent, String title, JComponent content) {
        show(parent, title, closer -> content);
    }

    /**
     * Como {@link #show(Component, String, JComponent)}, pero el contenido se arma con acceso a
     * un cierre propio: sirve para botones internos (un "Ir a" que ademas de moverse, cierra la
     * ventana) sin depender solo del boton "Cerrar" de la barra.
     */
    public static void show(Component parent, String title, java.util.function.Function<Runnable, JComponent> content) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        javax.swing.JButton close = DialogStyle.flatButton("Cerrar");
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

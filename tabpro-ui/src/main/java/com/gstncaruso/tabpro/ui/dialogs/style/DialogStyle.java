package com.gstncaruso.tabpro.ui.dialogs.style;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Los numeros y helpers que comparten todas las ventanas del manual, para que se
 * vean iguales: plano, con aire, sin bordes 3D. Nunca fija colores propios, solo
 * lee las claves del look and feel activo (UIManager) para respetar el tema oscuro.
 */
public final class DialogStyle {

    public static final int GAP_XS = 4;
    public static final int GAP_S = 8;
    public static final int GAP_M = 16;
    public static final int GAP_L = 24;

    public static final int TEXT_FIELD_COLUMNS = 24;
    public static final int SHORT_FIELD_COLUMNS = 6;

    private DialogStyle() {
    }

    /** El titulo de una seccion dentro de una ventana con varios grupos de campos. */
    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(sectionFont());
        label.setBorder(BorderFactory.createEmptyBorder(GAP_S, 0, GAP_XS, 0));
        return label;
    }

    public static Font sectionFont() {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new JLabel().getFont();
        }
        return base.deriveFont(Font.BOLD);
    }

    /** Aire uniforme alrededor del contenido de una ventana. */
    public static void padded(JComponent component) {
        component.setBorder(BorderFactory.createEmptyBorder(GAP_L, GAP_L, GAP_L, GAP_L));
    }

    /** Un boton plano: sin foco pintado ni relieve, con aire adentro. */
    public static JButton flatButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setMargin(new java.awt.Insets(GAP_XS, GAP_M, GAP_XS, GAP_M));
        return button;
    }
}

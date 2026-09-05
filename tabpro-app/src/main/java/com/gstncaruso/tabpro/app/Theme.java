package com.gstncaruso.tabpro.app;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

/**
 * El aspecto de tabpro: oscuro, plano y con un acento calido. Se apoya en
 * FlatLaf y le agrega las claves propias que usa la interfaz.
 */
public final class Theme {

    private static final Color BACKGROUND = new Color(0x1E1F22);
    private static final Color PANEL = new Color(0x2B2D30);
    private static final Color RAISED_PANEL = new Color(0x35373B);
    private static final Color SEPARATOR = new Color(0x3C3F41);
    private static final Color TEXT = new Color(0xD7D9DD);
    private static final Color MUTED_TEXT = new Color(0x8B8F96);
    private static final Color ACCENT = new Color(0xE8A33D);
    private static final Color WARNING = new Color(0xE05C5C);
    private static final Color PAPER = new Color(0xF6F3EC);
    private static final Color INK = new Color(0x1A1A1A);

    private Theme() {
    }

    public static void install() {
        FlatDarkLaf.setup();
        UIManager.put("tabpro.background", BACKGROUND);
        UIManager.put("tabpro.panel", PANEL);
        UIManager.put("tabpro.raisedPanel", RAISED_PANEL);
        UIManager.put("tabpro.separator", SEPARATOR);
        UIManager.put("tabpro.text", TEXT);
        UIManager.put("tabpro.mutedText", MUTED_TEXT);
        UIManager.put("tabpro.accent", ACCENT);
        UIManager.put("tabpro.warning", WARNING);
        UIManager.put("tabpro.paper", PAPER);
        UIManager.put("tabpro.ink", INK);

        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.borderColor", SEPARATOR);
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ProgressBar.arc", 6);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Panel.background", PANEL);
        UIManager.put("ToolBar.background", PANEL);
        UIManager.put("ToolBar.separatorColor", SEPARATOR);
        UIManager.put("MenuBar.background", PANEL);
        UIManager.put("Menu.selectionBackground", ACCENT);
        UIManager.put("MenuItem.selectionBackground", ACCENT);
        UIManager.put("Menu.selectionForeground", BACKGROUND);
        UIManager.put("MenuItem.selectionForeground", BACKGROUND);
        UIManager.put("CheckBoxMenuItem.selectionBackground", ACCENT);
        UIManager.put("CheckBoxMenuItem.selectionForeground", BACKGROUND);
        UIManager.put("RadioButtonMenuItem.selectionBackground", ACCENT);
        UIManager.put("RadioButtonMenuItem.selectionForeground", BACKGROUND);
        UIManager.put("SplitPane.background", SEPARATOR);
        UIManager.put("SplitPaneDivider.gripColor", MUTED_TEXT);
        UIManager.put("SplitPane.dividerSize", 5);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("Table.gridColor", SEPARATOR);
        UIManager.put("Table.selectionBackground", ACCENT.darker());
        UIManager.put("Slider.thumbColor", ACCENT);
        UIManager.put("Slider.trackValueColor", ACCENT);
        UIManager.put("defaultFont", interfaceFont());
    }

    /** Una tipografia de interfaz chica y prolija, que es lo que pide una partitura. */
    private static Font interfaceFont() {
        Font base = UIManager.getFont("defaultFont");
        return base == null ? new Font(Font.SANS_SERIF, Font.PLAIN, 12) : base.deriveFont(12f);
    }
}

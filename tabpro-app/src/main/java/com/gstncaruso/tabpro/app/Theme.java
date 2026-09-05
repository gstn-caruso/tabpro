package com.gstncaruso.tabpro.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.UIManager;

/**
 * El aspecto de tabpro: plano y con un acento calido, en su version oscura o
 * clara. Es lo que el manual llama "skins", pero sin la estetica de Windows XP.
 */
public final class Theme implements ThemeSwitch {

    public static final String DARK = "Oscuro";
    public static final String LIGHT = "Claro";

    private static final Color ACCENT = new Color(0xE8A33D);
    private static final Color WARNING = new Color(0xE05C5C);

    private static final Map<String, Palette> PALETTES = Map.of(
            DARK, new Palette(
                    new Color(0x1E1F22), new Color(0x2B2D30), new Color(0x35373B), new Color(0x3C3F41),
                    new Color(0xD7D9DD), new Color(0x8B8F96), new Color(0xF6F3EC), new Color(0x1A1A1A)),
            LIGHT, new Palette(
                    new Color(0xEFEFF1), new Color(0xF7F7F9), new Color(0xFFFFFF), new Color(0xD4D6DA),
                    new Color(0x24262A), new Color(0x6B7078), new Color(0xFFFFFF), new Color(0x101010)));

    private String current = DARK;

    public static Theme install() {
        Theme theme = new Theme();
        theme.apply(DARK);
        return theme;
    }

    @Override
    public List<String> names() {
        return List.of(DARK, LIGHT);
    }

    @Override
    public String current() {
        return current;
    }

    @Override
    public void apply(String name) {
        Palette palette = PALETTES.getOrDefault(name, PALETTES.get(DARK));
        if (LIGHT.equals(name)) {
            FlatLightLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }
        current = PALETTES.containsKey(name) ? name : DARK;
        putPalette(palette);
        putFlatLafTweaks();
    }

    private static void putPalette(Palette palette) {
        UIManager.put("tabpro.background", palette.background());
        UIManager.put("tabpro.panel", palette.panel());
        UIManager.put("tabpro.raisedPanel", palette.raisedPanel());
        UIManager.put("tabpro.separator", palette.separator());
        UIManager.put("tabpro.text", palette.text());
        UIManager.put("tabpro.mutedText", palette.mutedText());
        UIManager.put("tabpro.paper", palette.paper());
        UIManager.put("tabpro.ink", palette.ink());
        UIManager.put("tabpro.accent", ACCENT);
        UIManager.put("tabpro.warning", WARNING);
        UIManager.put("Panel.background", palette.panel());
        UIManager.put("ToolBar.background", palette.panel());
        UIManager.put("MenuBar.background", palette.panel());
        UIManager.put("Component.borderColor", palette.separator());
        UIManager.put("ToolBar.separatorColor", palette.separator());
        UIManager.put("SplitPane.background", palette.separator());
        UIManager.put("Table.gridColor", palette.separator());
        UIManager.put("SplitPaneDivider.gripColor", palette.mutedText());
    }

    private static void putFlatLafTweaks() {
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ProgressBar.arc", 6);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Menu.selectionBackground", ACCENT);
        UIManager.put("MenuItem.selectionBackground", ACCENT);
        UIManager.put("Menu.selectionForeground", Color.BLACK);
        UIManager.put("MenuItem.selectionForeground", Color.BLACK);
        UIManager.put("CheckBoxMenuItem.selectionBackground", ACCENT);
        UIManager.put("CheckBoxMenuItem.selectionForeground", Color.BLACK);
        UIManager.put("RadioButtonMenuItem.selectionBackground", ACCENT);
        UIManager.put("RadioButtonMenuItem.selectionForeground", Color.BLACK);
        UIManager.put("SplitPane.dividerSize", 5);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("Slider.thumbColor", ACCENT);
        UIManager.put("Slider.trackValueColor", ACCENT);
        UIManager.put("defaultFont", interfaceFont());
    }

    /** Una tipografia de interfaz chica y prolija, que es lo que pide una partitura. */
    private static Font interfaceFont() {
        Font base = UIManager.getFont("defaultFont");
        return base == null ? new Font(Font.SANS_SERIF, Font.PLAIN, 12) : base.deriveFont(12f);
    }

    private record Palette(
            Color background, Color panel, Color raisedPanel, Color separator,
            Color text, Color mutedText, Color paper, Color ink) {
    }
}

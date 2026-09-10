package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.page.DefaultPageSetup;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.JPanel;

public final class PageSetupDialog {

    private PageSetupDialog() {
    }

    public static Optional<PageSetup> ask(Component parent, PageSetup current, Consumer<PageSetup> refresh) {
        return ask(parent, current, refresh, DefaultPageSetup.userSetup());
    }

    public static Optional<PageSetup> ask(
            Component parent, PageSetup current, Consumer<PageSetup> refresh, DefaultPageSetup defaults) {
        PageSetupPanel panel = new PageSetupPanel(current);

        boolean accepted = DialogShell.ask(
                parent, "Configurar página", panel, extraButtons(panel, refresh, defaults), "Aceptar", null);
        return accepted ? Optional.of(panel.toPageSetup()) : Optional.empty();
    }

    private static JPanel extraButtons(PageSetupPanel panel, Consumer<PageSetup> refresh, DefaultPageSetup defaults) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        buttons.setOpaque(false);
        buttons.add(button("Actualizar partitura", () -> refresh.accept(panel.toPageSetup())));
        buttons.add(button("Guardar como configuración por defecto", () -> defaults.save(panel.toPageSetup())));
        buttons.add(button("Aplicar configuración por defecto", () -> {
            panel.apply(defaults.get());
            refresh.accept(panel.toPageSetup());
        }));
        return buttons;
    }

    private static javax.swing.JButton button(String label, Runnable action) {
        javax.swing.JButton button = DialogStyle.flatButton(label);
        button.addActionListener(event -> action.run());
        return button;
    }
}

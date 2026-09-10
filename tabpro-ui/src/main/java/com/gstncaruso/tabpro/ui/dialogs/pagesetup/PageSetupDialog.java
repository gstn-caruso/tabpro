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

/**
 * La ventana de Configurar pagina [F8]. Ademas de Aceptar y Cancelar trae los tres botones del
 * manual: Actualizar partitura, que aplica lo que hay en la ventana sin cerrarla ni validar nada;
 * Guardar como configuracion por defecto, que deja lo de ahora para las partituras nuevas; y
 * Aplicar configuracion por defecto, para cuando uno abre un archivo ajeno con un papel raro.
 */
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
                parent, "Configurar pagina", panel, extraButtons(panel, refresh, defaults), "Aceptar", null);
        return accepted ? Optional.of(panel.toPageSetup()) : Optional.empty();
    }

    /**
     * Los tres botones extra del manual, aparte de Aceptar y Cancelar: quedan siempre visibles
     * fuera de cualquier scroll (regla general de {@link DialogShell}), nunca adentro del
     * formulario que puede llegar a scrollear en una pantalla chica.
     */
    private static JPanel extraButtons(PageSetupPanel panel, Consumer<PageSetup> refresh, DefaultPageSetup defaults) {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, DialogStyle.GAP_S, DialogStyle.GAP_S));
        buttons.setOpaque(false);
        buttons.add(button("Actualizar partitura", () -> refresh.accept(panel.toPageSetup())));
        buttons.add(button("Guardar como configuracion por defecto", () -> defaults.save(panel.toPageSetup())));
        buttons.add(button("Aplicar configuracion por defecto", () -> {
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

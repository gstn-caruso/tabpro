package com.gstncaruso.tabpro.ui.dialogs.pagesetup;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.util.Optional;

/** La ventana de Configurar pagina [F8]. */
public final class PageSetupDialog {

    private PageSetupDialog() {
    }

    public static Optional<PageSetup> ask(Component parent, PageSetup current) {
        PageSetupPanel panel = new PageSetupPanel(current);

        boolean accepted = DialogShell.ask(parent, "Configurar pagina", panel);
        return accepted ? Optional.of(panel.toPageSetup()) : Optional.empty();
    }
}

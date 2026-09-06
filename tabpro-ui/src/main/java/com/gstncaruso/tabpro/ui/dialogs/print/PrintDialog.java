package com.gstncaruso.tabpro.ui.dialogs.print;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.print.PrintSettings;
import com.gstncaruso.tabpro.ui.print.ScorePrinting;
import java.awt.Component;
import java.util.Optional;

/** La ventana de Imprimir: que hojas salen y de que tamano. */
public final class PrintDialog {

    private PrintDialog() {
    }

    public static Optional<PrintSettings> ask(Component parent, int sheetCount) {
        PrintPanel panel = new PrintPanel(sheetCount);
        panel.configureButton().addActionListener(event -> ScorePrinting.configurePrinterPage());

        boolean accepted = DialogShell.ask(parent, "Imprimir", panel, "Imprimir");
        return accepted ? Optional.of(panel.toPrintSettings()) : Optional.empty();
    }
}

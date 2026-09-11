package com.gstncaruso.tabpro.ui.dialogs.print;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.print.PrintSettings;
import com.gstncaruso.tabpro.ui.print.ScorePrinting;
import java.awt.Component;
import java.util.Optional;

public final class PrintDialog {

    private PrintDialog() {
    }

    public static Optional<PrintSettings> ask(Component parent, int sheetCount, ScorePrinting printing) {
        PrintPanel panel = new PrintPanel(sheetCount);
        panel.configureButton().addActionListener(event -> printing.configurePrinterPage());

        boolean accepted = DialogShell.ask(
                parent, Texts.get("score_dialogs.shared.print"), panel, Texts.get("score_dialogs.shared.print"));
        return accepted ? Optional.of(panel.toPrintSettings()) : Optional.empty();
    }
}

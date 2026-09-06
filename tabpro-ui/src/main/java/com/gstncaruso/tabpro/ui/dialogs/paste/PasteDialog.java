package com.gstncaruso.tabpro.ui.dialogs.paste;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;

/** La ventana de Pegar. */
public final class PasteDialog {

    private PasteDialog() {
    }

    public static void show(Component parent, Editor editor) {
        PastePanel panel = new PastePanel();

        boolean accepted = DialogShell.ask(parent, "Pegar", panel, "Pegar");
        if (accepted) {
            editor.paste(panel.toPasteOptions());
        }
    }
}

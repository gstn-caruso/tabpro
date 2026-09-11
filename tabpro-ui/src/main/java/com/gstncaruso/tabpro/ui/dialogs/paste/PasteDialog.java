package com.gstncaruso.tabpro.ui.dialogs.paste;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;

public final class PasteDialog {

    private PasteDialog() {
    }

    public static void show(Component parent, Editor editor) {
        PastePanel panel = new PastePanel();

        boolean accepted = DialogShell.ask(
                parent, Texts.get("edit_dialogs.PasteDialog.title"), panel, Texts.get("edit_dialogs.PasteDialog.title"));
        if (accepted) {
            editor.paste(panel.toPasteOptions());
        }
    }
}

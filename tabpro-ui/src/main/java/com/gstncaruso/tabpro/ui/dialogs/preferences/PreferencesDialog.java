package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.util.Optional;

public final class PreferencesDialog {

    private PreferencesDialog() {
    }

    public static Optional<Preferences> ask(Component parent, Preferences current) {
        PreferencesPanel panel = new PreferencesPanel(current);

        boolean accepted = DialogShell.ask(parent, Texts.get("score_dialogs.PreferencesDialog.title"), panel);
        return accepted ? Optional.of(panel.toPreferences()) : Optional.empty();
    }
}

package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.Component;
import java.util.Optional;

/** La ventana de Preferencias [F12]. */
public final class PreferencesDialog {

    private PreferencesDialog() {
    }

    public static Optional<Preferences> ask(Component parent, Preferences current) {
        PreferencesPanel panel = new PreferencesPanel(current);

        boolean accepted = DialogShell.ask(parent, "Preferencias", panel);
        return accepted ? Optional.of(panel.toPreferences()) : Optional.empty();
    }
}

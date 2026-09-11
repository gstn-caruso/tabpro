package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesPanel;
import com.gstncaruso.tabpro.ui.i18n.Language;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Tag("integration")
@Isolated
class InterfaceLanguagePreferenceAuditTest {

    private final Preferences preferences = new Preferences();

    @AfterEach
    void restoreTheDefault() {
        preferences.setInterfaceLanguage(Language.AUTOMATIC);
    }

    @Test
    void thePreferencesDialogOpensOnTheStoredInterfaceLanguage() throws Exception {
        preferences.setInterfaceLanguage(Language.ENGLISH);
        MainFrame frame = AuditSupport.newFrame(AuditSupport.blankEditor());
        try {
            Language[] shown = new Language[1];
            JMenuItem preferencesItem = AuditSupport.findMenuItem(frame.getJMenuBar(), "Preferencias…");
            AuditSupport.withDialog(preferencesItem::doClick, dialog -> {
                shown[0] = AuditSupport.findComponent(dialog, PreferencesPanel.class).toPreferences().interfaceLanguage();
                AuditSupport.findButton(dialog, "Cancelar").doClick();
            });

            assertEquals(Language.ENGLISH, shown[0]);
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void acceptingEnglishStoresItAsTheInterfaceLanguage() throws Exception {
        MainFrame frame = AuditSupport.newFrame(AuditSupport.blankEditor());
        try {
            JMenuItem preferencesItem = AuditSupport.findMenuItem(frame.getJMenuBar(), "Preferencias…");
            AuditSupport.withDialog(preferencesItem::doClick, dialog -> {
                PreferencesPanel panel = AuditSupport.findComponent(dialog, PreferencesPanel.class);
                panel.apply(panel.toPreferences().withInterfaceLanguage(Language.ENGLISH));
                AuditSupport.findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(Language.ENGLISH, new Preferences().interfaceLanguage());
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}

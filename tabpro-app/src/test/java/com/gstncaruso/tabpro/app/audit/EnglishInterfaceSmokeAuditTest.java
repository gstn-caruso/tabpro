package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Tag("integration")
@Isolated
class EnglishInterfaceSmokeAuditTest {

    @Test
    void theMainWindowAndItsDialogsSpeakEnglishOnceEnglishIsInstalled() throws Exception {
        Texts.install(Locale.ENGLISH);
        MainFrame frame = null;
        try {
            frame = AuditSupport.newFrame(AuditSupport.blankEditor());
            JMenuBar bar = frame.getJMenuBar();
            List<String> dialogTexts = new ArrayList<>();

            AuditSupport.withDialog(AuditSupport.findMenuItem(bar, "Preferences…")::doClick, dialog -> {
                dialogTexts.add(dialog.getTitle());
                assertNotNull(AuditSupport.findButton(dialog, "OK"));
                AuditSupport.findButton(dialog, "Cancel").doClick();
            });
            AuditSupport.withDialog(AuditSupport.findMenuItem(bar, "Key Signature…")::doClick, dialog -> {
                JTabbedPane tabs = AuditSupport.findComponent(dialog, JTabbedPane.class);
                dialogTexts.add(dialog.getTitle());
                dialogTexts.add(tabs.getTitleAt(tabs.getSelectedIndex()));
                AuditSupport.findButton(dialog, "Cancel").doClick();
            });

            assertEquals("File", bar.getMenu(0).getText());
            assertTrue(menuTitles(bar).contains("Track"), menuTitles(bar).toString());
            assertEquals(List.of("Preferences", "Measure Properties", "Key Signature"), dialogTexts);
        } finally {
            if (frame != null) {
                AuditSupport.dispose(frame);
            }
            Texts.install(Locale.forLanguageTag("es"));
        }
    }

    private static List<String> menuTitles(JMenuBar bar) {
        List<String> titles = new ArrayList<>();
        for (int index = 0; index < bar.getMenuCount(); index++) {
            titles.add(bar.getMenu(index).getText());
        }
        return titles;
    }
}

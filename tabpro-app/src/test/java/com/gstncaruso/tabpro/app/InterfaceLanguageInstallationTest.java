package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class InterfaceLanguageInstallationTest {

    private final Locale originalLocale = Locale.getDefault();
    private final Locale originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY);
    private final Locale originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
    private final Locale originalSwingLocale = JComponent.getDefaultLocale();
    private LookAndFeel originalLaf;
    private final Map<Object, Object> originalOverrides = new HashMap<>();
    private final java.util.prefs.Preferences node = java.util.prefs.Preferences.userRoot()
            .node("tabpro-test/" + getClass().getSimpleName() + "/" + UUID.randomUUID());
    private final Preferences preferences = new Preferences(node);

    @BeforeEach
    void rememberUiState() {
        originalLaf = UIManager.getLookAndFeel();
        UIManager.getDefaults().forEach(originalOverrides::put);
    }

    @AfterEach
    void cleanUp() throws Exception {
        Map<Object, Object> overrides = new HashMap<>();
        UIManager.getDefaults().forEach(overrides::put);
        overrides.keySet().forEach(key -> UIManager.put(key, null));
        UIManager.setLookAndFeel(originalLaf);
        originalOverrides.forEach(UIManager::put);
        Locale.setDefault(originalLocale);
        Locale.setDefault(Locale.Category.DISPLAY, originalDisplayLocale);
        Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale);
        JComponent.setDefaultLocale(originalSwingLocale);
        Texts.install(Locale.forLanguageTag("es"));
        node.removeNode();
    }

    @Test
    void unsupportedSystemLanguagesUseEnglishForTheWholeInterface() {
        new InterfaceLanguageStartup(Locale.forLanguageTag("pt-BR"), preferences).install();

        assertEquals(Locale.ENGLISH, Locale.getDefault(Locale.Category.DISPLAY));
        assertEquals("OK", Texts.get("common.accept"));
        assertEquals(Locale.ENGLISH, new JFileChooser().getLocale());
        assertEquals("Yes", UIManager.getString("OptionPane.yesButtonText"));
        assertEquals("All Files", new JFileChooser().getAcceptAllFileFilter().getDescription());
    }

    @Test
    void automaticUsesTheSystemDisplayLanguageWithoutChangingRegionalFormats() {
        Locale.setDefault(Locale.US);
        Locale.setDefault(Locale.Category.DISPLAY, Locale.forLanguageTag("es-AR"));
        Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);

        new InterfaceLanguageStartup(Locale.getDefault(Locale.Category.DISPLAY), preferences).install();

        assertEquals("Aceptar", Texts.get("common.accept"));
        assertEquals("es", JComponent.getDefaultLocale().getLanguage());
        assertEquals("es", Locale.getDefault().getLanguage());
        assertEquals(Locale.GERMANY, Locale.getDefault(Locale.Category.FORMAT));
    }

    @Test
    void spanishAlsoLocalizesStandardSwingDialogsAndSurvivesAThemeChange() {
        new InterfaceLanguageStartup(Locale.forLanguageTag("es-AR"), preferences).install();
        Theme.install().apply(Theme.LIGHT);

        assertEquals("Sí", UIManager.getString("OptionPane.yesButtonText"));
        assertEquals("Cancelar", UIManager.getString("OptionPane.cancelButtonText"));
        assertEquals("Abrir", UIManager.getString("FileChooser.openButtonText"));
        assertEquals("Todos los archivos", new JFileChooser().getAcceptAllFileFilter().getDescription());
    }
}

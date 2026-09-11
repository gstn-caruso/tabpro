package com.gstncaruso.tabpro.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.a11y.MnemonicWalker;
import com.gstncaruso.tabpro.ui.a11y.Violation;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Test;

class MenuBarTest {

    private final Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
    private final List<Path> opened = new ArrayList<>();
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void withNoRecentFilesTheFileMenuDoesNotOfferTheSubmenu() {
        JMenuBar bar = new MenuBar(commands, List::of, opened::add).build();

        assertNull(recentFilesMenuOf(bar));
    }

    @Test
    void offersEveryRecentFileWithItsName() {
        List<Path> recent = List.of(Path.of("/tmp/one.tabpro"), Path.of("/tmp/another.tabpro"));
        JMenuBar bar = new MenuBar(commands, () -> recent, opened::add).build();

        JMenu recentMenu = recentFilesMenuOf(bar);

        assertEquals(2, recentMenu.getItemCount());
        assertEquals("one.tabpro", recentMenu.getItem(0).getText());
        assertEquals("another.tabpro", recentMenu.getItem(1).getText());
    }

    @Test
    void choosingARecentFileOpensIt() {
        Path path = Path.of("/tmp/one.tabpro");
        JMenuBar bar = new MenuBar(commands, () -> List.of(path), opened::add).build();

        recentFilesMenuOf(bar).getItem(0).doClick();

        assertEquals(List.of(path), opened);
    }

    @Test
    void everyCommandWithAnAcceleratorHangsFromSomeMenu() {
        JMenuBar bar = new MenuBar(commands).build();
        Set<Command> inTheMenu = new HashSet<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            collect(bar.getMenu(i), inTheMenu);
        }

        long withAccelerator = commands.all().values().stream().filter(c -> c.accelerator() != null).count();
        List<String> stray = commands.all().entrySet().stream()
                .filter(entry -> entry.getValue().accelerator() != null)
                .filter(entry -> !inTheMenu.contains(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        assertTrue(withAccelerator > 0, "no command has an accelerator: there would be nothing to check");
        assertEquals(List.of(), stray, "there are shortcuts that do not hang from any menu");
    }

    @Test
    void noMenuItemIsLeftWithoutAnAccessibleNameOrTooltip() {
        JMenuBar bar = new MenuBar(commands).build();

        AccessibilityAssertions.assertNoViolations(bar);
    }

    @Test
    void everyMenuInTheBarHasAMnemonicAndNoClashes() {
        JMenuBar bar = new MenuBar(commands).build();

        assertEquals(List.of(), new MnemonicWalker().walkMenuBar(bar));
    }

    @Test
    void noTopLevelMenuUsesTheSameLetterAsAnExistingAltAccelerator() {
        JMenuBar bar = new MenuBar(commands).build();
        Set<Integer> menuMnemonics = new HashSet<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            menuMnemonics.add(bar.getMenu(i).getMnemonic());
        }

        List<javax.swing.KeyStroke> altLetterAccelerators = commands.all().values().stream()
                .map(Command::accelerator)
                .filter(java.util.Objects::nonNull)
                .filter(accelerator -> (accelerator.getModifiers() & java.awt.event.InputEvent.ALT_DOWN_MASK) != 0)
                .toList();

        assertTrue(!altLetterAccelerators.isEmpty(), "no Alt+letter accelerator to check");
        assertTrue(altLetterAccelerators.stream().noneMatch(a -> menuMnemonics.contains(a.getKeyCode())));
    }

    @Test
    void noItemOfAnyMenuClashesWithAnotherInTheSameMenu() {
        List<Violation> violations = mnemonicViolationsOfEveryItem(new MenuBar(commands).build());

        assertTrue(violations.stream().noneMatch(v -> v.reason().equals("duplicate mnemonic")));
    }

    @Test
    void onlyItemsOfTheMostCrowdedMenusAreLeftWithoutAFreeLetter() {
        List<Violation> violations = mnemonicViolationsOfEveryItem(new MenuBar(commands).build());

        Set<String> withoutMnemonic = violations.stream()
                .filter(v -> v.reason().equals("missing mnemonic"))
                .map(Violation::path)
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        assertEquals(Set.of(
                "Acorde…", "Barra de unión", "Plica",
                "Nota muerta", "Nota acentuada", "Fade in", "Nota de adorno…", "Armónicos…",
                "Armónico natural", "Armónico artificial",
                "Slap", "Pop", "Rasgueo y púa",
                "Último compás",
                "Mesa de mezcla",
                "pp", "p", "mp", "f", "ff", "fff"), withoutMnemonic);
    }

    private List<Violation> mnemonicViolationsOfEveryItem(JMenuBar bar) {
        MnemonicWalker walker = new MnemonicWalker();
        List<Violation> violations = new ArrayList<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            collectMnemonicViolations(bar.getMenu(i), walker, violations);
        }
        return violations;
    }

    private void collectMnemonicViolations(JMenu menu, MnemonicWalker walker, List<Violation> violations) {
        violations.addAll(walker.walkMenu(menu));
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item instanceof JMenu submenu) {
                collectMnemonicViolations(submenu, walker, violations);
            }
        }
    }

    private void collect(JMenu menu, Set<Command> found) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            if (item instanceof JMenu submenu) {
                collect(submenu, found);
            } else if (item.getAction() instanceof Command command) {
                found.add(command);
            }
        }
    }

    @Test
    void theSoundMenuOffersTheMetronomeSettings() {
        JMenuBar bar = new MenuBar(commands).build();

        JMenu sound = menuNamed(bar, "Sonido");

        assertTrue(itemLabels(sound).contains("Configuración del metrónomo…"));
    }

    @Test
    void theMarkersMenuOffersEditingTheCurrentMarker() {
        JMenuBar bar = new MenuBar(commands).build();

        JMenu markers = menuNamed(bar, "Marcadores");

        assertTrue(itemLabels(markers).contains("Editar el marcador…"));
    }

    @Test
    void theNoteMenuOffersTheEightDynamicsAlongsideTheExistingEntry() {
        JMenuBar bar = new MenuBar(commands).build();

        Set<String> labels = itemLabels(menuNamed(bar, "Nota"));

        assertTrue(labels.containsAll(
                List.of("ppp", "pp", "p", "mp", "mf", "f", "ff", "fff")));
    }

    @Test
    void theNoteMenuOffersRightHandFingeringAlongsideTheExistingOne() {
        JMenuBar bar = new MenuBar(commands).build();

        Set<String> labels = itemLabels(menuNamed(bar, "Nota"));

        assertTrue(labels.contains("Digitación (mano derecha)…"));
    }

    private JMenu menuNamed(JMenuBar bar, String name) {
        for (int i = 0; i < bar.getMenuCount(); i++) {
            JMenu menu = bar.getMenu(i);
            if (name.equals(menu.getText())) {
                return menu;
            }
        }
        return null;
    }

    private Set<String> itemLabels(JMenu menu) {
        Set<String> labels = new HashSet<>();
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null) {
                labels.add(item.getText());
            }
        }
        return labels;
    }

    @Test
    void everyMenuTitleInTheBarResolvesInEnglish() {
        Commands withATheme = new Commands(
                editor, record(Ports.Document.class), record(Ports.Dialogs.class),
                record(Ports.Playback.class), record(Ports.View.class), List.of("dark"));
        JMenuBar bar = new MenuBar(withATheme, () -> List.of(Path.of("/tmp/one.tabpro")), path -> { }).build();
        Set<String> spanishTitles = new HashSet<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            collectMenuTitles(bar.getMenu(i), spanishTitles);
        }
        Map<String, String> titleKeyBySpanishText = titleKeysBySpanishText();

        assertFalse(spanishTitles.isEmpty(), "no menu title to check");
        for (String title : spanishTitles) {
            String key = titleKeyBySpanishText.get(title);
            assertNotNull(key, "no menus.title.* key maps to \"" + title + "\"");
            assertFalse(Texts.forLocale(Locale.ENGLISH).text(key).isBlank(), key);
        }
    }

    private void collectMenuTitles(JMenu menu, Set<String> titles) {
        titles.add(menu.getText());
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item instanceof JMenu submenu) {
                collectMenuTitles(submenu, titles);
            }
        }
    }

    private Map<String, String> titleKeysBySpanishText() {
        ResourceBundle spanishTitles = ResourceBundle.getBundle(
                "com.gstncaruso.tabpro.ui.i18n.menus", Locale.forLanguageTag("es"),
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        Map<String, String> bySpanishText = new HashMap<>();
        for (String key : spanishTitles.keySet()) {
            if (key.startsWith("menus.title.")) {
                bySpanishText.put(spanishTitles.getString(key), key);
            }
        }
        return bySpanishText;
    }

    private JMenu recentFilesMenuOf(JMenuBar bar) {
        JMenu fileMenu = (JMenu) bar.getMenu(0);
        for (int i = 0; i < fileMenu.getItemCount(); i++) {
            JMenuItem item = fileMenu.getItem(i);
            if (item instanceof JMenu menu && "Abrir reciente".equals(menu.getText())) {
                return menu;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

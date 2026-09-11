package com.gstncaruso.tabpro.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.MnemonicWalker;
import com.gstncaruso.tabpro.ui.a11y.Violation;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.event.InputEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class EnglishMenuBarMnemonicsTest {

    @Test
    void theEnglishMenuBarHasNoMnemonicClashes() {
        Texts.install(Locale.ENGLISH);
        try {
            Commands commands = commands();
            JMenuBar bar = new MenuBar(commands, () -> List.of(Path.of("/tmp/one.tabpro")), path -> { }).build();
            MnemonicWalker walker = new MnemonicWalker();

            assertEquals("File", bar.getMenu(0).getText());
            assertEquals(List.of(), walker.walkMenuBar(bar));
            assertEquals(List.of(), duplicateItemMnemonics(bar, walker));
            assertFalse(topLevelMnemonicsClashWithAltAccelerators(bar, commands));
        } finally {
            Texts.install(Locale.forLanguageTag("es"));
        }
    }

    private static List<Violation> duplicateItemMnemonics(JMenuBar bar, MnemonicWalker walker) {
        List<Violation> duplicates = new ArrayList<>();
        for (int index = 0; index < bar.getMenuCount(); index++) {
            collectDuplicates(bar.getMenu(index), walker, duplicates);
        }
        return duplicates;
    }

    private static void collectDuplicates(JMenu menu, MnemonicWalker walker, List<Violation> duplicates) {
        walker.walkMenu(menu).stream()
                .filter(violation -> violation.reason().equals("duplicate mnemonic"))
                .forEach(duplicates::add);
        for (int index = 0; index < menu.getItemCount(); index++) {
            JMenuItem item = menu.getItem(index);
            if (item instanceof JMenu submenu) {
                collectDuplicates(submenu, walker, duplicates);
            }
        }
    }

    private static boolean topLevelMnemonicsClashWithAltAccelerators(JMenuBar bar, Commands commands) {
        List<Integer> menuMnemonics = new ArrayList<>();
        for (int index = 0; index < bar.getMenuCount(); index++) {
            menuMnemonics.add(bar.getMenu(index).getMnemonic());
        }
        return commands.all().values().stream()
                .map(Command::accelerator)
                .filter(Objects::nonNull)
                .filter(accelerator -> (accelerator.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0)
                .map(KeyStroke::getKeyCode)
                .anyMatch(menuMnemonics::contains);
    }

    private static Commands commands() {
        return new Commands(
                new Editor(Score.blank(new TextsDefaultNames())), port(Ports.Document.class),
                port(Ports.Dialogs.class), port(Ports.Playback.class), port(Ports.View.class), List.of("dark"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T port(Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }
}

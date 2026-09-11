package com.gstncaruso.tabpro.ui.dialogs.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ShortcutListTest {

    private final Commands commands = new Commands(
            new Editor(Score.blank()), record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void theEditingAndEffectsGroupTitlesAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Editing", english.text("edit_dialogs.ShortcutList.editing"));
        assertEquals("Effects", english.text("edit_dialogs.ShortcutList.effects"));
    }

    @Test
    void groupsTheShortcutsTheWayTheManualDoes() {
        List<ShortcutList.Group> groups = ShortcutList.of(commands);

        assertEquals(
                List.of("Edición", "Efectos", "Navegación", "Sonido", "Varios"),
                groups.stream().map(ShortcutList.Group::title).toList());
    }

    @Test
    void everyGroupListsCommandsWithTheirShortcut() {
        List<ShortcutList.Group> groups = ShortcutList.of(commands);

        groups.forEach(group -> {
            assertFalse(group.entries().isEmpty(), "group " + group.title() + " ended up empty");
            group.entries().forEach(entry -> {
                assertFalse(entry.label().isBlank());
                assertFalse(entry.shortcut().isBlank(), entry.label() + " has no shortcut");
            });
        });
    }

    @Test
    void aCommandWithoutAShortcutIsNotListed() {
        boolean listed = ShortcutList.of(commands).stream()
                .flatMap(group -> group.entries().stream())
                .anyMatch(entry -> entry.label().equals(commands.get("note.dynamics").label()));

        assertFalse(listed, "dynamics has no shortcut, it should not be listed");
    }

    @Test
    void theEffectsGroupCarriesTheLettersOfTheManual() {
        List<String> shortcuts = ShortcutList.of(commands).stream()
                .filter(group -> group.title().equals("Efectos"))
                .flatMap(group -> group.entries().stream())
                .map(ShortcutList.Entry::shortcut)
                .toList();

        assertTrue(shortcuts.contains("H"), "missing the hammer-on/pull-off: " + shortcuts);
        assertTrue(shortcuts.contains("B"), "missing the bend: " + shortcuts);
    }

    @SuppressWarnings("unchecked")
    private static <T> T record(Class<T> port) {
        return (T) Proxy.newProxyInstance(
                port.getClassLoader(), new Class<?>[] {port},
                (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null);
    }
}

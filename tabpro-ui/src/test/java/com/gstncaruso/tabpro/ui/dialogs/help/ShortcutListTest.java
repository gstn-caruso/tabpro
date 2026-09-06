package com.gstncaruso.tabpro.ui.dialogs.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShortcutListTest {

    private final Commands commands = new Commands(
            new Editor(Score.blank()), record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

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
            assertFalse(group.entries().isEmpty(), "el grupo " + group.title() + " quedo vacio");
            group.entries().forEach(entry -> {
                assertFalse(entry.label().isBlank());
                assertFalse(entry.shortcut().isBlank(), entry.label() + " no tiene atajo");
            });
        });
    }

    @Test
    void aCommandWithoutAShortcutIsNotListed() {
        boolean listed = ShortcutList.of(commands).stream()
                .flatMap(group -> group.entries().stream())
                .anyMatch(entry -> entry.label().equals(commands.get("note.dynamics").label()));

        assertFalse(listed, "la dinámica no tiene atajo, no va en la lista");
    }

    @Test
    void theEffectsGroupCarriesTheLettersOfTheManual() {
        List<String> shortcuts = ShortcutList.of(commands).stream()
                .filter(group -> group.title().equals("Efectos"))
                .flatMap(group -> group.entries().stream())
                .map(ShortcutList.Entry::shortcut)
                .toList();

        assertTrue(shortcuts.contains("H"), "falta el ligado: " + shortcuts);
        assertTrue(shortcuts.contains("B"), "falta el bend: " + shortcuts);
    }

    @SuppressWarnings("unchecked")
    private static <T> T record(Class<T> port) {
        return (T) Proxy.newProxyInstance(
                port.getClassLoader(), new Class<?>[] {port}, (proxy, method, args) -> null);
    }
}

package com.gstncaruso.tabpro.ui.dialogs.help;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Los atajos de teclado agrupados como los agrupa el capitulo Reference del manual: edicion,
 * efectos, navegacion, sonido y varios. Se arma leyendo el catalogo de comandos, asi que un
 * comando nuevo con atajo aparece solo, sin que haya que acordarse de anotarlo aca.
 */
public final class ShortcutList {

    private static final List<Section> SECTIONS = List.of(
            new Section("Edición", List.of("edit.", "bar.", "note.", "track.", "marker.insert", "marker.list")),
            new Section("Efectos", List.of("effect.")),
            new Section("Navegación", List.of("nav.", "marker.previous", "marker.next")),
            new Section("Sonido", List.of("sound.")),
            new Section("Varios", List.of("file.", "tool.", "view.", "options.", "help.")));

    private ShortcutList() {
    }

    public static List<Group> of(Commands commands) {
        List<Group> groups = new ArrayList<>();
        for (Section section : SECTIONS) {
            List<Entry> entries = entriesOf(commands.all(), section);
            if (!entries.isEmpty()) {
                groups.add(new Group(section.title(), entries));
            }
        }
        return List.copyOf(groups);
    }

    private static List<Entry> entriesOf(Map<String, Command> commands, Section section) {
        return commands.entrySet().stream()
                .filter(entry -> section.covers(entry.getKey()))
                .filter(entry -> entry.getValue().accelerator() != null)
                .map(entry -> new Entry(entry.getValue().label(), entry.getValue().acceleratorText()))
                .sorted(Comparator.comparing(Entry::label))
                .toList();
    }

    private record Section(String title, List<String> prefixes) {

        boolean covers(String commandName) {
            return prefixes.stream().anyMatch(commandName::startsWith);
        }
    }

    /** Un bloque de la ayuda, con su titulo y sus atajos. */
    public record Group(String title, List<Entry> entries) {
    }

    /** Un atajo: que hace y que teclas hay que apretar. */
    public record Entry(String label, String shortcut) {
    }
}

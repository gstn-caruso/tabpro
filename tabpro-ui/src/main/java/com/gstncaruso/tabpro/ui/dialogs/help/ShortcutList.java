package com.gstncaruso.tabpro.ui.dialogs.help;

import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ShortcutList {

    private static final List<Section> SECTIONS = List.of(
            new Section("edit_dialogs.ShortcutList.editing",
                    List.of("edit.", "bar.", "note.", "track.", "marker.insert", "marker.list")),
            new Section("edit_dialogs.ShortcutList.effects", List.of("effect.")),
            new Section("edit_dialogs.ShortcutList.navigation",
                    List.of("nav.", "marker.previous", "marker.next")),
            new Section("edit_dialogs.ShortcutList.sound", List.of("sound.")),
            new Section("edit_dialogs.ShortcutList.miscellaneous",
                    List.of("file.", "tool.", "view.", "options.", "help.")));

    private ShortcutList() {
    }

    public static List<Group> of(Commands commands) {
        List<Group> groups = new ArrayList<>();
        for (Section section : SECTIONS) {
            List<Entry> entries = entriesOf(commands.all(), section);
            if (!entries.isEmpty()) {
                groups.add(new Group(Texts.get(section.titleKey()), entries));
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

    private record Section(String titleKey, List<String> prefixes) {

        boolean covers(String commandName) {
            return prefixes.stream().anyMatch(commandName::startsWith);
        }
    }

    public record Group(String title, List<Entry> entries) {
    }

    public record Entry(String label, String shortcut) {
    }
}

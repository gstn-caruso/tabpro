package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class CommandsTest {

    private final Editor editor = new Editor(Score.blank());
    private final List<String> asked = new ArrayList<>();
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void everyCommandHasANameAndSomethingToDo() {
        commands.all().forEach((name, command) -> {
            assertNotNull(command.label(), name);
            assertFalse(command.label().isBlank(), name);
        });
    }

    @Test
    void noTwoCommandsShareTheSameShortcut() {
        Map<KeyStroke, String> byShortcut = new HashMap<>();
        commands.all().forEach((name, command) -> {
            KeyStroke shortcut = command.accelerator();
            if (shortcut == null) {
                return;
            }
            String previous = byShortcut.put(shortcut, name);
            assertEquals(null, previous, "el atajo " + shortcut + " lo usan " + previous + " y " + name);
        });
    }

    @Test
    void theShortcutsOfTheManualAreThere() {
        Set<String> expected = new HashSet<>(List.of(
                "ctrl N", "ctrl O", "ctrl S", "ctrl P", "ctrl Z", "ctrl X", "ctrl C", "ctrl V", "ctrl A",
                "F5", "F6", "F7", "F8", "F9", "F10", "F12", "SPACE"));
        commands.all().values().stream()
                .map(Command::accelerator)
                .filter(java.util.Objects::nonNull)
                .map(KeyStroke::toString)
                .forEach(shortcut -> expected.removeIf(wanted ->
                        KeyStroke.getKeyStroke(wanted).toString().equals(shortcut)));

        assertTrue(expected.isEmpty(), "faltan los atajos " + expected);
    }

    @Test
    void aCommandDoesWhatItsNameSays() {
        commands.get("note.rest").actionPerformed(event());
        assertTrue(editor.currentBeat().isRest());

        commands.get("note.value.EIGHTH").actionPerformed(event());
        assertEquals(NoteValue.EIGHTH, editor.currentBeat().duration().value());

        commands.get("edit.bassVoice").actionPerformed(event());
        assertEquals(VoicePart.BASS, editor.cursor().voice());

        commands.get("bar.forceLineBreak").actionPerformed(event());
        assertEquals(LineBreak.FORCED, editor.currentMeasure().attributes().lineBreak());

        commands.get("bar.preventLineBreak").actionPerformed(event());
        assertEquals(LineBreak.PREVENTED, editor.currentMeasure().attributes().lineBreak());

        commands.get("bar.resetLineBreak").actionPerformed(event());
        assertEquals(LineBreak.AUTOMATIC, editor.currentMeasure().attributes().lineBreak());
    }

    @Test
    void anEffectCommandReachesTheNoteUnderTheCursor() {
        editor.setFret(5);

        commands.get("effect.palmMute").actionPerformed(event());

        assertTrue(editor.currentNote().orElseThrow().has(Ornament.PALM_MUTE));
    }

    @Test
    void aCommandThatNeedsAWindowAsksForIt() {
        commands.get("file.information").actionPerformed(event());

        assertEquals(List.of("scoreInformation"), asked);
    }

    @Test
    void askingForACommandThatDoesNotExistIsAMistake() {
        assertThrows(IllegalArgumentException.class, () -> commands.get("no.existe"));
    }

    private static ActionEvent event() {
        return new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "test");
    }

    /** Un doble que anota que le pidieron, para ver que el comando llegue a destino. */
    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> {
            asked.add(method.getName());
            return null;
        };
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

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
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                "F1", "F2", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "SPACE",
                "PLUS", "MINUS", "ctrl G", "ctrl TAB", "shift TAB", "ENTER"));
        commands.all().values().stream()
                .map(Command::accelerator)
                .filter(java.util.Objects::nonNull)
                .map(KeyStroke::toString)
                .forEach(shortcut -> expected.removeIf(wanted ->
                        KeyStroke.getKeyStroke(wanted).toString().equals(shortcut)));

        assertTrue(expected.isEmpty(), "faltan los atajos " + expected);
    }

    /**
     * El manual es explicito: "+ Divide the Duration of the Notes by 2" y
     * "- Multiply the Duration of the Notes by 2". Es el atajo mas usado al escribir el ritmo.
     */
    @Test
    void plusShortensTheFigureAndMinusLengthensIt() {
        commands.get("note.value.QUARTER").actionPerformed(event());

        pressing("PLUS");
        assertEquals(NoteValue.EIGHTH, editor.currentBeat().duration().value());

        pressing("MINUS");
        pressing("MINUS");
        assertEquals(NoteValue.HALF, editor.currentBeat().duration().value());
    }

    private void pressing(String accelerator) {
        KeyStroke wanted = KeyStroke.getKeyStroke(accelerator);
        commands.all().values().stream()
                .filter(command -> wanted.equals(command.accelerator()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no hay ningun comando con el atajo " + accelerator))
                .actionPerformed(event());
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

        commands.get("bar.octave8va").actionPerformed(event());
        assertEquals(OctaveMark.OTTAVA_ALTA, editor.currentMeasure().attributes().octaveMark());

        commands.get("bar.octave8vb").actionPerformed(event());
        assertEquals(OctaveMark.OTTAVA_BASSA, editor.currentMeasure().attributes().octaveMark());

        commands.get("bar.octave15ma").actionPerformed(event());
        assertEquals(OctaveMark.QUINDICESIMA_ALTA, editor.currentMeasure().attributes().octaveMark());

        commands.get("bar.octave15mb").actionPerformed(event());
        assertEquals(OctaveMark.QUINDICESIMA_BASSA, editor.currentMeasure().attributes().octaveMark());

        commands.get("bar.octaveNone").actionPerformed(event());
        assertEquals(OctaveMark.NONE, editor.currentMeasure().attributes().octaveMark());
    }

    /**
     * El manual dice que el salto de linea vale solo para la pista activa o para la vista
     * multipista, asi que el comando tiene que consultarle a la vista en cual de las dos esta
     * antes de aplicarlo (el alcance en si lo prueba EditorBarsTest, en tabpro-core).
     */
    @Test
    void theLineBreakCommandsAskTheViewWhetherTheMultitrackViewIsOn() {
        commands.get("bar.forceLineBreak").actionPerformed(event());

        assertTrue(asked.contains("isMultitrack"));
    }

    /** Ver > Notas con dinamica [F11] del manual: el comando le avisa a la vista. */
    @Test
    void theDynamicNotesCommandTogglesTheView() {
        commands.get("view.dynamicNotes").actionPerformed(event());

        assertEquals(List.of("toggleShowsDynamicNotes"), asked);
    }

    /**
     * El manual agrupa cualquier n-tuplet igual que el tresillo (Managing the Triplets and
     * n-Tuplets): quintillo, seisillo, septillo y los que sigan tienen que estar en el menu Nota.
     */
    @Test
    void aTupletCommandGroupsTheCurrentBeatWithThatManyNotes() {
        commands.get("note.tuplet.5").actionPerformed(event());

        assertEquals(com.gstncaruso.tabpro.core.model.Tuplet.of(5), editor.currentBeat().duration().tuplet());
    }

    @Test
    void everyAvailableTupletBesidesThePlainOneAndTheTripletHasItsOwnCommand() {
        for (int enters : com.gstncaruso.tabpro.core.model.Tuplet.AVAILABLE) {
            if (enters == 1 || enters == 3) {
                continue;
            }
            assertNotNull(commands.get("note.tuplet." + enters), "falta el comando para el grupo de " + enters);
        }
    }

    @Test
    void anEffectCommandReachesTheNoteUnderTheCursor() {
        editor.setFret(5);

        commands.get("effect.palmMute").actionPerformed(event());

        assertTrue(editor.currentNote().orElseThrow().has(Ornament.PALM_MUTE));
    }

    /**
     * El manual describe seis tipos de slide (linea 1250 y siguientes: legato, con ataque, y los
     * cuatro que entran o salen de un traste indefinido). Cada uno necesita su propio comando en
     * el menu Efectos.
     */
    @Test
    void everySlideTypeTheManualDescribesHasItsOwnCommand() {
        editor.setFret(5);
        Map<String, SlideType> commandNameToType = Map.of(
                "effect.legatoSlide", SlideType.LEGATO,
                "effect.shiftSlide", SlideType.SHIFT,
                "effect.slideInFromBelow", SlideType.IN_FROM_BELOW,
                "effect.slideInFromAbove", SlideType.IN_FROM_ABOVE,
                "effect.slideOutDownwards", SlideType.OUT_DOWNWARDS,
                "effect.slideOutUpwards", SlideType.OUT_UPWARDS);

        commandNameToType.forEach((name, type) -> {
            commands.get(name).actionPerformed(event());
            assertEquals(Optional.of(type), editor.currentNote().orElseThrow().effects().slide(), name);
        });
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
            return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        };
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

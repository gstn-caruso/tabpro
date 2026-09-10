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
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandsTest {

    private final Editor editor = new Editor(Score.blank());
    private final List<String> asked = new ArrayList<>();
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    /**
     * Armar el comando de sound.soundFont ya le pregunta al puerto su estado real para arrancar
     * sincronizado (ver Commands.defineSoundCommands): esa pregunta no es lo que cada test quiere
     * comprobar, asi que el rastro de "a quien le preguntaron" arranca limpio para cada uno.
     */
    @BeforeEach
    void olvidaLoQuePreguntoElConstructor() {
        asked.clear();
    }

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
        List<String> repetidos = new ArrayList<>();
        commands.all().forEach((name, command) -> {
            KeyStroke shortcut = command.accelerator();
            if (shortcut == null) {
                return;
            }
            String previous = byShortcut.put(shortcut, name);
            if (previous != null) {
                repetidos.add("el atajo " + shortcut + " lo usan " + previous + " y " + name);
            }
        });

        assertFalse(byShortcut.isEmpty(), "ningun comando tiene atajo: no habria nada que verificar");
        assertEquals(List.of(), repetidos);
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

        commands.get("note.forceBeamBreak").actionPerformed(event());
        assertEquals(BeamBreak.FORCED, editor.currentBeat().effects().beamBreak());

        commands.get("note.preventBeamBreak").actionPerformed(event());
        assertEquals(BeamBreak.PREVENTED, editor.currentBeat().effects().beamBreak());

        commands.get("note.resetBeamBreak").actionPerformed(event());
        assertEquals(BeamBreak.AUTOMATIC, editor.currentBeat().effects().beamBreak());

        commands.get("note.stemUp").actionPerformed(event());
        assertEquals(StemOverride.UP, editor.currentBeat().effects().stemOverride());

        commands.get("note.stemDown").actionPerformed(event());
        assertEquals(StemOverride.DOWN, editor.currentBeat().effects().stemOverride());

        commands.get("note.stemAutomatic").actionPerformed(event());
        assertEquals(StemOverride.AUTOMATIC, editor.currentBeat().effects().stemOverride());
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

    /**
     * Manual, "Dynamic" (linea 1000): las ocho dinamicas, de ppp a fff, son botones que fijan la
     * dinamica de la nota bajo el cursor, el mismo camino que ya usa DynamicsDialog sin marcar
     * "aplicar a todo el acorde". Un acorde de dos cuerdas confirma que solo tocan esa nota.
     */
    @Test
    void everyDynamicCommandSetsTheNoteUnderTheCursorWithoutTouchingTheRestOfTheChord() {
        editor.setFret(5);
        int firstString = editor.currentNote().orElseThrow().string();
        editor.moveDown();
        editor.setFret(7);
        int secondString = editor.currentNote().orElseThrow().string();
        editor.moveUp();
        assertEquals(firstString, editor.currentNote().orElseThrow().string());

        for (Dynamic dynamic : Dynamic.values()) {
            commands.get("note.dynamic." + dynamic.name()).actionPerformed(event());
            assertEquals(dynamic, editor.currentNote().orElseThrow().effects().dynamic(), dynamic.name());
        }

        assertEquals(Dynamic.defaultDynamic(),
                editor.currentBeat().noteOn(secondString).orElseThrow().effects().dynamic());
    }

    @Test
    void aCommandThatNeedsAWindowAsksForIt() {
        commands.get("file.information").actionPerformed(event());

        assertEquals(List.of("scoreInformation"), asked);
    }

    /**
     * Manual, "Configure the Sound" (linea 1945): el volumen y la actividad del metronomo se
     * configuran en su propio dialogo, {@code MetronomeDialog}, ya implementado en
     * {@code MainFrame.Windows.metronomeSettings()} pero sin ningun comando que lo llame.
     */
    @Test
    void theMetronomeSettingsCommandAsksForItsWindow() {
        commands.get("sound.metronomeSettings").actionPerformed(event());

        assertEquals(List.of("metronomeSettings"), asked);
    }

    /**
     * Guitar Pro 5, manual pagina 14: junto al boton de digitacion de mano izquierda hay uno de
     * mano derecha, que abre el mismo dialogo con el foco en su campo.
     */
    @Test
    void theFingeringRightHandCommandAsksForItsDialog() {
        commands.get("note.fingeringRightHand").actionPerformed(event());

        assertEquals(List.of("fingeringRightHand"), asked);
    }

    @Test
    void editMarkerStartsDisabledWithoutAMarkerOnTheScore() {
        assertFalse(commands.get("marker.edit").isEnabled());
    }

    @Test
    void theEditMarkerCommandAsksForItsDialog() {
        editor.setMarker(Marker.named("Intro"));

        commands.get("marker.edit").actionPerformed(event());

        assertEquals(List.of("editMarker"), asked);
    }

    @Test
    void editMarkerBecomesEnabledAfterInsertingAMarkerOnTheCursor() {
        editor.setMarker(Marker.named("Intro"));

        assertTrue(commands.get("marker.edit").isEnabled());
    }

    @Test
    void editMarkerStaysEnabledPastTheMeasureWhereTheMarkerLives() {
        Editor localEditor = new Editor(scoreWithMeasures(3));
        Commands localCommands = new Commands(
                localEditor, record(Ports.Document.class), record(Ports.Dialogs.class),
                record(Ports.Playback.class), record(Ports.View.class));
        localEditor.moveTo(0, 0, 1);
        localEditor.setMarker(Marker.named("Intro"));

        localEditor.moveTo(2, 0, 1);

        assertTrue(localCommands.get("marker.edit").isEnabled());
    }

    private Score scoreWithMeasures(int count) {
        Score score = Score.blank();
        for (int i = 1; i < count; i++) {
            score = score.withMeasureInsertedInEveryTrackAt(i);
        }
        return score;
    }

    @Test
    void askingForACommandThatDoesNotExistIsAMistake() {
        assertThrows(IllegalArgumentException.class, () -> commands.get("no.existe"));
    }

    @Test
    void lasFlechasDePistaTienenIcono() {
        assertNotNull(commands.get("track.previous").icon());
        assertNotNull(commands.get("track.next").icon());
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

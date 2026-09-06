package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

/**
 * Transcripcion completa de la tabla "Keyboard Shortcuts" del manual (capitulo Reference,
 * paginas 79 a 81): cada atajo cotejado contra el comando exacto que deberia tener, no solo
 * contra "algun comando lo tiene". Un test que solo revisa que la tecla exista en algun lado del
 * catalogo no hubiera atrapado que "+" y "-" estuvieran invertidos; este si, porque afirma la
 * correspondencia comando-tecla uno a uno.
 *
 * <p>Cuatro atajos del manual no estan en esta tabla porque el catalogo de comandos no es donde
 * viven: "Home"/"End" (primer/ultimo beat del compas) y "*" (puntillo alternativo) se resuelven
 * como teclas crudas del lienzo en {@code KeyboardEditing} (ver su test), y "Page Up"/"Page Down"
 * (scroll) los resuelve Swing solo, en el JScrollPane que envuelve la partitura.
 */
class ManualKeyboardShortcutsTest {

    private final Commands commands = new Commands(
            new Editor(Score.blank()), record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    private record ManualShortcut(String command, String accelerator) {
    }

    @Test
    void everyShortcutOfTheManualMatchesItsExactCommand() {
        List<ManualShortcut> manual = List.of(
                // Edicion (manual pp. 79)
                new ManualShortcut("note.shorter", "PLUS"),
                new ManualShortcut("note.longer", "MINUS"),
                new ManualShortcut("note.dot", "PERIOD"),
                new ManualShortcut("note.rest", "R"),
                new ManualShortcut("note.triplet", "SLASH"),
                new ManualShortcut("note.tie", "L"),
                new ManualShortcut("note.tieBeat", "ctrl L"),
                new ManualShortcut("note.repeatToEndOfBar", "C"),
                new ManualShortcut("note.insertBeat", "INSERT"),
                new ManualShortcut("note.deleteNote", "DELETE"),
                new ManualShortcut("note.deleteBeat", "ctrl DELETE"),
                new ManualShortcut("note.toUpperString", "alt UP"),
                new ManualShortcut("note.toLowerString", "alt DOWN"),
                new ManualShortcut("note.up", "shift PLUS"),
                new ManualShortcut("note.down", "shift MINUS"),
                new ManualShortcut("edit.leadVoice", "ctrl 1"),
                new ManualShortcut("edit.bassVoice", "ctrl 2"),
                new ManualShortcut("view.grayInactiveVoice", "ctrl G"),
                new ManualShortcut("edit.selectAll", "ctrl A"),
                new ManualShortcut("edit.cut", "ctrl X"),
                new ManualShortcut("edit.copy", "ctrl C"),
                new ManualShortcut("edit.paste", "ctrl V"),
                new ManualShortcut("bar.insert", "ctrl INSERT"),
                new ManualShortcut("track.add", "ctrl shift INSERT"),
                new ManualShortcut("track.delete", "ctrl shift DELETE"),
                new ManualShortcut("track.moveUp", "ctrl alt UP"),
                new ManualShortcut("track.moveDown", "ctrl alt DOWN"),
                new ManualShortcut("marker.insert", "shift INSERT"),
                new ManualShortcut("file.information", "F5"),
                new ManualShortcut("track.properties", "F6"),
                new ManualShortcut("track.instrument", "F7"),
                new ManualShortcut("file.pageSetup", "F8"),
                new ManualShortcut("note.mixTableChange", "F10"),
                new ManualShortcut("edit.undo", "ctrl Z"),
                new ManualShortcut("edit.redo", "ctrl shift Z"),

                // Efectos (manual p. 80)
                new ManualShortcut("effect.hammer", "H"),
                new ManualShortcut("effect.legatoSlide", "S"),
                new ManualShortcut("effect.shiftSlide", "alt S"),
                new ManualShortcut("effect.bend", "B"),
                new ManualShortcut("note.chord", "A"),
                new ManualShortcut("effect.text", "T"),
                new ManualShortcut("effect.vibrato", "V"),
                new ManualShortcut("effect.fadeIn", "F"),
                new ManualShortcut("effect.deadNote", "X"),
                new ManualShortcut("effect.letRing", "I"),
                new ManualShortcut("effect.graceNote", "G"),
                new ManualShortcut("effect.ghostNote", "O"),
                new ManualShortcut("effect.palmMute", "P"),
                new ManualShortcut("effect.strokeUp", "ctrl U"),
                new ManualShortcut("effect.strokeDown", "ctrl D"),

                // Navegacion (manual pp. 80-81)
                new ManualShortcut("nav.nextNote", "ENTER"),
                new ManualShortcut("nav.previousBar", "ctrl LEFT"),
                new ManualShortcut("nav.nextBar", "ctrl RIGHT"),
                new ManualShortcut("nav.firstBar", "ctrl HOME"),
                new ManualShortcut("nav.lastBar", "ctrl END"),
                new ManualShortcut("track.next", "ctrl DOWN"),
                new ManualShortcut("track.previous", "ctrl UP"),
                new ManualShortcut("marker.previous", "shift TAB"),
                new ManualShortcut("marker.next", "ctrl TAB"),

                // Sonido (manual p. 81)
                new ManualShortcut("sound.play", "SPACE"),
                new ManualShortcut("sound.playFromStart", "ctrl SPACE"),
                new ManualShortcut("sound.soundFont", "F2"),
                new ManualShortcut("sound.loop", "F9"),

                // Varios (manual p. 81)
                new ManualShortcut("file.new", "ctrl N"),
                new ManualShortcut("file.open", "ctrl O"),
                new ManualShortcut("file.browse", "ctrl B"),
                new ManualShortcut("file.save", "ctrl S"),
                new ManualShortcut("file.print", "ctrl P"),
                new ManualShortcut("help.contents", "F1"),
                new ManualShortcut("options.preferences", "F12"),
                new ManualShortcut("tool.checkBarDurations", "F4"),
                new ManualShortcut("view.dynamicNotes", "F11"));

        assertAll(manual.stream().map(shortcut -> () -> assertEquals(
                KeyStroke.getKeyStroke(shortcut.accelerator()),
                commands.get(shortcut.command()).accelerator(),
                "el manual pide " + shortcut.accelerator() + " para " + shortcut.command())));
    }

    @SuppressWarnings("unchecked")
    private static <T> T record(Class<T> port) {
        return (T) Proxy.newProxyInstance(
                port.getClassLoader(), new Class<?>[] {port}, (proxy, method, args) -> null);
    }
}

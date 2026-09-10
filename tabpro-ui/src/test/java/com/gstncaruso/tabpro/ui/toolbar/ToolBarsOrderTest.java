package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.awt.Component;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JToolBar;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5, manual pagina 14: cada fila de la barra de herramientas trae los mismos grupos,
 * en el mismo orden y con los mismos separadores. Un token "|" marca un separador; el resto son
 * los nombres de los comandos del catalogo, en el orden exacto en que tienen que aparecer.
 */
class ToolBarsOrderTest {

    private static final String SEP = "|";

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final ToolBars toolBars = new ToolBars(commands);

    @Test
    void laFilaDeDocumentoYEdicionSigueElOrdenDeGuitarPro5() {
        assertOrder(toolBars.documentToolBar,
                "file.new", "file.open", "file.save", SEP,
                "file.information", SEP,
                "file.pageSetup", "file.print", SEP,
                "edit.undo", "edit.redo", SEP,
                "track.add", "track.properties", "track.moveUp", "track.moveDown", "track.delete",
                "tool.checkBarDurations", SEP,
                "bar.insert", "bar.delete", SEP,
                "edit.cut", "options.preferences", SEP,
                "view.multitrack", SEP,
                "view.page", "view.parchment", "view.verticalScreen", "view.horizontalScreen", SEP,
                "view.zoomOut", "view.resetZoom", "view.zoomIn", SEP,
                "view.fretboard", "view.keyboard", "view.mixTable", SEP,
                "edit.copy", "edit.paste");
    }

    @Test
    void laFilaDeEstructuraYSonidoSigueElOrdenDeGuitarPro5() {
        assertOrder(toolBars.structureToolBar,
                "bar.keySignature", "bar.timeSignature", "bar.tripletFeel", SEP,
                "bar.repeatOpen", "bar.repeatClose", SEP,
                "bar.doubleBar", SEP,
                "bar.alternateEndings", "bar.forceLineBreak", "bar.preventLineBreak", SEP,
                "marker.insert", "marker.previous", "marker.next", "marker.list", SEP,
                "sound.play", "nav.firstBar", "nav.lastBar", "sound.metronome", "sound.countDown",
                "sound.loop", "sound.soundFont", SEP,
                "tool.transpose", SEP,
                "nav.previousBar", "nav.nextBar", "tool.scales", "tool.tuner", SEP);
    }

    @Test
    void laFilaDeFigurasSigueElOrdenDeGuitarPro5() {
        assertOrder(toolBars.notationToolBar,
                "note.value.WHOLE", "note.value.HALF", "note.value.QUARTER", "note.value.EIGHTH",
                "note.value.SIXTEENTH", "note.value.THIRTY_SECOND", "note.value.SIXTY_FOURTH", SEP,
                "note.dot", "note.triplet", "note.tieBeat", SEP,
                "note.rest", SEP,
                "note.tie", "note.soundDuration", SEP,
                "bar.octave8va", "bar.octave8vb", "bar.octave15ma", "bar.octave15mb", SEP,
                "view.hideStandardNotation", "view.hideTablature", SEP,
                "note.preventBeamBreak", "note.forceBeamBreak", "note.resetBeamBreak", SEP,
                "note.stemUp", "note.stemDown", "note.stemAutomatic", SEP,
                "note.dynamic.PIANO_PIANISSIMO", "note.dynamic.PIANISSIMO", "note.dynamic.PIANO",
                "note.dynamic.MEZZO_PIANO", "note.dynamic.MEZZO_FORTE", "note.dynamic.FORTE",
                "note.dynamic.FORTISSIMO", "note.dynamic.FORTE_FORTISSIMO");
    }

    @Test
    void laFilaDeEfectosSigueElOrdenDeGuitarPro5() {
        assertOrder(toolBars.effectsToolBar,
                "effect.deadNote", "effect.graceNote", "effect.ghostNote", "effect.accent",
                "effect.heavyAccent", "effect.letRing", "effect.naturalHarmonic", "effect.artificialHarmonic", SEP,
                "effect.hammer", "effect.legatoSlide", "effect.shiftSlide", "effect.bend", "effect.tremoloBar",
                "effect.vibrato", "effect.wideVibrato", SEP,
                "effect.trill", "effect.tremoloPicking", "effect.palmMute", "effect.staccato", SEP,
                "effect.tapping", "effect.slapping", "effect.popping", SEP,
                "effect.fadeIn", "effect.pickstrokeDown", "effect.pickstrokeUp", SEP,
                "note.chord", "effect.text", "note.mixTableChange", "note.fingering", SEP,
                "effect.strokeUp", "effect.strokeDown");
    }

    private void assertOrder(JToolBar bar, String... tokens) {
        List<Object> expected = new ArrayList<>();
        for (String token : tokens) {
            expected.add(token.equals(SEP) ? SEP : commands.get(token));
        }
        assertEquals(expected, actualOrderOf(bar));
    }

    private List<Object> actualOrderOf(JToolBar bar) {
        List<Object> actual = new ArrayList<>();
        for (Component component : bar.getComponents()) {
            if (component instanceof JToolBar.Separator) {
                actual.add(SEP);
            } else if (component instanceof AbstractButton button) {
                actual.add(button.getAction());
            }
        }
        return actual;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

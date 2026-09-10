package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5, manual pagina 14, fila 1: el selector de pista, un boton numerado por pista mas
 * las flechas para ir a la anterior y la siguiente.
 */
class TrackSelectorTest {

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void unaSolaPistaMuestraUnBotonSeleccionado() {
        TrackSelector selector = new TrackSelector(editor, commands);

        assertEquals(1, selector.trackButtons().size());
        assertTrue(selector.trackButtons().get(0).isSelected());
    }

    @Test
    void variasPistasSoloLaActivaQuedaSeleccionada() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.addTrack(Track.standardGuitar("Guitarra 2"));
        editor.selectTrack(1);

        TrackSelector selector = new TrackSelector(editor, commands);

        assertEquals(3, selector.trackButtons().size());
        assertFalse(selector.trackButtons().get(0).isSelected());
        assertTrue(selector.trackButtons().get(1).isSelected());
        assertFalse(selector.trackButtons().get(2).isSelected());
    }

    @Test
    void conUnaSolaPistaLasDosFlechasArrancanDeshabilitadas() {
        TrackSelector selector = new TrackSelector(editor, commands);

        assertFalse(selector.previousButton().isEnabled());
        assertFalse(selector.nextButton().isEnabled());
    }

    @Test
    void enLaPrimeraPistaSoloLaFlechaSiguienteQuedaHabilitada() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.addTrack(Track.standardGuitar("Guitarra 2"));
        editor.selectTrack(0);

        TrackSelector selector = new TrackSelector(editor, commands);

        assertFalse(selector.previousButton().isEnabled());
        assertTrue(selector.nextButton().isEnabled());
    }

    @Test
    void enLaUltimaPistaSoloLaFlechaAnteriorQuedaHabilitada() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.addTrack(Track.standardGuitar("Guitarra 2"));
        editor.selectTrack(2);

        TrackSelector selector = new TrackSelector(editor, commands);

        assertTrue(selector.previousButton().isEnabled());
        assertFalse(selector.nextButton().isEnabled());
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

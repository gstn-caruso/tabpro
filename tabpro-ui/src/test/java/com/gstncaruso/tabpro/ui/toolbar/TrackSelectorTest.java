package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.AwaitEdt;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

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

    @Test
    void clickEnUnNumeroMueveElCursorAEsaPistaSinCambiarDeCompas() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.insertMeasure();
        editor.selectTrack(0);
        editor.moveToNextMeasure();
        TrackSelector selector = new TrackSelector(editor, commands);

        selector.trackButtons().get(1).doClick();

        assertEquals(1, editor.cursor().track());
        assertEquals(1, editor.cursor().measure());
    }

    @Test
    void clickEnLaFlechaSiguienteAvanzaALaProximaPista() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.selectTrack(0);
        TrackSelector selector = new TrackSelector(editor, commands);

        selector.nextButton().doClick();

        assertEquals(1, editor.cursor().track());
    }

    @Test
    void clickEnLaFlechaAnteriorRetrocedeALaPistaAnterior() {
        editor.addTrack(Track.standardBass("Bajo"));

        TrackSelector selector = new TrackSelector(editor, commands);
        selector.previousButton().doClick();

        assertEquals(0, editor.cursor().track());
    }

    @Test
    void unCambioDeCursorPorOtroCaminoActualizaElBotonSeleccionado() {
        editor.addTrack(Track.standardBass("Bajo"));
        editor.addTrack(Track.standardGuitar("Guitarra 2"));
        editor.selectTrack(0);
        TrackSelector selector = new TrackSelector(editor, commands);

        editor.selectTrack(2);
        AwaitEdt.flush();

        assertFalse(selector.trackButtons().get(0).isSelected());
        assertTrue(selector.trackButtons().get(2).isSelected());
    }

    @Test
    void agregarUnaPistaRehaceLosBotones() {
        TrackSelector selector = new TrackSelector(editor, commands);

        editor.addTrack(Track.standardBass("Bajo"));
        AwaitEdt.flush();

        assertEquals(2, selector.trackButtons().size());
        assertTrue(selector.trackButtons().get(1).isSelected());
    }

    @Test
    void borrarLaPistaActivaRehaceLosBotones() {
        editor.addTrack(Track.standardBass("Bajo"));
        TrackSelector selector = new TrackSelector(editor, commands);

        editor.removeCurrentTrack();
        AwaitEdt.flush();

        assertEquals(1, selector.trackButtons().size());
        assertTrue(selector.trackButtons().get(0).isSelected());
    }

    @Test
    void elBotonDeCadaPistaTieneSuNumeroYSuNombreComoNombreAccesible() {
        editor.addTrack(Track.standardBass("Bajo"));
        TrackSelector selector = new TrackSelector(editor, commands);

        assertEquals("Pista 1: Guitarra", selector.trackButtons().get(0).getAccessibleContext().getAccessibleName());
        assertEquals("Pista 2: Bajo", selector.trackButtons().get(1).getAccessibleContext().getAccessibleName());
        assertEquals("Pista 1: Guitarra", selector.trackButtons().get(0).getToolTipText());
    }

    @Test
    void reordenarPistasActualizaElNombreAccesibleDeCadaBoton() {
        editor.addTrack(Track.standardBass("Bajo"));
        TrackSelector selector = new TrackSelector(editor, commands);

        editor.moveCurrentTrack(-1);
        AwaitEdt.flush();

        assertEquals("Pista 1: Bajo", selector.trackButtons().get(0).getAccessibleContext().getAccessibleName());
        assertEquals("Pista 2: Guitarra", selector.trackButtons().get(1).getAccessibleContext().getAccessibleName());
    }

    @Test
    void cadaBotonDePistaMuestraUnIconoConSuNumero() {
        TrackSelector selector = new TrackSelector(editor, commands);

        assertNotNull(selector.trackButtons().get(0).getIcon());
    }

    @Test
    void lasFlechasMuestranSoloElIconoConNombreAccesibleYTooltip() {
        TrackSelector selector = new TrackSelector(editor, commands);

        assertNotNull(selector.previousButton().getIcon());
        assertNotNull(selector.nextButton().getIcon());
        assertNull(selector.previousButton().getText());
        assertNull(selector.nextButton().getText());
        assertEquals("Pista anterior", selector.previousButton().getAccessibleContext().getAccessibleName());
        assertEquals("Pista siguiente", selector.nextButton().getAccessibleContext().getAccessibleName());
        assertFalse(selector.previousButton().getToolTipText().isBlank());
        assertFalse(selector.nextButton().getToolTipText().isBlank());
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

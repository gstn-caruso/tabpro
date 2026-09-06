package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

/**
 * Ver > Menus y barras: cada fila de herramientas se puede esconder por separado, como pide
 * el manual, sin afectar a las otras.
 */
class ToolBarsTest {

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final ToolBars toolBars = new ToolBars(commands);

    @Test
    void lasTresFilasArrancanVisibles() {
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
    }

    @Test
    void escondeUnaFilaYLasOtrasDosQuedanVisibles() {
        toolBars.setStructureToolBarVisible(false);

        assertFalse(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
    }

    @Test
    void volverAMostrarlaLaTraeDeVuelta() {
        toolBars.setNotationToolBarVisible(false);
        toolBars.setNotationToolBarVisible(true);

        assertTrue(toolBars.isNotationToolBarVisible());
    }

    @Test
    void elEstadoDeCadaFilaEsIndependiente() {
        toolBars.setDocumentToolBarVisible(false);
        toolBars.setStructureToolBarVisible(false);

        assertFalse(toolBars.isDocumentToolBarVisible());
        assertFalse(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

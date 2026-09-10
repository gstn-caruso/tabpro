package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5, manual pagina 14, fila 1: el zoom es un combo editable con el porcentaje visible,
 * no tres botones sin texto.
 */
class ZoomSelectorTest {

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final FakeZoomHolder zoomHolder = new FakeZoomHolder();

    @Test
    void arrancaMostrandoElZoomReal() {
        ZoomSelector selector = new ZoomSelector(zoomHolder, commands);

        assertEquals("100%", String.valueOf(selector.getEditor().getItem()));
    }

    @Test
    void elDesplegableTraeLosValoresPredefinidosDelManual() {
        ZoomSelector selector = new ZoomSelector(zoomHolder, commands);

        List<String> items = new ArrayList<>();
        for (int index = 0; index < selector.getItemCount(); index++) {
            items.add(selector.getItemAt(index));
        }

        assertEquals(Zoom.presets().stream().map(percent -> percent + "%").toList(), items);
    }

    @Test
    void elegirUnValorDelDesplegableAplicaEseZoom() {
        ZoomSelector selector = new ZoomSelector(zoomHolder, commands);

        selector.setSelectedItem("150%");

        assertEquals(150, zoomHolder.zoom().percent());
    }

    @Test
    void tipearAlgoInvalidoIgnoraYVuelveAlValorReal() {
        ZoomSelector selector = new ZoomSelector(zoomHolder, commands);

        type(selector, "abc");

        assertEquals(100, zoomHolder.zoom().percent());
        assertEquals("100%", String.valueOf(selector.getEditor().getItem()));
    }

    @Test
    void tipearUnValorFueraDeRangoIgnoraYVuelveAlValorReal() {
        ZoomSelector selector = new ZoomSelector(zoomHolder, commands);

        type(selector, "500");

        assertEquals(100, zoomHolder.zoom().percent());
        assertEquals("100%", String.valueOf(selector.getEditor().getItem()));
    }

    private static void type(ZoomSelector selector, String text) {
        selector.getEditor().setItem(text);
        selector.actionPerformed(new java.awt.event.ActionEvent(selector, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
    }

    private static final class FakeZoomHolder implements com.gstncaruso.tabpro.ui.score.ZoomHolder {
        private Zoom zoom = Zoom.whole();
        private final List<Runnable> listeners = new ArrayList<>();

        @Override
        public Zoom zoom() {
            return zoom;
        }

        @Override
        public void setZoom(Zoom zoom) {
            this.zoom = zoom;
            listeners.forEach(Runnable::run);
        }

        @Override
        public void onZoomChange(Runnable listener) {
            listeners.add(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import org.junit.jupiter.api.Test;

class ToolBarsTest {

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final ToolBars toolBars = new ToolBars(editor, commands, new FakeZoomHolder());

    @Test
    void lasCuatroFilasArrancanVisibles() {
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void escondeUnaFilaYLasOtrasTresQuedanVisibles() {
        toolBars.setStructureToolBarVisible(false);

        assertFalse(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());
        assertTrue(toolBars.isEffectsToolBarVisible());
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
        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void laFilaDeEfectosSeEscondeYSeVuelveAMostrarSinAfectarALasOtras() {
        toolBars.setEffectsToolBarVisible(false);

        assertFalse(toolBars.isEffectsToolBarVisible());
        assertTrue(toolBars.isDocumentToolBarVisible());
        assertTrue(toolBars.isStructureToolBarVisible());
        assertTrue(toolBars.isNotationToolBarVisible());

        toolBars.setEffectsToolBarVisible(true);

        assertTrue(toolBars.isEffectsToolBarVisible());
    }

    @Test
    void ningunBotonQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(toolBars.component());
        AccessibilityAssertions.assertNoViolations(toolBars.effectsComponent());
    }

    @Test
    void ningunBotonDeNingunaDeLasCuatroFilasQuedaSinIcono() {
        for (Container row : new Container[] {
            toolBars.component(), toolBars.effectsComponent(),
        }) {
            for (AbstractButton button : buttonsOf(row)) {
                assertNotNull(button.getIcon(), button.getAccessibleContext().getAccessibleName() + " sin icono");
            }
        }
    }

    @Test
    void elBotonDelBancoDeSonidoArrancaYSeMantieneSincronizadoConElPuertoReal() {
        boolean[] activo = {true};
        List<String> llamados = new java.util.ArrayList<>();
        InvocationHandler handler = (proxy, method, args) -> {
            llamados.add(method.getName());
            if (method.getName().equals("toggleSoundFont")) {
                activo[0] = !activo[0];
                return null;
            }
            if (method.getName().equals("soundFontActive")) {
                return activo[0];
            }
            return null;
        };
        Ports.Playback playback = (Ports.Playback) Proxy.newProxyInstance(
                Ports.Playback.class.getClassLoader(), new Class<?>[] {Ports.Playback.class}, handler);
        ToolBars otraBarra = new ToolBars(editor, new Commands(
                editor, record(Ports.Document.class), record(Ports.Dialogs.class), playback, record(Ports.View.class)),
                new FakeZoomHolder());
        JToggleButton button = toggleButtonNamed(otraBarra.structureToolBar, "Banco de sonido");

        assertTrue(button.isSelected(), "tiene que arrancar mostrando que el banco esta prendido");

        button.doClick();
        assertFalse(button.isSelected());

        button.getAction().actionPerformed(null);
        assertTrue(button.isSelected(), "un disparo ajeno al boton (F2, el menu) tiene que sincronizarlo igual");

        assertEquals(2, llamados.stream().filter("toggleSoundFont"::equals).count());
    }

    private JToggleButton toggleButtonNamed(Container root, String name) {
        for (AbstractButton button : buttonsOf(root)) {
            if (button instanceof JToggleButton toggle && name.equals(button.getAccessibleContext().getAccessibleName())) {
                return toggle;
            }
        }
        throw new AssertionError("no encontre un boton conmutable llamado " + name);
    }

    private java.util.List<AbstractButton> buttonsOf(Container root) {
        java.util.List<AbstractButton> found = new java.util.ArrayList<>();
        for (Component child : root.getComponents()) {
            if (child instanceof AbstractButton button) {
                found.add(button);
            }
            if (child instanceof Container container && !(child instanceof javax.swing.JComboBox)) {
                found.addAll(buttonsOf(container));
            }
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }

    private static final class FakeZoomHolder implements ZoomHolder {
        @Override
        public Zoom zoom() {
            return Zoom.whole();
        }

        @Override
        public void setZoom(Zoom zoom) {
        }

        @Override
        public void onZoomChange(Runnable listener) {
        }
    }
}

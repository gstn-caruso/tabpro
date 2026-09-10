package com.gstncaruso.tabpro.ui.toolbar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import javax.swing.JButton;
import org.junit.jupiter.api.Test;

/**
 * Ver > Menus y barras: cada una de las cuatro filas de herramientas se puede esconder por
 * separado, como pide el manual, sin afectar a las otras.
 */
class ToolBarsTest {

    private final Editor editor = new Editor(Score.blank());
    private final Commands commands = new Commands(
            editor, record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));
    private final ToolBars toolBars = new ToolBars(commands);

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

    /**
     * La barra de efectos vive aparte de las otras tres (ver > MainFrame la ubica abajo de la
     * partitura), asi que su visibilidad se prueba con la misma mecanica pero sin mezclarla con
     * {@link #elEstadoDeCadaFilaEsIndependiente()}.
     */
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

    /**
     * Un boton de barra sin icono se ve como un rectangulo vacio: nadie lo nota en la lista de
     * comandos porque el nombre esta, pero en la barra real queda ciego. Este test recorre las
     * cuatro filas de verdad, no confia en que cada PR se acuerde de mirarlas a ojo.
     */
    @Test
    void ningunBotonDeNingunaDeLasCuatroFilasQuedaSinIcono() {
        for (Container row : new Container[] {
            toolBars.component(), toolBars.effectsComponent(),
        }) {
            for (JButton button : buttonsOf(row)) {
                assertNotNull(button.getIcon(), button.getAccessibleContext().getAccessibleName() + " sin icono");
            }
        }
    }

    private java.util.List<JButton> buttonsOf(Container root) {
        java.util.List<JButton> found = new java.util.ArrayList<>();
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button) {
                found.add(button);
            }
            if (child instanceof Container container) {
                found.addAll(buttonsOf(container));
            }
        }
        return found;
    }

    @SuppressWarnings("unchecked")
    private <T> T record(Class<T> port) {
        InvocationHandler handler = (proxy, method, args) -> null;
        return (T) Proxy.newProxyInstance(port.getClassLoader(), new Class<?>[] {port}, handler);
    }
}

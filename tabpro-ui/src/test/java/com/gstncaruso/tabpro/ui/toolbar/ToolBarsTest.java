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
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
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
            for (AbstractButton button : buttonsOf(row)) {
                assertNotNull(button.getIcon(), button.getAccessibleContext().getAccessibleName() + " sin icono");
            }
        }
    }

    /**
     * Manual, "Configure the Sound" (linea 1953): F2 prende o apaga el banco de sonido. Guitar
     * Pro 5 pone ahi mismo, en la fila de estructura y sonido, los dos iconos de RSE; tabpro pone
     * uno solo, y tiene que ser un conmutable de verdad -que cambie de estado al tocarlo y llegue
     * al mismo puerto que F2-, no un boton que solo dispara sin mostrar nada.
     */
    @Test
    void elBotonDelBancoDeSonidoEsUnConmutableQueLlegaAlPuertoReal() {
        java.util.List<String> llamados = new java.util.ArrayList<>();
        InvocationHandler contador = (proxy, method, args) -> {
            llamados.add(method.getName());
            return null;
        };
        Ports.Playback playback = (Ports.Playback) Proxy.newProxyInstance(
                Ports.Playback.class.getClassLoader(), new Class<?>[] {Ports.Playback.class}, contador);
        ToolBars otraBarra = new ToolBars(new Commands(
                editor, record(Ports.Document.class), record(Ports.Dialogs.class), playback, record(Ports.View.class)));
        JToggleButton button = toggleButtonNamed(otraBarra.structureToolBar, "Banco de sonido");

        assertFalse(button.isSelected());

        button.doClick();

        assertTrue(button.isSelected());
        assertEquals(List.of("toggleSoundFont"), llamados);

        button.doClick();

        assertFalse(button.isSelected());
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

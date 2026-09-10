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
    private final ToolBars toolBars = new ToolBars(editor, commands);

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
     * Manual, "Configure the Sound" (linea 1953): F2 prende o apaga el banco de sonido. Un
     * conmutable que no arranca mostrando el estado real, o que se desincroniza en cuanto el
     * cambio viene de otro lado (F2, el item del menu Sonido, que comparten este mismo comando),
     * es la "interfaz que miente" que la auditoria de tabpro persigue.
     */
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
                editor, record(Ports.Document.class), record(Ports.Dialogs.class), playback, record(Ports.View.class)));
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
            if (child instanceof Container container) {
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
}

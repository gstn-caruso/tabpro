package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.lang.reflect.Proxy;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

/**
 * JScrollPane y JSplitPane traen atajos propios de fabrica (scroll, navegar el split) que Swing
 * revisa ANTES de llegar al atajo de un menu: recorre los antepasados del componente enfocado
 * (WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) antes que la ventana entera
 * (WHEN_IN_FOCUSED_WINDOW, que es donde vive el ACCELERATOR_KEY de un JMenuItem). La partitura
 * vive adentro de un JScrollPane que a su vez vive adentro de un JSplitPane (con la mesa de
 * mezcla), asi que mientras esta enfocada -que es la situacion normal al editar- esos atajos de
 * fabrica se comen algunas teclas del catalogo antes de que el menu se entere:
 *
 * <ul>
 *   <li>[[Ctrl] Home] -&gt; nav.firstBar lo tapa el "scrollHome" del JScrollPane.
 *   <li>[[Ctrl] End] -&gt; nav.lastBar lo tapa el "scrollEnd" del JScrollPane.
 *   <li>[F6] -&gt; track.properties lo tapa el "toggleFocus" del JSplitPane.
 *   <li>[F8] -&gt; file.pageSetup lo tapa el "startResize" del JSplitPane.
 *   <li>[Ctrl] Tab -&gt; marker.next lo tapa el "focusOutForward" del JSplitPane.
 * </ul>
 */
class AcceleratorGuardTest {

    private final Commands commands = new Commands(
            new Editor(Score.blank()), record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void unJScrollPaneNuevoSeComeCtrlHomeYCtrlEnd() {
        InputMap inputMap = new JScrollPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertEquals("scrollHome", inputMap.get(commands.get("nav.firstBar").accelerator()));
        assertEquals("scrollEnd", inputMap.get(commands.get("nav.lastBar").accelerator()));
    }

    @Test
    void unJSplitPaneNuevoSeComeF6F8YCtrlTab() {
        InputMap inputMap = new JSplitPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertEquals("toggleFocus", inputMap.get(commands.get("track.properties").accelerator()));
        assertEquals("startResize", inputMap.get(commands.get("file.pageSetup").accelerator()));
        assertEquals("focusOutForward", inputMap.get(commands.get("marker.next").accelerator()));
    }

    @Test
    void leSacaAlJScrollPaneLasTeclasQueElCatalogoYaUsa() {
        JScrollPane scrollPane = new JScrollPane();
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke ctrlHome = commands.get("nav.firstBar").accelerator();
        KeyStroke ctrlEnd = commands.get("nav.lastBar").accelerator();

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        assertNotEquals("scrollHome", inputMap.get(ctrlHome));
        assertNotEquals("scrollEnd", inputMap.get(ctrlEnd));
    }

    @Test
    void leSacaAlJSplitPaneLasTeclasQueElCatalogoYaUsa() {
        JSplitPane split = new JSplitPane();
        InputMap inputMap = split.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke f6 = commands.get("track.properties").accelerator();
        KeyStroke f8 = commands.get("file.pageSetup").accelerator();
        KeyStroke ctrlTab = commands.get("marker.next").accelerator();

        AcceleratorGuard.letCommandsWin(commands, split);

        assertNotEquals("toggleFocus", inputMap.get(f6));
        assertNotEquals("startResize", inputMap.get(f8));
        assertNotEquals("focusOutForward", inputMap.get(ctrlTab));
    }

    /** La tecla bloqueada resuelve a una accion real que no hace nada, no queda un nombre colgado. */
    @Test
    void laTeclaBloqueadaResuelveAUnaAccionQueNoHaceNada() {
        JScrollPane scrollPane = new JScrollPane();
        KeyStroke ctrlHome = commands.get("nav.firstBar").accelerator();

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        Object nombreDeLaAccion = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .get(ctrlHome);
        Action accion = scrollPane.getActionMap().get(nombreDeLaAccion);
        assertNotNull(accion, "la tecla bloqueada tiene que resolver a una accion, no a null");
    }

    /** Una tecla que ningun comando usa (Page Up, por ejemplo) queda intacta. */
    @Test
    void noTocaLasTeclasQueNingunComandoUsa() {
        JScrollPane scrollPane = new JScrollPane();
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke pageUp = KeyStroke.getKeyStroke("PAGE_UP");

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        assertEquals("scrollUp", inputMap.get(pageUp));
    }

    /** No toca el InputMap por defecto que Swing comparte entre instancias: solo esta, puntual. */
    @Test
    void noTocaUnJScrollPaneQueNoSeLePaso() {
        AcceleratorGuard.letCommandsWin(commands, new JScrollPane());

        JScrollPane otro = new JScrollPane();
        assertEquals("scrollHome", otro.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .get(commands.get("nav.firstBar").accelerator()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T record(Class<T> port) {
        return (T) Proxy.newProxyInstance(
                port.getClassLoader(), new Class<?>[] {port}, (proxy, method, args) -> null);
    }
}

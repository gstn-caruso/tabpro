package com.gstncaruso.tabpro.ui.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import java.awt.event.KeyEvent;
import java.lang.reflect.Proxy;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class AcceleratorGuardTest {

    private final Commands commands = new Commands(
            new Editor(Score.blank()), record(Ports.Document.class), record(Ports.Dialogs.class),
            record(Ports.Playback.class), record(Ports.View.class));

    @Test
    void aNewJScrollPaneEatsCtrlHomeAndCtrlEnd() {
        InputMap inputMap = new JScrollPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertEquals("scrollHome", inputMap.get(commands.get("nav.firstBar").accelerator()));
        assertEquals("scrollEnd", inputMap.get(commands.get("nav.lastBar").accelerator()));
    }

    @Test
    void aNewJSplitPaneEatsF6F8AndCtrlTab() {
        InputMap inputMap = new JSplitPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        assertEquals("toggleFocus", inputMap.get(commands.get("track.properties").accelerator()));
        assertEquals("startResize", inputMap.get(commands.get("file.pageSetup").accelerator()));
        assertEquals("focusOutForward", inputMap.get(commands.get("marker.next").accelerator()));
    }

    @Test
    void stripsFromTheJScrollPaneTheKeysTheCatalogAlreadyUses() {
        JScrollPane scrollPane = new JScrollPane();
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke ctrlHome = commands.get("nav.firstBar").accelerator();
        KeyStroke ctrlEnd = commands.get("nav.lastBar").accelerator();

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        assertNotEquals("scrollHome", inputMap.get(ctrlHome));
        assertNotEquals("scrollEnd", inputMap.get(ctrlEnd));
    }

    @Test
    void stripsFromTheJSplitPaneTheKeysTheCatalogAlreadyUses() {
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

    @Test
    void theBlockedKeyIsNoLongerHandledByTheAncestor() {
        ExposedJScrollPane scrollPane = new ExposedJScrollPane();
        KeyStroke ctrlHome = commands.get("nav.firstBar").accelerator();

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        KeyEvent event = new KeyEvent(scrollPane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                ctrlHome.getModifiers(), ctrlHome.getKeyCode(), KeyEvent.CHAR_UNDEFINED);
        assertFalse(
                scrollPane.processKeyBinding(ctrlHome, event, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true),
                "la tecla bloqueada no puede quedar atendida por el ancestro: el atajo real tiene que poder seguir subiendo");
    }

    private static final class ExposedJScrollPane extends JScrollPane {
        @Override
        public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
            return super.processKeyBinding(ks, e, condition, pressed);
        }
    }

    @Test
    void doesNotTouchKeysThatNoCommandUses() {
        JScrollPane scrollPane = new JScrollPane();
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke pageUp = KeyStroke.getKeyStroke("PAGE_UP");

        AcceleratorGuard.letCommandsWin(commands, scrollPane);

        assertEquals("scrollUp", inputMap.get(pageUp));
    }

    @Test
    void doesNotTouchAJScrollPaneThatWasNotPassedIn() {
        AcceleratorGuard.letCommandsWin(commands, new JScrollPane());

        JScrollPane another = new JScrollPane();
        assertEquals("scrollHome", another.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .get(commands.get("nav.firstBar").accelerator()));
    }

    @Test
    void neutralizesF10OnTheMenuBarSoMixTableChangeWins() {
        ExposedJMenuBar menuBar = new ExposedJMenuBar();
        KeyStroke f10 = commands.get("note.mixTableChange").accelerator();

        AcceleratorGuard.letCommandsWinOverTheMenuBar(commands, menuBar);

        KeyEvent event = new KeyEvent(menuBar, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                f10.getModifiers(), f10.getKeyCode(), KeyEvent.CHAR_UNDEFINED);
        assertFalse(
                menuBar.processKeyBinding(f10, event, JComponent.WHEN_IN_FOCUSED_WINDOW, true),
                "F10 no puede quedar atendido por la barra de menus: el atajo real de Cambio de "
                        + "parametros tiene que poder seguir subiendo");
    }

    private static final class ExposedJMenuBar extends JMenuBar {
        @Override
        public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
            return super.processKeyBinding(ks, e, condition, pressed);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T record(Class<T> port) {
        return (T) Proxy.newProxyInstance(
                port.getClassLoader(), new Class<?>[] {port},
                (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null);
    }
}

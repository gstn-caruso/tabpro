package com.gstncaruso.tabpro.app.audit;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Lo que necesita cualquier test de la auditoria de uso real para llegar a la ventana principal
 * real por el camino del usuario: armar un MainFrame de verdad (con un Editor real, sin MIDI ni
 * disco), encontrar sus componentes reales (menu, canvas, dialogos) y despachar teclas reales en
 * vez de invocar un Action a mano.
 */
final class AuditSupport {

    private AuditSupport() {
    }

    /**
     * El atajo de un menu se resuelve con WHEN_IN_FOCUSED_WINDOW: Swing solo lo registra en su
     * KeyboardManager global cuando el componente esta "showing" de verdad, y eso exige
     * setVisible(true) -pack() solo no alcanza-. Por eso esta ventana se muestra de verdad (hay
     * DISPLAY real) y el test que la pide tiene que cerrarla con dispose() al terminar.
     */
    static MainFrame newFrame(Editor editor) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(editor, new NoScoreFiles(), new SilentPlayer());
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    static void dispose(MainFrame frame) throws Exception {
        SwingUtilities.invokeAndWait(frame::dispose);
    }

    static Editor blankEditor() {
        return new Editor(Score.blank());
    }

    /** Una partitura con varios compases, para que navegar entre ellos tenga algo que mostrar. */
    static Editor editorWithMeasures(int extraMeasures) {
        Editor editor = blankEditor();
        for (int i = 0; i < extraMeasures; i++) {
            editor.insertMeasure();
        }
        editor.moveToFirstMeasure();
        return editor;
    }

    /** Deja una nota real (no un silencio) parada en el cursor, para los efectos que la piden. */
    static Editor editorWithANote() {
        Editor editor = blankEditor();
        editor.setFret(3);
        return editor;
    }

    @SuppressWarnings("unchecked")
    static <T extends Component> T findComponent(Container root, Class<T> type) {
        if (type.isInstance(root)) {
            return (T) root;
        }
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) {
                return (T) child;
            }
            if (child instanceof Container container) {
                T found = findComponent(container, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    static JButton findButton(Container root, String text) {
        if (root instanceof JButton button && text.equals(button.getText())) {
            return button;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (child instanceof Container container) {
                JButton found = findButton(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** El JMenuItem real cuya Action tiene esa etiqueta exacta, buscando en toda la barra. */
    static JMenuItem findMenuItem(JMenuBar menuBar, String label) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenuItem found = findMenuItem(menuBar.getMenu(i), label);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static JMenuItem findMenuItem(JMenu menu, String label) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            if (label.equals(item.getText())) {
                return item;
            }
            if (item instanceof JMenu submenu) {
                JMenuItem found = findMenuItem(submenu, label);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** El JButton real de una barra de herramientas cuya Action tiene ese nombre exacto. */
    static JButton findButtonByActionName(Container root, String label) {
        if (root instanceof JButton button
                && button.getAction() != null
                && label.equals(button.getAction().getValue(Action.NAME))) {
            return button;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button
                    && button.getAction() != null
                    && label.equals(button.getAction().getValue(Action.NAME))) {
                return button;
            }
            if (child instanceof Container container) {
                JButton found = findButtonByActionName(container, label);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Una foto del modelo observable: si dos de estas son iguales, el modelo no se movio. */
    private record ModelSnapshot(Score score, Object cursor) {
    }

    private static ModelSnapshot snapshot(Editor editor) {
        return new ModelSnapshot(editor.score(), editor.cursor());
    }

    /**
     * El comando del catalogo, ejercitado por su JMenuItem real y por el KeyEvent real de su
     * acelerador, tiene que mover el modelo exactamente igual por los dos caminos -y tiene que
     * moverlo de verdad, no ser un cambio que ya estaba en el estado inicial-. Arma una ventana
     * nueva por cada camino para que uno no contamine al otro.
     */
    static void assertAcceleratorMatchesMenu(String menuItemLabel, Supplier<Editor> setup) throws Exception {
        Editor viaMenu = setup.get();
        MainFrame menuFrame = newFrame(viaMenu);
        try {
            JMenuItem item = findMenuItem(menuFrame.getJMenuBar(), menuItemLabel);
            assertNotNull(item, "no encontre en el menu real el item \"" + menuItemLabel + "\"");
            KeyStroke accelerator = item.getAccelerator();
            assertNotNull(accelerator, "\"" + menuItemLabel + "\" no tiene acelerador en el menu real");

            ModelSnapshot before = snapshot(viaMenu);
            SwingUtilities.invokeAndWait(item::doClick);
            ModelSnapshot afterMenu = snapshot(viaMenu);
            assertNotEquals(before, afterMenu,
                    "\"" + menuItemLabel + "\" por menu no cambio nada, no sirve de referencia");

            Editor viaKey = setup.get();
            MainFrame keyFrame = newFrame(viaKey);
            try {
                var canvas = findComponent(keyFrame.getContentPane(), com.gstncaruso.tabpro.ui.score.ScoreCanvas.class);
                assertNotNull(canvas, "no encontre el ScoreCanvas real en la ventana");
                pressKey(canvas, accelerator);
                ModelSnapshot afterKey = snapshot(viaKey);
                assertEquals(afterMenu, afterKey,
                        "el atajo de \"" + menuItemLabel + "\" no produjo el mismo efecto que su menu");
            } finally {
                dispose(keyFrame);
            }
        } finally {
            dispose(menuFrame);
        }
    }

    /**
     * HALLAZGO documentado como test verde: el comando funciona por menu (se comprueba antes de
     * afirmar nada), pero su atajo -despachado de verdad sobre el lienzo- no mueve el modelo un
     * pelo. Sirve para los casos en que AcceleratorGuard neutraliza al ancestro que le robaba la
     * tecla poniendole una accion que no hace nada, en vez de sacarle la tecla del mapa: el
     * barrido de Swing encuentra esa accion vacia, la da por atendida y nunca llega al
     * WHEN_IN_FOCUSED_WINDOW donde vive el atajo real.
     */
    static void assertAcceleratorIsSwallowedBeforeReachingTheMenu(String menuItemLabel, Supplier<Editor> setup)
            throws Exception {
        Editor viaMenu = setup.get();
        MainFrame menuFrame = newFrame(viaMenu);
        try {
            JMenuItem item = findMenuItem(menuFrame.getJMenuBar(), menuItemLabel);
            assertNotNull(item, "no encontre en el menu real el item \"" + menuItemLabel + "\"");
            KeyStroke accelerator = item.getAccelerator();
            assertNotNull(accelerator, "\"" + menuItemLabel + "\" no tiene acelerador en el menu real");

            ModelSnapshot before = snapshot(viaMenu);
            SwingUtilities.invokeAndWait(item::doClick);
            ModelSnapshot afterMenu = snapshot(viaMenu);
            assertNotEquals(before, afterMenu,
                    "\"" + menuItemLabel + "\" por menu no cambio nada: el comando en si no sirve de referencia");

            Editor viaKey = setup.get();
            MainFrame keyFrame = newFrame(viaKey);
            try {
                var canvas = findComponent(keyFrame.getContentPane(), com.gstncaruso.tabpro.ui.score.ScoreCanvas.class);
                assertNotNull(canvas, "no encontre el ScoreCanvas real en la ventana");
                ModelSnapshot beforeKey = snapshot(viaKey);
                pressKey(canvas, accelerator);
                ModelSnapshot afterKey = snapshot(viaKey);

                assertEquals(beforeKey, afterKey,
                        "HALLAZGO: se esperaba que \"" + menuItemLabel + "\" ya no quedara mudo por teclado; "
                                + "si este assert empieza a fallar, revisar si AcceleratorGuard se arreglo");
            } finally {
                dispose(keyFrame);
            }
        } finally {
            dispose(menuFrame);
        }
    }

    /** Todos los JMenuItem de la barra, para las auditorias que recorren el menu entero. */
    static List<JMenuItem> allMenuItems(JMenuBar menuBar) {
        List<JMenuItem> items = new ArrayList<>();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            collectMenuItems(menuBar.getMenu(i), items);
        }
        return items;
    }

    private static void collectMenuItems(JMenu menu, List<JMenuItem> items) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            if (item instanceof JMenu submenu) {
                collectMenuItems(submenu, items);
            } else {
                items.add(item);
            }
        }
    }

    /**
     * Despacha, sobre ese componente, la misma tecla que Swing recibiria del sistema operativo:
     * un KEY_PRESSED con el codigo y los modificadores del acelerador. Nada de invocar la Action.
     */
    static void pressKey(Component target, KeyStroke accelerator) throws Exception {
        SwingUtilities.invokeAndWait(() -> target.dispatchEvent(new KeyEvent(
                target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                accelerator.getModifiers(), accelerator.getKeyCode(), KeyEvent.CHAR_UNDEFINED)));
    }

    /** Los digitos de traste y el "*": no tienen KeyStroke, los resuelve un KeyListener crudo. */
    static void typeChar(Component target, char c) throws Exception {
        SwingUtilities.invokeAndWait(() -> target.dispatchEvent(new KeyEvent(
                target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, c)));
    }

    /**
     * Dispara la accion que abre un dialogo modal real y lo maneja apenas se abre, sin Robot:
     * un AWTEventListener global agarra el WINDOW_OPENED del JDialog -que Swing entrega dentro
     * del mismo bucle anidado que bloquea a setVisible(true)-, ahi mismo el test toca los
     * controles reales del dialogo y lo cierra, y entonces el hilo que disparo la accion sigue.
     */
    static void withDialog(Runnable trigger, Consumer<JDialog> onOpen) throws Exception {
        CountDownLatch opened = new CountDownLatch(1);
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event.getSource() instanceof JDialog dialog) {
                onOpen.accept(dialog);
                opened.countDown();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            SwingUtilities.invokeAndWait(trigger::run);
            if (!opened.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("el dialogo nunca abrio una ventana (WINDOW_OPENED)");
            }
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    /**
     * Despacha una tecla sin bloquear el hilo del test y dice si eso abrio una ventana real
     * dentro del tiempo dado: para los atajos que deberian abrir un dialogo modal, sin arriesgar
     * que el test quede colgado si el dialogo de verdad aparece y nadie lo cierra.
     */
    static boolean dispatchKeyAndDetectDialog(Component target, KeyStroke keyStroke, long timeoutMillis)
            throws Exception {
        CountDownLatch opened = new CountDownLatch(1);
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event.getSource() instanceof JDialog dialog) {
                opened.countDown();
                dialog.dispose();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            SwingUtilities.invokeLater(() -> target.dispatchEvent(new KeyEvent(
                    target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    keyStroke.getModifiers(), keyStroke.getKeyCode(), KeyEvent.CHAR_UNDEFINED)));
            return opened.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    private static final class NoScoreFiles implements ScoreFiles {
        @Override
        public Score load(Path path) {
            throw new UnsupportedOperationException("la auditoria no toca el disco");
        }

        @Override
        public void save(Score score, Path path) {
            throw new UnsupportedOperationException("la auditoria no toca el disco");
        }
    }

    private static final class SilentPlayer implements Player {
        private volatile boolean playing;

        @Override
        public void play(Timeline timeline, PlaybackListener listener) {
            playing = true;
            listener.playbackFinished();
            playing = false;
        }

        @Override
        public void playNote(Pitch pitch, int program) {
        }

        @Override
        public void stop() {
            playing = false;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }
    }
}

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

    /**
     * Swing tiene un solo EDT por maquina virtual, y la suite corre en una sola maquina virtual
     * con las clases en paralelo (ver pom.xml raiz): sin este lock, un dialogo modal real que
     * abre un test puede terminar cerrado por el AWTEventListener global de otro test que corre
     * al mismo tiempo -Toolkit.addAWTEventListener no distingue de que test es cada ventana-, y
     * la suite queda colgada esperando un WINDOW_OPENED que ya paso. Cada clase de la auditoria
     * se anota con {@code @ResourceLock(AuditSupport.SWING_LOCK)} para que JUnit las serialice
     * entre si, aunque sigan en paralelo con el resto de la suite (que no toca Swing de verdad).
     */
    static final String SWING_LOCK = "tabpro-audit-swing";

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

    /** Como newFrame, pero con un Player que el test puede inspeccionar despues. */
    static MainFrame newFrame(Editor editor, Player player) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(editor, new NoScoreFiles(), player);
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    /** Como newFrame, pero con unos Devices que el test puede inspeccionar despues. */
    static MainFrame newFrame(Editor editor, com.gstncaruso.tabpro.ui.actions.Ports.Devices devices) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, new NoScoreFiles(), new SilentPlayer(),
                    com.gstncaruso.tabpro.ui.theme.ThemeSwitch.NONE, devices,
                    com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE);
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    /**
     * Como newFrame, pero con un ScoreFiles y un ScoreExchange reales: los que necesita
     * cualquier test que ejercite Abrir/Guardar/Importar/Exportar por el JFileChooser real,
     * en vez de los dobles NoScoreFiles/ScoreExchange.NONE que tiran UnsupportedOperationException.
     */
    static MainFrame newFrame(
            Editor editor, ScoreFiles files, com.gstncaruso.tabpro.core.files.ScoreExchange exchange) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, files, new SilentPlayer(),
                    com.gstncaruso.tabpro.ui.theme.ThemeSwitch.NONE, com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                    exchange, com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE);
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    /** Como newFrame, pero con un ThemeSwitch que el test puede inspeccionar despues. */
    static MainFrame newFrame(Editor editor, com.gstncaruso.tabpro.ui.theme.ThemeSwitch themes) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, new NoScoreFiles(), new SilentPlayer(),
                    themes, com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                    com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE);
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    /** Un Devices sin MIDI real, salvo el banco de sonido: registra si lo prendieron/apagaron. */
    static final class RecordingDevices implements com.gstncaruso.tabpro.ui.actions.Ports.Devices {
        private boolean soundFontActive;
        private int toggleCount;

        @Override
        public java.util.List<String> outputs() {
            return java.util.List.of();
        }

        @Override
        public String output(int port) {
            return "";
        }

        @Override
        public void useOutput(int port, String name) {
        }

        @Override
        public void playTestNote(String deviceName) {
        }

        @Override
        public java.util.List<String> inputs() {
            return java.util.List.of();
        }

        @Override
        public String input() {
            return "";
        }

        @Override
        public void useInput(String name) {
        }

        @Override
        public boolean isCapturing() {
            return false;
        }

        @Override
        public void startCapture(com.gstncaruso.tabpro.ui.actions.Ports.CapturedNote listener) {
        }

        @Override
        public void stopCapture() {
        }

        @Override
        public int sensitivityMillis() {
            return 0;
        }

        @Override
        public void useSensitivityMillis(int millis) {
        }

        @Override
        public boolean limitsPitchVariation(int port) {
            return false;
        }

        @Override
        public void useLimitPitchVariation(int port, boolean limit) {
        }

        @Override
        public java.util.Optional<String> soundFontFile() {
            return java.util.Optional.empty();
        }

        @Override
        public void chooseSoundFontFile(java.util.Optional<String> path) {
        }

        @Override
        public boolean soundFontActive() {
            return soundFontActive;
        }

        @Override
        public void toggleSoundFont() {
            toggleCount++;
            soundFontActive = !soundFontActive;
        }

        @Override
        public String soundFontStatus() {
            return "";
        }

        int toggleCount() {
            return toggleCount;
        }
    }

    /**
     * No termina la reproduccion sola -al reves del Player de {@link #newFrame(Editor)}-: se
     * queda "sonando" hasta que el propio Transport la frene, para poder mirar desde afuera si
     * Espacio de verdad la arranco y la freno.
     */
    static final class RecordingPlayer implements Player {
        private volatile boolean playCalled;
        private volatile boolean stopCalled;
        private volatile boolean playing;
        private volatile Timeline lastTimeline;

        @Override
        public void play(Timeline timeline, PlaybackListener listener) {
            playCalled = true;
            lastTimeline = timeline;
            playing = true;
        }

        @Override
        public void playNote(Pitch pitch, int program) {
        }

        @Override
        public void stop() {
            stopCalled = true;
            playing = false;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        boolean playCalled() {
            return playCalled;
        }

        boolean stopCalled() {
            return stopCalled;
        }

        Timeline lastTimeline() {
            return lastTimeline;
        }
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

    /** El contenido real de una solapa de un JTabbedPane, por su titulo. */
    static Container tabContent(Container root, String tabTitle) {
        javax.swing.JTabbedPane tabs = findComponent(root, javax.swing.JTabbedPane.class);
        if (tabs == null) {
            return null;
        }
        int index = tabs.indexOfTab(tabTitle);
        return index < 0 ? null : (Container) tabs.getComponentAt(index);
    }

    /** Todos los componentes de ese tipo, en el orden en que aparecen en el arbol real. */
    static <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collectComponents(root, type, found);
        return found;
    }

    /**
     * OJO: no agregar el hijo aca Y llamarse de nuevo sobre el si es Container -todo componente
     * Swing lo es-, porque el chequeo de "es el tipo buscado" ya esta arriba, al entrar a la
     * llamada recursiva: hacer las dos cosas cuenta cada componente dos veces.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Component> void collectComponents(Container root, Class<T> type, List<T> into) {
        if (type.isInstance(root)) {
            into.add((T) root);
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                collectComponents(container, type, into);
            }
        }
    }

    static javax.swing.JRadioButton findRadioButton(Container root, String text) {
        if (root instanceof javax.swing.JRadioButton button && text.equals(button.getText())) {
            return button;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof javax.swing.JRadioButton button && text.equals(button.getText())) {
                return button;
            }
            if (child instanceof Container container) {
                javax.swing.JRadioButton found = findRadioButton(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    static javax.swing.JCheckBox findCheckBox(Container root, String text) {
        if (root instanceof javax.swing.JCheckBox box && text.equals(box.getText())) {
            return box;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof javax.swing.JCheckBox box && text.equals(box.getText())) {
                return box;
            }
            if (child instanceof Container container) {
                javax.swing.JCheckBox found = findCheckBox(container, text);
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

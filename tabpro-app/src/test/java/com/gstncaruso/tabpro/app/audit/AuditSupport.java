package com.gstncaruso.tabpro.app.audit;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.print.Printing;
import com.gstncaruso.tabpro.ui.print.SystemPrinting;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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

final class AuditSupport {

    /**
     * Swing has a single EDT per JVM, and this suite runs in one JVM with its classes in
     * parallel (see the root pom.xml): without this lock, a real modal dialog opened by one test
     * can end up closed by another test's global AWTEventListener running at the same time
     * -Toolkit.addAWTEventListener cannot tell which test owns which window-, leaving the suite
     * hanging on a WINDOW_OPENED that already happened. Every audit class is annotated with
     * {@code @ResourceLock(AuditSupport.SWING_LOCK)} so JUnit serializes them against each other,
     * while still running in parallel with the rest of the suite (which never touches real Swing).
     */
    static final String SWING_LOCK = "tabpro-audit-swing";

    private AuditSupport() {
    }

    /**
     * A menu accelerator resolves through WHEN_IN_FOCUSED_WINDOW: Swing only registers it in its
     * global KeyboardManager once the component is really "showing", which requires
     * setVisible(true) -pack() alone is not enough-. That is why this window is shown for real
     * (a real DISPLAY is needed), and callers must dispose() it when done.
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

    static MainFrame newFrame(Editor editor, com.gstncaruso.tabpro.ui.actions.Ports.Devices devices) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, new NoScoreFiles(), new SilentPlayer(),
                    com.gstncaruso.tabpro.ui.theme.ThemeSwitch.NONE, devices,
                    com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE, new SystemPrinting());
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    static MainFrame newFrame(Editor editor, Printing printing) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, new NoScoreFiles(), new SilentPlayer(),
                    com.gstncaruso.tabpro.ui.theme.ThemeSwitch.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                    com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE, printing);
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    static MainFrame newFrame(
            Editor editor, ScoreFiles files, com.gstncaruso.tabpro.core.files.ScoreExchange exchange) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, files, new SilentPlayer(),
                    com.gstncaruso.tabpro.ui.theme.ThemeSwitch.NONE, com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                    exchange, com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE, new SystemPrinting());
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

    static MainFrame newFrame(Editor editor, com.gstncaruso.tabpro.ui.theme.ThemeSwitch themes) throws Exception {
        MainFrame[] built = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(
                    editor, new NoScoreFiles(), new SilentPlayer(),
                    themes, com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                    com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                    com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE, new SystemPrinting());
            frame.pack();
            frame.setVisible(true);
            built[0] = frame;
        });
        return built[0];
    }

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
        public Ports.SoundBankStatus soundFontStatus() {
            return Ports.SoundBankStatus.none();
        }

        int toggleCount() {
            return toggleCount;
        }
    }

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

    static final class RecordingPrinting implements Printing {
        private String jobName;
        private java.awt.print.Printable printable;
        private boolean printCalled;

        @Override
        public void setJobName(String name) {
            this.jobName = name;
        }

        @Override
        public void setPrintable(java.awt.print.Printable printable, java.awt.print.PageFormat format) {
            this.printable = printable;
        }

        @Override
        public boolean printDialog() {
            return true;
        }

        @Override
        public void print() {
            printCalled = true;
        }

        @Override
        public java.awt.print.PageFormat defaultPage() {
            return new java.awt.print.PageFormat();
        }

        @Override
        public java.awt.print.PageFormat pageDialog(java.awt.print.PageFormat page) {
            return page;
        }

        String jobName() {
            return jobName;
        }

        java.awt.print.Printable printable() {
            return printable;
        }

        boolean printCalled() {
            return printCalled;
        }
    }

    static Editor blankEditor() {
        return new Editor(Score.blank());
    }

    /**
     * The aggregated run in tabpro-tests (see the root pom.xml) copies compiled classes from
     * every module, but leaves each module's src/test/resources in place, one level above the
     * working directory surefire actually runs from. This resolves a fixture from another
     * module's resources on disk, for tests that need a real file for a real JFileChooser.
     */
    static Path repoFile(String relativeFromRepoRoot) {
        return Path.of(System.getProperty("user.dir"), "..", relativeFromRepoRoot).normalize();
    }

    static Editor editorWithMeasures(int extraMeasures) {
        Editor editor = blankEditor();
        for (int i = 0; i < extraMeasures; i++) {
            editor.insertMeasure();
        }
        editor.moveToFirstMeasure();
        return editor;
    }

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

    static JButton findButtonByAccessibleName(Container root, String name) {
        if (root instanceof JButton button && name.equals(button.getAccessibleContext().getAccessibleName())) {
            return button;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof JButton button
                    && name.equals(button.getAccessibleContext().getAccessibleName())) {
                return button;
            }
            if (child instanceof Container container) {
                JButton found = findButtonByAccessibleName(container, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    static JMenuItem findMenuItem(JMenuBar menuBar, String label) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenuItem found = findMenuItem(menuBar.getMenu(i), label);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    static JMenuItem findMenuItem(JMenu menu, String label) {
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

    static Container tabContent(Container root, String tabTitle) {
        javax.swing.JTabbedPane tabs = findComponent(root, javax.swing.JTabbedPane.class);
        if (tabs == null) {
            return null;
        }
        int index = tabs.indexOfTab(tabTitle);
        return index < 0 ? null : (Container) tabs.getComponentAt(index);
    }

    static <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collectComponents(root, type, found);
        return found;
    }

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

    static javax.swing.JToggleButton findToggleButtonByActionName(Container root, String label) {
        if (root instanceof javax.swing.JToggleButton button
                && button.getAction() != null
                && label.equals(button.getAction().getValue(Action.NAME))) {
            return button;
        }
        for (Component child : root.getComponents()) {
            if (child instanceof javax.swing.JToggleButton button
                    && button.getAction() != null
                    && label.equals(button.getAction().getValue(Action.NAME))) {
                return button;
            }
            if (child instanceof Container container) {
                javax.swing.JToggleButton found = findToggleButtonByActionName(container, label);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private record ModelSnapshot(Score score, Object cursor) {
    }

    private static ModelSnapshot snapshot(Editor editor) {
        return new ModelSnapshot(editor.score(), editor.cursor());
    }

    static void assertAcceleratorMatchesMenu(String menuItemLabel, Supplier<Editor> setup) throws Exception {
        Editor viaMenu = setup.get();
        MainFrame menuFrame = newFrame(viaMenu);
        try {
            JMenuItem item = findMenuItem(menuFrame.getJMenuBar(), menuItemLabel);
            assertNotNull(item, "could not find the item \"" + menuItemLabel + "\" in the real menu");
            KeyStroke accelerator = item.getAccelerator();
            assertNotNull(accelerator, "\"" + menuItemLabel + "\" has no accelerator in the real menu");

            ModelSnapshot before = snapshot(viaMenu);
            SwingUtilities.invokeAndWait(item::doClick);
            ModelSnapshot afterMenu = snapshot(viaMenu);
            assertNotEquals(before, afterMenu,
                    "\"" + menuItemLabel + "\" via menu changed nothing, it is not a useful reference");

            Editor viaKey = setup.get();
            MainFrame keyFrame = newFrame(viaKey);
            try {
                var canvas = findComponent(keyFrame.getContentPane(), com.gstncaruso.tabpro.ui.score.ScoreCanvas.class);
                assertNotNull(canvas, "could not find the real ScoreCanvas in the window");
                pressKey(canvas, accelerator);
                ModelSnapshot afterKey = snapshot(viaKey);
                assertEquals(afterMenu, afterKey,
                        "the shortcut for \"" + menuItemLabel + "\" did not produce the same effect as its menu");
            } finally {
                dispose(keyFrame);
            }
        } finally {
            dispose(menuFrame);
        }
    }

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
     * Dispatches, on that component, the same key event Swing would receive from the operating
     * system: a KEY_PRESSED with the accelerator's code and modifiers, never an Action invocation.
     */
    static void pressKey(Component target, KeyStroke accelerator) throws Exception {
        SwingUtilities.invokeAndWait(() -> target.dispatchEvent(new KeyEvent(
                target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                accelerator.getModifiers(), accelerator.getKeyCode(), KeyEvent.CHAR_UNDEFINED)));
    }

    /** Characters with no KeyStroke, like digits, resolve through a raw KeyListener on KEY_TYPED. */
    static void typeChar(Component target, char c) throws Exception {
        SwingUtilities.invokeAndWait(() -> target.dispatchEvent(new KeyEvent(
                target, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, c)));
    }

    /**
     * Triggers the action that opens a real modal dialog and handles it as soon as it opens, with
     * no Robot needed: a global AWTEventListener catches the JDialog's WINDOW_OPENED -which Swing
     * delivers inside the same nested loop that blocks setVisible(true)-, and right there the test
     * operates the dialog's real controls and closes it, letting the triggering thread continue.
     */
    static void withDialog(Runnable trigger, Consumer<JDialog> onOpen) throws Exception {
        CountDownLatch opened = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event.getSource() instanceof JDialog dialog) {
                try {
                    onOpen.accept(dialog);
                    if (dialog.isVisible()) {
                        throw new AssertionError(
                                "the withDialog callback left the dialog open: call dispose() before finishing");
                    }
                } catch (Throwable thrown) {
                    failure.set(thrown);
                    dialog.dispose();
                }
                opened.countDown();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            SwingUtilities.invokeAndWait(trigger::run);
            if (!opened.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("the dialog never opened a window (WINDOW_OPENED)");
            }
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
        rethrowIfCaptured(failure.get());
    }

    private static void rethrowIfCaptured(Throwable thrown) {
        if (thrown == null) {
            return;
        }
        if (thrown instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (thrown instanceof Error error) {
            throw error;
        }
        throw new AssertionError(thrown);
    }

    /**
     * Like {@link #withDialog(Runnable, Consumer)}, but returns the real dialog already open
     * instead of handling and closing it: it triggers the action without blocking -blocking with
     * invokeAndWait would trap the test thread in the modal's nested loop- so the test thread
     * stays free to wait for real events (focus, for instance) while the EDT keeps pumping that
     * loop. The caller is responsible for closing it.
     */
    static JDialog awaitDialog(Runnable trigger, long timeoutMillis) throws Exception {
        CountDownLatch opened = new CountDownLatch(1);
        JDialog[] captured = new JDialog[1];
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event.getSource() instanceof JDialog dialog) {
                captured[0] = dialog;
                opened.countDown();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            SwingUtilities.invokeLater(trigger::run);
            if (!opened.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new AssertionError("the dialog never opened a window (WINDOW_OPENED)");
            }
            return captured[0];
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

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

    /**
     * Requests real focus on the component and waits for the real FocusEvent (never a sleep):
     * with a real DISPLAY, the focus request is asynchronous, so the test cannot assume it already
     * has focus as soon as requestFocusInWindow returns.
     */
    static boolean requestFocusAndAwait(Component target, long timeoutMillis) throws Exception {
        if (target.isFocusOwner()) {
            return true;
        }
        CountDownLatch gained = new CountDownLatch(1);
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                gained.countDown();
            }
        };
        target.addFocusListener(listener);
        try {
            SwingUtilities.invokeLater(target::requestFocusInWindow);
            return gained.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            target.removeFocusListener(listener);
        }
    }

    static boolean awaitFocusOwner(Component target, long timeoutMillis) throws Exception {
        if (target.isFocusOwner()) {
            return true;
        }
        CountDownLatch gained = new CountDownLatch(1);
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                gained.countDown();
            }
        };
        target.addFocusListener(listener);
        try {
            return gained.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            target.removeFocusListener(listener);
        }
    }

    static boolean pressKeyAndAwaitFocusLost(Component target, KeyStroke keyStroke, long timeoutMillis)
            throws Exception {
        CountDownLatch lost = new CountDownLatch(1);
        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lost.countDown();
            }
        };
        target.addFocusListener(listener);
        try {
            SwingUtilities.invokeLater(() -> target.dispatchEvent(new KeyEvent(
                    target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    keyStroke.getModifiers(), keyStroke.getKeyCode(), KeyEvent.CHAR_UNDEFINED)));
            return lost.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            target.removeFocusListener(listener);
        }
    }

    private static final class NoScoreFiles implements ScoreFiles {
        @Override
        public Score load(Path path) {
            throw new UnsupportedOperationException("the audit does not touch the disk");
        }

        @Override
        public void save(Score score, Path path) {
            throw new UnsupportedOperationException("the audit does not touch the disk");
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

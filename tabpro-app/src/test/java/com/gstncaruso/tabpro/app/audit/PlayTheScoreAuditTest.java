package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class PlayTheScoreAuditTest {

    @Test
    void givenStoppedPlayback_whenSpacePausesAndResumes_thenControlsShowCurrentAction() throws Exception {
        Editor editor = editorWithANote();
        AuditSupport.RecordingPlayer player = new AuditSupport.RecordingPlayer();
        MainFrame frame = AuditSupport.newFrame(editor, player);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = AuditSupport.allMenuItems(frame.getJMenuBar()).stream()
                    .filter(menu -> KeyStroke.getKeyStroke("SPACE").equals(menu.getAccelerator()))
                    .findFirst().orElseThrow();
            JButton button = AuditSupport.findComponents(frame.getContentPane(), JButton.class).stream()
                    .filter(candidate -> candidate.getAction() == item.getAction())
                    .findFirst().orElseThrow();
            int[][] icons = new int[2][];

            SwingUtilities.invokeAndWait(() -> {
                assertFalse(player.isPlaying());
                assertPlaybackControls(item, button, "Reproducir");
                icons[0] = iconPixels(button);
            });

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(player.isPlaying(), "Space dispatched on ScoreCanvas starts playback");
                assertPlaybackControls(item, button, "Pausar");
                icons[1] = iconPixels(button);
                assertFalse(Arrays.equals(icons[0], icons[1]),
                        "the pause icon must look different from the play triangle");
            });

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(player.isPlaying(), "the second Space pauses playback");
                assertPlaybackControls(item, button, "Reanudar");
                assertArrayEquals(icons[0], iconPixels(button), "resume shows the play triangle");
            });

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(player.isPlaying(), "the third Space resumes playback");
                assertPlaybackControls(item, button, "Pausar");
                assertArrayEquals(icons[1], iconPixels(button), "playing shows the pause icon again");
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static void assertPlaybackControls(JMenuItem item, JButton button, String action) {
        assertEquals(action, item.getText(), "the menu names the currently available action");
        assertEquals(KeyStroke.getKeyStroke("SPACE"), item.getAccelerator());
        assertEquals(action, button.getAccessibleContext().getAccessibleName());
        assertEquals(action + "  [" + KeyEvent.getKeyText(KeyEvent.VK_SPACE) + "]", button.getToolTipText());
        assertTrue(button.getText() == null || button.getText().isEmpty(),
                "renaming the action must not expose text in the icon-only toolbar button");
    }

    private static int[] iconPixels(JButton button) {
        var icon = button.getIcon();
        assertNotNull(icon, "playback has a visible action icon");
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            icon.paintIcon(button, graphics, 0, 0);
        } finally {
            graphics.dispose();
        }
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    @Test
    void ctrlSpaceByTheShortcutPlaysFromTheBeginning() throws Exception {
        Editor editor = editorWithANote();
        editor.insertMeasure();
        editor.moveToLastMeasure();
        AuditSupport.RecordingPlayer player = new AuditSupport.RecordingPlayer();
        MainFrame frame = AuditSupport.newFrame(editor, player);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Reproducir desde el principio");
            assertNotNull(item, "could not find 'Reproducir desde el principio' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("ctrl SPACE"), item.getAccelerator());

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl SPACE"));

            assertTrue(player.playCalled(),
                    "Ctrl+Space, really dispatched, must start real playback from the beginning");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f9OpensTheRealLoopAndSpeedTrainerDialog() throws Exception {
        Editor editor = editorWithANote();
        editor.insertMeasure();
        MainFrame frame = AuditSupport.newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Loop / Entrenador de velocidad…");
            assertNotNull(item, "could not find 'Loop / Entrenador de velocidad…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("F9"), item.getAccelerator());

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F9"), 800);

            assertTrue(openedADialog, "F9, really dispatched on the canvas, must open the real dialog");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}

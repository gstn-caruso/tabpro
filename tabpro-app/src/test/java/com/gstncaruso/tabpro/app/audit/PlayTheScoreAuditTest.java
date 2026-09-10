package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class PlayTheScoreAuditTest {

    @Test
    void spaceByTheShortcutStartsRealPlaybackAndTheSecondSpaceStopsIt() throws Exception {
        Editor editor = editorWithANote();
        AuditSupport.RecordingPlayer player = new AuditSupport.RecordingPlayer();
        MainFrame frame = AuditSupport.newFrame(editor, player);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Reproducir / Detener");
            assertNotNull(item, "could not find 'Reproducir / Detener' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("SPACE"), item.getAccelerator());

            assertFalse(player.playCalled(), "nothing was pressed yet");

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            assertTrue(player.playCalled(),
                    "Space, really dispatched on the canvas, must start real playback");
            assertTrue(player.isPlaying());

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            assertTrue(player.stopCalled(),
                    "the second Space must stop the real playback started by the first one");
            assertFalse(player.isPlaying());
        } finally {
            AuditSupport.dispose(frame);
        }
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

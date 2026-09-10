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
            assertNotNull(item, "no encontre 'Reproducir / Detener' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("SPACE"), item.getAccelerator());

            assertFalse(player.playCalled(), "todavia no se apreto nada");

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            assertTrue(player.playCalled(),
                    "Espacio, despachado de verdad sobre el lienzo, tiene que arrancar la reproduccion real");
            assertTrue(player.isPlaying());

            pressKey(canvas, KeyStroke.getKeyStroke("SPACE"));
            assertTrue(player.stopCalled(),
                    "el segundo Espacio tiene que frenar la reproduccion real que arranco el primero");
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
            assertNotNull(item, "no encontre 'Reproducir desde el principio' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("ctrl SPACE"), item.getAccelerator());

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl SPACE"));

            assertTrue(player.playCalled(),
                    "Ctrl+Espacio, despachado de verdad, tiene que arrancar la reproduccion real desde el principio");
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
            assertNotNull(item, "no encontre 'Loop / Entrenador de velocidad…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("F9"), item.getAccelerator());

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F9"), 800);

            assertTrue(openedADialog, "F9, despachado de verdad sobre el lienzo, tiene que abrir el dialogo real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}

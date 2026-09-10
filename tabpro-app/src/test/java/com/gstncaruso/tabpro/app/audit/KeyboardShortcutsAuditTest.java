package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.assertAcceleratorMatchesMenu;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * JScrollPane and JSplitPane come with their own built-in keyboard shortcuts, so this class
 * dispatches real KeyEvents on a real MainFrame's canvas instead of only comparing accelerators
 * against the command catalog statically.
 */
@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class KeyboardShortcutsAuditTest {

    @Test
    void theOnlyFiveKeysThatScrollPaneAndSplitPaneAlreadyOccupiedAreTheDocumentedOnes() throws Exception {
        Editor editor = AuditSupport.blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JScrollPane scrollPane = findComponent(frame.getContentPane(), JScrollPane.class);
            JSplitPane splitPane = findComponent(frame.getContentPane(), JSplitPane.class);
            assertNotNull(scrollPane);
            assertNotNull(splitPane);

            List<String> collisions = new ArrayList<>();
            for (JMenuItem item : AuditSupport.allMenuItems(frame.getJMenuBar())) {
                KeyStroke accelerator = item.getAccelerator();
                if (accelerator == null) {
                    continue;
                }
                boolean occupiedInScroll =
                        scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(accelerator) != null;
                boolean occupiedInSplit =
                        splitPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(accelerator) != null;
                if (occupiedInScroll || occupiedInSplit) {
                    collisions.add(item.getText());
                }
            }

            assertEquals(
                    java.util.Set.of("Primer compás", "Último compás", "Propiedades de la pista…",
                            "Configurar página…", "Marcador siguiente"),
                    java.util.Set.copyOf(collisions),
                    "las cinco colisiones documentadas no cambiaron; si esto falla hay una nueva (o una que se arreglo)");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f6OpensTrackPropertiesWithTheScoreFocused() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item);
            assertEquals(KeyStroke.getKeyStroke("F6"), item.getAccelerator());

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F6"), 800);

            assertTrue(openedADialog,
                    "F6 con la partitura enfocada tiene que abrir 'Propiedades de la pista', "
                            + "igual que el menu Pista > Propiedades");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f8OpensPageSetupWithTheScoreFocused() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item);
            assertEquals(KeyStroke.getKeyStroke("F8"), item.getAccelerator());

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("F8"), 800);

            assertTrue(openedADialog,
                    "F8 con la partitura enfocada tiene que abrir 'Configurar página', "
                            + "igual que el menu Archivo > Configurar página");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void ctrlTabByMenuAndByShortcutMoveTheCursorToTheNextMarker() throws Exception {
        assertAcceleratorMatchesMenu("Marcador siguiente", () -> {
            Editor editor = editorWithMeasures(2);
            editor.moveTo(1, 0, editor.cursor().string());
            editor.setMarker(Marker.named("Solo"));
            editor.moveToFirstMeasure();
            return editor;
        });
    }

    @Test
    void undoByMenuAndByCtrlZShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Deshacer", () -> {
            Editor editor = editorWithANote();
            editor.setFret(5);
            return editor;
        });
    }

    @Test
    void cutByMenuAndByCtrlXShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Cortar", AuditSupport::editorWithANote);
    }

    @Test
    void shorteningTheNoteValueByMenuAndByPlusShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Acortar la figura", AuditSupport::editorWithANote);
    }

    @Test
    void raisingASemitoneByMenuAndByShiftPlusShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Subir un semitono", AuditSupport::editorWithANote);
    }

    @Test
    void hammerOnPullOffByMenuAndByHShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Ligado (hammer on / pull off)", AuditSupport::editorWithANote);
    }

    @Test
    void vibratoByMenuAndByVShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Vibrato", AuditSupport::editorWithANote);
    }

    @Test
    void palmMuteByMenuAndByPShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Palm mute", AuditSupport::editorWithANote);
    }

    @Test
    void deadNoteByMenuAndByXShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Nota muerta", AuditSupport::editorWithANote);
    }

    @Test
    void grayingTheInactiveVoiceByTheCtrlGShortcutChangesTheRealCanvas() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            boolean before = canvas.graysTheInactiveVoice();

            AuditSupport.pressKey(canvas, KeyStroke.getKeyStroke("ctrl G"));

            assertEquals(!before, canvas.graysTheInactiveVoice(),
                    "Ctrl+G, despachado de verdad sobre el lienzo, tiene que alternar el atenuado real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void nextTrackByMenuAndByCtrlDownShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Pista siguiente", () -> {
            Editor editor = editorWithANote();
            editor.addTrack(com.gstncaruso.tabpro.core.model.Track.standardBass("Bajo"));
            editor.selectTrack(0);
            return editor;
        });
    }

    @Test
    void previousBarByMenuAndByCtrlLeftShortcutMatch() throws Exception {
        assertAcceleratorMatchesMenu("Compás anterior", () -> {
            Editor editor = editorWithMeasures(2);
            editor.moveToLastMeasure();
            return editor;
        });
    }

    /**
     * Moving focus out for real requires a real KeyboardFocusManager, not a test double, and a
     * real window with a DISPLAY.
     */
    @Test
    void ctrlF6ReallyTakesFocusAwayFromTheScore() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertTrue(AuditSupport.requestFocusAndAwait(canvas, 2000),
                    "la partitura nunca gano el foco real para arrancar el test");

            boolean lostFocus = AuditSupport.pressKeyAndAwaitFocusLost(canvas, KeyStroke.getKeyStroke("ctrl F6"), 2000);

            assertTrue(lostFocus,
                    "Ctrl+F6 con la partitura enfocada tiene que sacarle el foco de verdad");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}

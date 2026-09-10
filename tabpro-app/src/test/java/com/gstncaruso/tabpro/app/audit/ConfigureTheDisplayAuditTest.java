package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.instruments.BeatViews;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ConfigureTheDisplayAuditTest {

    @Test
    void zoomingInAndOutByTheCtrlPlusAndCtrlMinusShortcutsChangesTheRealZoom() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            Zoom initial = canvas.zoom();

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl EQUALS"));
            Zoom zoomedIn = canvas.zoom();
            assertNotEquals(initial, zoomedIn, "Ctrl+ tiene que acercar el zoom real del lienzo");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            Zoom zoomedOut = canvas.zoom();
            assertNotEquals(zoomedIn, zoomedOut, "Ctrl- tiene que alejar el zoom real del lienzo");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 0"));
            assertEquals(Zoom.whole(), canvas.zoom(), "Ctrl+0 tiene que volver el zoom real al 100%");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theMixingConsoleThroughTheMenuHidesAndShowsTheRealTrackPanel() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            TrackPanel trackPanel = findComponent(frame.getContentPane(), TrackPanel.class);
            assertNotNull(trackPanel, "no encontre el TrackPanel real");
            assertEquals(true, trackPanel.isVisible(), "la mesa de mezcla arranca visible al abrir la ventana");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Mesa de mezcla");
            assertNotNull(item, "no encontre 'Mesa de mezcla' en el menu real");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, trackPanel.isVisible(),
                    "el menu real tiene que esconder el TrackPanel real");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, trackPanel.isVisible(),
                    "el menu real tiene que volver a mostrar el TrackPanel real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theFretboardByTheCtrl3ShortcutShowsTheRealBeatViews() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            assertNotNull(beatViews, "no encontre el BeatViews real");
            boolean before = beatViews.isFretboardVisible();

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));

            assertEquals(!before, beatViews.isFretboardVisible(),
                    "Ctrl+3, despachado de verdad, tiene que alternar el diapason real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theWindowStartsWithBothPanelsClosedAndCtrl3OpensTheRealFretboard() throws Exception {
        com.gstncaruso.tabpro.ui.Preferences preferences = new com.gstncaruso.tabpro.ui.Preferences();
        preferences.setFretboardVisible(false);
        preferences.setKeyboardVisible(false);
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            assertEquals(false, beatViews.isFretboardVisible(),
                    "la ventana real tiene que arrancar con el diapason cerrado, como en Guitar Pro 5");
            assertEquals(false, beatViews.isKeyboardVisible(),
                    "la ventana real tiene que arrancar con el teclado cerrado, como en Guitar Pro 5");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));

            assertEquals(true, beatViews.isFretboardVisible(),
                    "Ctrl+3, despachado de verdad, tiene que abrir el diapason real desde cerrado");
        } finally {
            preferences.setFretboardVisible(false);
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void thePanelsStatePersistsWhenReopeningTheWindow() throws Exception {
        com.gstncaruso.tabpro.ui.Preferences preferences = new com.gstncaruso.tabpro.ui.Preferences();
        Editor firstEditor = blankEditor();
        MainFrame firstFrame = newFrame(firstEditor);
        try {
            ScoreCanvas firstCanvas = findComponent(firstFrame.getContentPane(), ScoreCanvas.class);
            BeatViews firstBeatViews = findComponent(firstFrame.getContentPane(), BeatViews.class);
            if (firstBeatViews.isFretboardVisible()) {
                pressKey(firstCanvas, KeyStroke.getKeyStroke("ctrl 3"));
            }
            if (firstBeatViews.isKeyboardVisible()) {
                pressKey(firstCanvas, KeyStroke.getKeyStroke("ctrl 4"));
            }
            assertEquals(false, firstBeatViews.isFretboardVisible());
            assertEquals(false, firstBeatViews.isKeyboardVisible());
        } finally {
            AuditSupport.dispose(firstFrame);
        }

        Editor secondEditor = blankEditor();
        MainFrame secondFrame = newFrame(secondEditor);
        try {
            BeatViews secondBeatViews = findComponent(secondFrame.getContentPane(), BeatViews.class);
            assertEquals(false, secondBeatViews.isFretboardVisible(),
                    "una ventana nueva tiene que respetar que el diapason quedo cerrado la vez anterior");
            assertEquals(false, secondBeatViews.isKeyboardVisible(),
                    "una ventana nueva tiene que respetar que el teclado quedo cerrado la vez anterior");
        } finally {
            preferences.setFretboardVisible(false);
            preferences.setKeyboardVisible(false);
            AuditSupport.dispose(secondFrame);
        }
    }

    @Test
    void theFretboardBarCloseButtonDoesTheSameAsViewFretboard() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            if (!beatViews.isFretboardVisible()) {
                pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));
            }
            JButton close = AuditSupport.findButtonByAccessibleName(frame.getContentPane(), "Cerrar diapasón");
            assertNotNull(close, "no encontre la ✕ real del diapason");
            assertEquals(true, close.isShowing(), "el diapason tiene que estar abierto para poder cerrarlo con la ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "no pude poner el foco en la ✕ real antes de clickearla");
            boolean before = beatViews.isFretboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!before, beatViews.isFretboardVisible(),
                    "la ✕ real tiene que hacer lo mismo que Ver > Diapasón");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "la ✕ real tiene que devolver el foco a la partitura, igual que el comando del menu");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theKeyboardBarCloseButtonDoesTheSameAsViewKeyboard() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            BeatViews beatViews = findComponent(frame.getContentPane(), BeatViews.class);
            if (!beatViews.isKeyboardVisible()) {
                pressKey(canvas, KeyStroke.getKeyStroke("ctrl 4"));
            }
            JButton close = AuditSupport.findButtonByAccessibleName(frame.getContentPane(), "Cerrar teclado");
            assertNotNull(close, "no encontre la ✕ real del teclado");
            assertEquals(true, close.isShowing(), "el teclado tiene que estar abierto para poder cerrarlo con la ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "no pude poner el foco en la ✕ real antes de clickearla");
            boolean before = beatViews.isKeyboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!before, beatViews.isKeyboardVisible(),
                    "la ✕ real tiene que hacer lo mismo que Ver > Teclado");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "la ✕ real tiene que devolver el foco a la partitura, igual que el comando del menu");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void pageModeThroughTheMenuChangesTheRealCanvasViewMode() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotEquals(ViewMode.PAGE, canvas.viewMode(), "no arranca ya en modo pagina");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Modo página");
            assertNotNull(item, "no encontre 'Modo página' en el menu real");

            SwingUtilities.invokeAndWait(item::doClick);

            assertEquals(ViewMode.PAGE, canvas.viewMode(),
                    "el menu real tiene que dejar el canvas real en modo pagina");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static java.util.concurrent.CountDownLatch focusGainedLatch(java.awt.Component component) {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        component.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent event) {
                latch.countDown();
            }
        });
        return latch;
    }
}

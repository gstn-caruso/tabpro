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

@Tag("integration")
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
            assertNotEquals(initial, zoomedIn, "Ctrl+ must zoom in the real canvas zoom");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            pressKey(canvas, KeyStroke.getKeyStroke("ctrl MINUS"));
            Zoom zoomedOut = canvas.zoom();
            assertNotEquals(zoomedIn, zoomedOut, "Ctrl- must zoom out the real canvas zoom");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 0"));
            assertEquals(Zoom.whole(), canvas.zoom(), "Ctrl+0 must bring the real zoom back to 100%");
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
            assertNotNull(trackPanel, "could not find the real TrackPanel");
            assertEquals(true, trackPanel.isVisible(), "the mixing console starts visible when the window opens");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Mesa de mezcla");
            assertNotNull(item, "could not find 'Mesa de mezcla' in the real menu");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(false, trackPanel.isVisible(),
                    "the real menu must hide the real TrackPanel");

            SwingUtilities.invokeAndWait(item::doClick);
            assertEquals(true, trackPanel.isVisible(),
                    "the real menu must show the real TrackPanel again");
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
            assertNotNull(beatViews, "could not find the real BeatViews");
            boolean before = beatViews.isFretboardVisible();

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));

            assertEquals(!before, beatViews.isFretboardVisible(),
                    "Ctrl+3, really dispatched, must toggle the real fretboard");
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
                    "the real window must start with the fretboard closed, as in Guitar Pro 5");
            assertEquals(false, beatViews.isKeyboardVisible(),
                    "the real window must start with the keyboard closed, as in Guitar Pro 5");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl 3"));

            assertEquals(true, beatViews.isFretboardVisible(),
                    "Ctrl+3, really dispatched, must open the real fretboard from closed");
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
                    "a new window must respect that the fretboard was left closed last time");
            assertEquals(false, secondBeatViews.isKeyboardVisible(),
                    "a new window must respect that the keyboard was left closed last time");
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
            assertNotNull(close, "could not find the real fretboard ✕");
            assertEquals(true, close.isShowing(), "the fretboard must be open to be able to close it with the ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "could not put focus on the real ✕ before clicking it");
            boolean before = beatViews.isFretboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!before, beatViews.isFretboardVisible(),
                    "the real ✕ must do the same as Ver > Diapasón");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "the real ✕ must return focus to the score, same as the menu command");
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
            assertNotNull(close, "could not find the real keyboard ✕");
            assertEquals(true, close.isShowing(), "the keyboard must be open to be able to close it with the ✕");
            assertEquals(true, AuditSupport.requestFocusAndAwait(close, 2000),
                    "could not put focus on the real ✕ before clicking it");
            boolean before = beatViews.isKeyboardVisible();
            java.util.concurrent.CountDownLatch focusBackOnTheScore = focusGainedLatch(canvas);

            SwingUtilities.invokeAndWait(close::doClick);

            assertEquals(!before, beatViews.isKeyboardVisible(),
                    "the real ✕ must do the same as Ver > Teclado");
            assertEquals(true,
                    focusBackOnTheScore.await(2, java.util.concurrent.TimeUnit.SECONDS),
                    "the real ✕ must return focus to the score, same as the menu command");
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
            assertNotEquals(ViewMode.PAGE, canvas.viewMode(), "does not start in page mode already");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Modo página");
            assertNotNull(item, "could not find 'Modo página' in the real menu");

            SwingUtilities.invokeAndWait(item::doClick);

            assertEquals(ViewMode.PAGE, canvas.viewMode(),
                    "the real menu must leave the real canvas in page mode");
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

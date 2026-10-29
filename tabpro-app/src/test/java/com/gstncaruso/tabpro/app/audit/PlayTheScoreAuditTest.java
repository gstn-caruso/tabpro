package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.tracks.MeasureGrid;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
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

    @Test
    void givenPlayingMeasureManuallyHidden_whenPaused_thenViewportStaysAndPlayheadIsRetained() throws Exception {
        Editor editor = AuditSupport.editorWithMeasures(63);
        editor.setFret(3);
        AuditSupport.RecordingPlayer player = new AuditSupport.RecordingPlayer();
        MainFrame frame = AuditSupport.newFrame(editor, player);
        try {
            BeatPosition audibleBeat = new BeatPosition(0, 0, 0);
            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(900, 650);
                frame.validate();
                frame.transport().toggle();
                player.beatStarted(audibleBeat);
            });
            // The real Transport dispatches the player's beat onto the EDT before this assertion.
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(frame.transport().isPlaying(), "setup must start playback through the real transport");
                assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0));
                MeasureGrid grid = findComponent(frame.getContentPane(), MeasureGrid.class);
                assertNotNull(grid);
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, grid);
                assertNotNull(viewport);
                Rectangle playingCell = SwingUtilities.convertRectangle(
                        grid, grid.cellBounds(0, audibleBeat.measure()), viewport.getView());
                assertTrue(viewport.getViewRect().contains(playingCell),
                        "setup must initially expose the audible measure in the real mixer viewport");
                JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, grid);
                pane.getHorizontalScrollBar().setValue(pane.getHorizontalScrollBar().getMaximum());
                Point manuallyScrolled = viewport.getViewPosition();
                assertTrue(manuallyScrolled.x >= playingCell.x + playingCell.width,
                        "manual horizontal scrolling must hide the audible measure before pausing");

                frame.transport().toggle();

                assertAll(
                        () -> assertTrue(frame.transport().isPaused(), "the transport must remain paused"),
                        () -> assertFalse(frame.transport().isPlaying(), "no audio plays while paused"),
                        () -> assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0),
                                "pausing must retain the playhead for resuming"),
                        () -> assertEquals(manuallyScrolled, viewport.getViewPosition(),
                                "pausing must not pull the manually scrolled grid back to the retained playhead"));
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void givenPausedPlayheadManuallyHidden_whenResumed_thenRetainedMeasureIsFullyVisible() throws Exception {
        Editor editor = AuditSupport.editorWithMeasures(63);
        editor.setFret(3);
        AuditSupport.RecordingPlayer player = new AuditSupport.RecordingPlayer();
        MainFrame frame = AuditSupport.newFrame(editor, player);
        try {
            BeatPosition audibleBeat = new BeatPosition(0, 0, 0);
            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(900, 650);
                frame.validate();
                frame.transport().toggle();
                player.beatStarted(audibleBeat);
            });
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(frame.transport().isPlaying(), "setup must start playback through the real transport");
                assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0));
                MeasureGrid grid = findComponent(frame.getContentPane(), MeasureGrid.class);
                assertNotNull(grid);
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, grid);
                assertNotNull(viewport);
                Rectangle playingCell = SwingUtilities.convertRectangle(
                        grid, grid.cellBounds(0, audibleBeat.measure()), viewport.getView());
                assertTrue(viewport.getViewRect().contains(playingCell),
                        "setup must initially expose the whole audible measure");
                var cursorBefore = editor.cursor();
                var caretBefore = grid.caret();

                frame.transport().toggle();
                assertTrue(frame.transport().isPaused(), "manual scrolling happens after pausing");
                assertFalse(frame.transport().isPlaying());
                assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0));
                JScrollPane pane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, grid);
                pane.getHorizontalScrollBar().setValue(pane.getHorizontalScrollBar().getMaximum());
                Point manuallyScrolled = viewport.getViewPosition();
                assertTrue(manuallyScrolled.x >= playingCell.x + playingCell.width,
                        "manual scrolling while paused must hide the retained measure");

                frame.transport().toggleMetronome();

                assertAll(
                        () -> assertTrue(frame.transport().isPaused()),
                        () -> assertFalse(frame.transport().isPlaying()),
                        () -> assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0)),
                        () -> assertEquals(manuallyScrolled, viewport.getViewPosition(),
                                "a metronome change while paused must not scroll to the retained playhead"));

                frame.transport().toggle();

                assertAll(
                        () -> assertTrue(frame.transport().isPlaying(), "resuming must restart playback"),
                        () -> assertFalse(frame.transport().isPaused()),
                        () -> assertEquals(Optional.of(audibleBeat), frame.transport().playingOn(0),
                                "resuming must recover the same retained beat without another beat notification"),
                        () -> assertTrue(viewport.getViewRect().contains(playingCell),
                                "resuming must automatically expose the whole retained audible measure"),
                        () -> assertEquals(manuallyScrolled.y, viewport.getViewPosition().y,
                                "following playback must not move the grid vertically"),
                        () -> assertEquals(cursorBefore, editor.cursor(), "following must not move the editor cursor"),
                        () -> assertEquals(caretBefore, grid.caret(), "following must not move the grid caret"));
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

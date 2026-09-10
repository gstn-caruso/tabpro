package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ScoreCanvasTest {

    private final Editor editor = new Editor(new Score("Test", 120, List.of(
            Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));
    private final ScoreCanvas canvas = new ScoreCanvas(editor);

    @Test
    void hasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(canvas);
    }

    @Test
    void startsInTheMultitrackView() {
        assertTrue(canvas.isMultitrack());
    }

    @Test
    void notifiesWhenTheZoomChanges() {
        boolean[] notified = {false};
        canvas.onZoomChange(() -> notified[0] = true);

        canvas.zoomIn();

        assertTrue(notified[0]);
    }

    @Test
    void disablesItsFocusKeysSoTabReachesTheKeyboardEditor() {
        assertFalse(canvas.getFocusTraversalKeysEnabled());
    }

    @Test
    void ctrlF6AsksTheFocusTraversalToGoToTheNextComponent() {
        RecordingFocusTraversal recorder = new RecordingFocusTraversal();
        ScoreCanvas canvasWithRecordedFocus = new ScoreCanvas(editor, new TrackVisibility(), recorder);

        pressShortcut(canvasWithRecordedFocus, javax.swing.KeyStroke.getKeyStroke("ctrl F6"));

        assertEquals(canvasWithRecordedFocus, recorder.nextRequestedFrom);
    }

    @Test
    void ctrlShiftF6AsksTheFocusTraversalToGoToThePreviousComponent() {
        RecordingFocusTraversal recorder = new RecordingFocusTraversal();
        ScoreCanvas canvasWithRecordedFocus = new ScoreCanvas(editor, new TrackVisibility(), recorder);

        pressShortcut(canvasWithRecordedFocus, javax.swing.KeyStroke.getKeyStroke("ctrl shift F6"));

        assertEquals(canvasWithRecordedFocus, recorder.previousRequestedFrom);
    }

    private static void pressShortcut(javax.swing.JComponent component, javax.swing.KeyStroke keyStroke) {
        Object name = component.getInputMap(javax.swing.JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name)
                .actionPerformed(new java.awt.event.ActionEvent(component, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
    }

    private static final class RecordingFocusTraversal implements FocusTraversal {
        private java.awt.Component nextRequestedFrom;
        private java.awt.Component previousRequestedFrom;

        @Override
        public void next(java.awt.Component component) {
            nextRequestedFrom = component;
        }

        @Override
        public void previous(java.awt.Component component) {
            previousRequestedFrom = component;
        }
    }

    @Test
    void leavingTheMultitrackViewLeavesRoomForOneTrackOnly() {
        int everyTrack = canvas.getPreferredSize().height;

        canvas.setMultitrack(false);

        assertTrue(canvas.getPreferredSize().height < everyTrack);
    }

    @Test
    void turningATrackOffLeavesTheSameRoomAsLeavingTheMultitrackView() {
        canvas.setMultitrack(false);
        int onlyTheActiveOne = canvas.getPreferredSize().height;

        canvas.setMultitrack(true);
        canvas.setTrackShown(1, false);

        assertEquals(onlyTheActiveOne, canvas.getPreferredSize().height);
    }

    @Test
    void hidingANotationMakesTheScoreShorter() {
        int both = canvas.getPreferredSize().height;

        canvas.setStandardNotationShown(false);

        assertFalse(canvas.showsStandardNotation());
        assertTrue(canvas.showsTablature());
        assertTrue(canvas.getPreferredSize().height < both);
    }

    @Test
    void hidingBothNotationsBringsTheOtherOneBack() {
        canvas.setTablatureShown(false);
        canvas.setStandardNotationShown(false);

        assertTrue(canvas.showsTablature(), "a track without any notation would not be visible");
    }

    @Test
    void theActiveTrackIsTheOneTheCursorIsOn() {
        canvas.setMultitrack(false);
        int height = canvas.getPreferredSize().height;

        editor.selectTrack(1);

        assertTrue(canvas.getPreferredSize().height != height,
                "the bass has four strings, so it takes up less height than the guitar");
    }

    @Test
    void draggingWithControlHeldSelectsWholeMeasures() {
        Editor twoMeasures = editorWithTwoMeasures();
        ScoreCanvas canvasWithTwoMeasures = new ScoreCanvas(twoMeasures);
        ScoreLayout layout = ScoreLayout.of(twoMeasures.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        Rectangle secondMeasureBeat = layout.beatBounds(0, 1, 0);

        press(canvasWithTwoMeasures, centerX(firstBeat), centerY(firstBeat), true);
        drag(canvasWithTwoMeasures, centerX(secondMeasureBeat), centerY(secondMeasureBeat), true);

        Selection selection = canvasWithTwoMeasures.selection().orElseThrow();
        assertTrue(selection.wholeMeasures());
        assertEquals(0, selection.fromMeasure());
        assertEquals(1, selection.toMeasure());
    }

    @Test
    void ctrlClickingJustOneMeasureSelectsItWholeWithoutNeedingToDrag() {
        Editor twoMeasures = editorWithTwoMeasures();
        ScoreCanvas canvasWithTwoMeasures = new ScoreCanvas(twoMeasures);
        ScoreLayout layout = ScoreLayout.of(twoMeasures.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);

        press(canvasWithTwoMeasures, centerX(firstBeat), centerY(firstBeat), true);

        Selection selection = canvasWithTwoMeasures.selection().orElseThrow();
        assertTrue(selection.wholeMeasures());
        assertEquals(0, selection.fromMeasure());
        assertEquals(0, selection.toMeasure());
    }

    @Test
    void draggingWithoutControlStillSelectsOnlyTheBeatsTouched() {
        Editor twoMeasures = editorWithTwoMeasures();
        ScoreCanvas canvasWithTwoMeasures = new ScoreCanvas(twoMeasures);
        ScoreLayout layout = ScoreLayout.of(twoMeasures.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        Rectangle secondMeasureBeat = layout.beatBounds(0, 1, 0);

        press(canvasWithTwoMeasures, centerX(firstBeat), centerY(firstBeat), false);
        drag(canvasWithTwoMeasures, centerX(secondMeasureBeat), centerY(secondMeasureBeat), false);

        Selection selection = canvasWithTwoMeasures.selection().orElseThrow();
        assertFalse(selection.wholeMeasures());
    }

    @Test
    void clickingSomewhereClearsAnyActiveSelection() {
        Editor twoMeasures = editorWithTwoMeasures();
        twoMeasures.selectAll();
        ScoreCanvas canvasWithTwoMeasures = new ScoreCanvas(twoMeasures);
        ScoreLayout layout = ScoreLayout.of(twoMeasures.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);

        press(canvasWithTwoMeasures, centerX(firstBeat), centerY(firstBeat), false);

        assertTrue(canvasWithTwoMeasures.selection().isEmpty());
    }

    @Test
    void shiftClickExtendsTheSelectionInsteadOfClearingIt() {
        Editor twoMeasures = editorWithTwoMeasures();
        ScoreCanvas canvasWithTwoMeasures = new ScoreCanvas(twoMeasures);
        ScoreLayout layout = ScoreLayout.of(twoMeasures.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        Rectangle secondMeasureBeat = layout.beatBounds(0, 1, 0);

        press(canvasWithTwoMeasures, centerX(firstBeat), centerY(firstBeat), false);
        shiftClick(canvasWithTwoMeasures, centerX(secondMeasureBeat), centerY(secondMeasureBeat));

        Selection selection = canvasWithTwoMeasures.selection().orElseThrow();
        assertEquals(0, selection.fromMeasure());
        assertEquals(1, selection.toMeasure());
    }

    @Test
    void rightClickingAStringOffersTheFretsOfItsTrack() {
        ScoreLayout layout = ScoreLayout.of(editor.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        int x = centerX(firstBeat);
        int y = layout.stringY(0, 0, 1);

        JPopupMenu menu = canvas.contextMenuAt(x, y).orElseThrow();

        assertEquals(Tuning.MAX_FRET + 1, menu.getComponentCount());
    }

    @Test
    void choosingAFretFromTheContextMenuWritesItOnTheClickedString() {
        ScoreLayout layout = ScoreLayout.of(editor.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        int x = centerX(firstBeat);
        int y = layout.stringY(0, 0, 2);

        JPopupMenu menu = canvas.contextMenuAt(x, y).orElseThrow();
        ((JMenuItem) menu.getComponent(7)).doClick();

        assertEquals(Optional.of(new Note(2, 7)), editor.currentBeat().noteOn(2));
    }

    @Test
    void rightClickingAPercussionTrackOffersItsSoundsInsteadOfFrets() {
        Editor percussionEditor = new Editor(new Score("Test", 120, List.of(Track.percussion("Bateria"))));
        ScoreCanvas percussionCanvas = new ScoreCanvas(percussionEditor);
        ScoreLayout layout = ScoreLayout.of(percussionEditor.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        int x = centerX(firstBeat);
        int y = layout.stringY(0, 0, 1);

        JPopupMenu menu = percussionCanvas.contextMenuAt(x, y).orElseThrow();

        assertEquals(PercussionKit.sounds().size(), menu.getComponentCount());
    }

    @Test
    void rightClickingOutsideTheScoreOffersNoMenu() {
        assertTrue(canvas.contextMenuAt(-100, -100).isEmpty());
    }

    @Test
    void aClickOnTheScoreTellsWhoeverIsListeningWhereItLanded() {
        ScoreLayout layout = ScoreLayout.of(editor.score(), 900);
        Rectangle firstBeat = layout.beatBounds(0, 0, 0);
        List<ScoreLayout.Hit> notified = new java.util.ArrayList<>();
        canvas.onClickReposition(notified::add);

        press(canvas, centerX(firstBeat), layout.stringY(0, 0, 1), false);

        assertEquals(1, notified.size());
        assertEquals(0, notified.get(0).measure());
        assertEquals(0, notified.get(0).beat());
    }

    @Test
    void clickingOutsideTheScoreDoesNotNotifyAnyReposition() {
        List<ScoreLayout.Hit> notified = new java.util.ArrayList<>();
        canvas.onClickReposition(notified::add);

        press(canvas, -100, -100, false);

        assertTrue(notified.isEmpty());
    }

    @Test
    void showPlayheadScrollsToKeepItVisibleByDefault() {
        ScoreCanvas horizontal = canvasWithManyMeasuresScrolledHorizontally();
        JScrollPane pane = paneShowing(horizontal);

        horizontal.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(0, 29, 0)));

        assertTrue(pane.getViewport().getViewPosition().x > 0,
                "with auto-scroll on (the default) a distant playhead has to bring the view up to it");
    }

    @Test
    void turningAutoScrollOffLeavesThePlayheadOffScreen() {
        ScoreCanvas horizontal = canvasWithManyMeasuresScrolledHorizontally();
        horizontal.setAutoScrollDuringPlayback(false);
        JScrollPane pane = paneShowing(horizontal);

        horizontal.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(0, 29, 0)));

        assertEquals(0, pane.getViewport().getViewPosition().x,
                "with auto-scroll unchecked the view must not move even when the playhead is out of sight");
    }

    @Test
    void doesNotScrollWhenTheAncestorViewportHasNoSizeYet() throws Exception {
        Editor manyMeasures = editorWithManyMeasures(30);
        ScoreCanvas horizontal = new ScoreCanvas(manyMeasures);
        horizontal.setViewMode(ViewMode.SCREEN_HORIZONTAL);
        JScrollPane pane = new JScrollPane(horizontal);

        SwingUtilities.invokeAndWait(manyMeasures::moveToLastMeasure);

        assertEquals(0, pane.getViewport().getViewPosition().x,
                "without a layout yet (viewport 0x0) the scroll must not move to a nonsensical place");
        assertEquals(0, pane.getViewport().getViewPosition().y,
                "without a layout yet (viewport 0x0) the scroll must not move to a nonsensical place");
    }

    @Test
    void deliversTheEditorNotificationOnTheEdtEvenWhenItCameFromAnotherThread() throws Exception {
        Editor manyMeasures = editorWithManyMeasures(30);
        ScoreCanvas horizontal = new ScoreCanvas(manyMeasures);
        horizontal.setViewMode(ViewMode.SCREEN_HORIZONTAL);
        JScrollPane pane = paneShowing(horizontal);

        CountDownLatch releaseEdt = blockTheEdtQueueUntilReleased();

        Thread background = new Thread(manyMeasures::moveToLastMeasure);
        background.start();
        background.join();

        assertEquals(0, pane.getViewport().getViewPosition().x,
                "it has not reached the EDT yet: the scroll from another thread cannot have applied already");

        releaseEdt.countDown();
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(pane.getViewport().getViewPosition().x > 0,
                "once the EDT has processed the queue, the real scroll has to have arrived");
    }

    private static CountDownLatch blockTheEdtQueueUntilReleased() {
        CountDownLatch releaseEdt = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> await(releaseEdt));
        return releaseEdt;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static ScoreCanvas canvasWithManyMeasuresScrolledHorizontally() {
        ScoreCanvas manyMeasures = new ScoreCanvas(editorWithManyMeasures(30));
        manyMeasures.setViewMode(ViewMode.SCREEN_HORIZONTAL);
        return manyMeasures;
    }

    private static Editor editorWithManyMeasures(int count) {
        List<Measure> measures = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            measures.add(Measure.empty(TimeSignature.fourFour(), Duration.quarter()));
        }
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(measures);
        return new Editor(new Score("Test", 120, List.of(guitar)));
    }

    private static JScrollPane paneShowing(ScoreCanvas canvas) {
        canvas.setSize(canvas.getPreferredSize());
        JScrollPane pane = new JScrollPane(canvas);
        pane.setSize(200, 200);
        pane.doLayout();
        pane.getViewport().doLayout();
        return pane;
    }

    private static Editor editorWithTwoMeasures() {
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(List.of(
                Measure.empty(TimeSignature.fourFour(), Duration.quarter()),
                Measure.empty(TimeSignature.fourFour(), Duration.quarter())));
        return new Editor(new Score("Test", 120, List.of(guitar)));
    }

    @Test
    void movingTheCursorSkipsRevalidateAndRepaintsOnlyTheCursorArea() throws Exception {
        Editor twoMeasures = editorWithTwoMeasures();
        SpyingScoreCanvas spy = new SpyingScoreCanvas(twoMeasures);
        spy.forgetCallsMadeWhileBuilding();
        Rectangle before = expectedCursorBounds(twoMeasures, 0);

        SwingUtilities.invokeAndWait(twoMeasures::moveToNextMeasure);

        Rectangle after = expectedCursorBounds(twoMeasures, 1);
        assertEquals(0, spy.revalidateCalls);
        assertFalse(spy.fullRepaintCalled);
        assertEquals(List.of(before.union(after)), spy.repaintedAreas);
    }

    @Test
    void editingANoteStillRevalidatesAndRepaintsEverything() throws Exception {
        Editor twoMeasures = editorWithTwoMeasures();
        SpyingScoreCanvas spy = new SpyingScoreCanvas(twoMeasures);
        spy.forgetCallsMadeWhileBuilding();

        SwingUtilities.invokeAndWait(() -> twoMeasures.setFret(3));

        assertEquals(1, spy.revalidateCalls);
        assertTrue(spy.fullRepaintCalled);
    }

    private static Rectangle expectedCursorBounds(Editor editor, int measure) {
        ScoreViewport viewport = ScoreViewport.of(ViewMode.SCREEN_VERTICAL, Zoom.whole(), 900);
        return PageScorePainter.boundsOf(editor.score(), viewport, 0, measure, 0);
    }

    private static final class SpyingScoreCanvas extends ScoreCanvas {
        private int revalidateCalls;
        private boolean fullRepaintCalled;
        private final List<Rectangle> repaintedAreas = new java.util.ArrayList<>();

        SpyingScoreCanvas(Editor editor) {
            super(editor);
        }

        @Override
        public void revalidate() {
            revalidateCalls++;
            super.revalidate();
        }

        @Override
        public void repaint() {
            fullRepaintCalled = true;
            super.repaint();
        }

        @Override
        public void repaint(Rectangle area) {
            repaintedAreas.add(area);
            super.repaint(area);
        }

        void forgetCallsMadeWhileBuilding() {
            revalidateCalls = 0;
            fullRepaintCalled = false;
            repaintedAreas.clear();
        }
    }

    private static void press(ScoreCanvas target, int x, int y, boolean controlHeld) {
        target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                controlHeld ? InputEvent.CTRL_DOWN_MASK : 0, x, y, 1, false));
    }

    private static void drag(ScoreCanvas target, int x, int y, boolean controlHeld) {
        target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(),
                controlHeld ? InputEvent.CTRL_DOWN_MASK : 0, x, y, 1, false));
    }

    private static void shiftClick(ScoreCanvas target, int x, int y) {
        target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK, x, y, 1, false));
    }

    private static int centerX(Rectangle rectangle) {
        return rectangle.x + rectangle.width / 2;
    }

    private static int centerY(Rectangle rectangle) {
        return rectangle.y + rectangle.height / 2;
    }
}

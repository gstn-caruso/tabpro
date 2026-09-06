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
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.jupiter.api.Test;

class ScoreCanvasTest {

    private final Editor editor = new Editor(new Score("Prueba", 120, List.of(
            Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));
    private final ScoreCanvas canvas = new ScoreCanvas(editor);

    @Test
    void startsInTheMultitrackView() {
        assertTrue(canvas.isMultitrack());
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

        assertTrue(canvas.showsTablature(), "una pista sin ninguna notacion no se veria");
    }

    @Test
    void theActiveTrackIsTheOneTheCursorIsOn() {
        canvas.setMultitrack(false);
        int height = canvas.getPreferredSize().height;

        editor.selectTrack(1);

        assertTrue(canvas.getPreferredSize().height != height,
                "el bajo tiene cuatro cuerdas, asi que ocupa menos alto que la guitarra");
    }

    /**
     * El manual, en Multiple Selection: "para seleccionar compases completos, apreta Ctrl
     * mientras haces la seleccion". Arrastrando con Ctrl apretado, la seleccion tiene que
     * abarcar los compases enteros que toco el arrastre, no solo los beats.
     */
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

    /**
     * El manual, en Using the Mouse: "Note > 0 to 30 (clic derecho sobre la tablatura)". El
     * menu tiene que ofrecer los trastes de la cuerda donde cayo el clic.
     */
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
        Editor percussionEditor = new Editor(new Score("Prueba", 120, List.of(Track.percussion("Bateria"))));
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

    /**
     * El manual deja reposicionar el audio con un clic durante la reproduccion. El lienzo no
     * sabe nada del Transport, asi que avisa donde cayo el clic para que quien lo escuche
     * decida si hay que saltar la reproduccion ahi.
     */
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

    private static Editor editorWithTwoMeasures() {
        Track guitar = Track.standardGuitar("Guitarra").withMeasures(List.of(
                Measure.empty(TimeSignature.fourFour(), Duration.quarter()),
                Measure.empty(TimeSignature.fourFour(), Duration.quarter())));
        return new Editor(new Score("Prueba", 120, List.of(guitar)));
    }

    private static void press(ScoreCanvas target, int x, int y, boolean controlHeld) {
        target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                controlHeld ? InputEvent.CTRL_DOWN_MASK : 0, x, y, 1, false));
    }

    private static void drag(ScoreCanvas target, int x, int y, boolean controlHeld) {
        target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(),
                controlHeld ? InputEvent.CTRL_DOWN_MASK : 0, x, y, 1, false));
    }

    private static int centerX(Rectangle rectangle) {
        return rectangle.x + rectangle.width / 2;
    }

    private static int centerY(Rectangle rectangle) {
        return rectangle.y + rectangle.height / 2;
    }
}

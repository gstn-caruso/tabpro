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

    private final Editor editor = new Editor(new Score("Prueba", 120, List.of(
            Track.standardGuitar("Guitarra"), Track.standardBass("Bajo"))));
    private final ScoreCanvas canvas = new ScoreCanvas(editor);

    @Test
    void noQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(canvas);
    }

    @Test
    void startsInTheMultitrackView() {
        assertTrue(canvas.isMultitrack());
    }

    @Test
    void avisaCuandoElZoomCambia() {
        boolean[] avisado = {false};
        canvas.onZoomChange(() -> avisado[0] = true);

        canvas.zoomIn();

        assertTrue(avisado[0]);
    }

    /**
     * Tab es, de fabrica, una tecla de navegacion de foco para cualquier JComponent: si
     * ScoreCanvas no la desactiva, AWT se queda con ella para mover el foco antes de que
     * KeyboardEditing (que si tiene el binding de Tab) llegue a verla.
     */
    @Test
    void desactivaSusTeclasDeFocoParaQueTabLlegueAlEditorDeTeclado() {
        assertFalse(canvas.getFocusTraversalKeysEnabled());
    }

    /**
     * Con Tab reservado para alternar tablatura/pentagrama, la partitura necesita otra forma de
     * ceder el foco. Ctrl+Tab y Shift+Tab ya son "Marcador siguiente/anterior" del manual, y F6
     * ya es "Propiedades de la pista": el primer par libre, en el orden que pide el manual de
     * atajos, es Ctrl+F6 / Ctrl+Shift+F6.
     */
    @Test
    void ctrlF6LePideALaCosturaDeFocoQueVayaAlSiguienteComponente() {
        RecordingFocusTraversal recorder = new RecordingFocusTraversal();
        ScoreCanvas canvasWithRecordedFocus = new ScoreCanvas(editor, new TrackVisibility(), recorder);

        pressShortcut(canvasWithRecordedFocus, javax.swing.KeyStroke.getKeyStroke("ctrl F6"));

        assertEquals(canvasWithRecordedFocus, recorder.nextRequestedFrom);
    }

    @Test
    void ctrlShiftF6LePideALaCosturaDeFocoQueVayaAlComponenteAnterior() {
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
     * Como en Guitar Pro 5 y en cualquier editor: un clic sin Shift limpia cualquier seleccion
     * vieja, aunque no arrastre a ningun lado.
     */
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

    /**
     * Como en Guitar Pro 5 y en cualquier editor: Shift mas clic no limpia la seleccion, la
     * extiende desde donde estaba el cursor hasta donde cayo el clic.
     */
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

    /**
     * Preferencias [F12], "Desplazar la pantalla durante la reproduccion": destildarla no hacia
     * nada, porque {@code showPlayhead} pedia el scroll sin preguntar. La prueba mete el lienzo
     * en un JScrollPane de verdad y mira si la vista se mueve -el efecto en la pantalla-, no si
     * la preferencia "quedo guardada", que es justo lo que no alcanzaba antes.
     */
    @Test
    void showPlayheadScrollsToKeepItVisibleByDefault() {
        ScoreCanvas horizontal = canvasWithManyMeasuresScrolledHorizontally();
        JScrollPane pane = paneShowing(horizontal);

        horizontal.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(0, 29, 0)));

        assertTrue(pane.getViewport().getViewPosition().x > 0,
                "con el auto-scroll prendido (el default) un playhead lejano tiene que traer la vista hasta el");
    }

    @Test
    void turningAutoScrollOffLeavesThePlayheadOffScreen() {
        ScoreCanvas horizontal = canvasWithManyMeasuresScrolledHorizontally();
        horizontal.setAutoScrollDuringPlayback(false);
        JScrollPane pane = paneShowing(horizontal);

        horizontal.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(0, 29, 0)));

        assertEquals(0, pane.getViewport().getViewPosition().x,
                "con el auto-scroll destildado la vista no se tiene que mover aunque el playhead quede afuera");
    }

    /**
     * Auditoria de corpus, hallazgo 2: justo despues de cambiar de modo de vista, el JViewport
     * real todavia mide 0x0 -Swing no layouteo todavia-. Pedirle un scroll ahi (como hacia
     * {@code editorChanged} sin guarda) no tira excepcion en un test headless, pero mueve la
     * vista a una posicion sin sentido -el mismo mecanismo que en pantalla real termina en
     * {@code IllegalArgumentException: Width (0) and height (0) cannot be <= 0}.
     */
    @Test
    void doesNotScrollWhenTheAncestorViewportHasNoSizeYet() throws Exception {
        Editor manyMeasures = editorWithManyMeasures(30);
        ScoreCanvas horizontal = new ScoreCanvas(manyMeasures);
        horizontal.setViewMode(ViewMode.SCREEN_HORIZONTAL);
        JScrollPane pane = new JScrollPane(horizontal);

        SwingUtilities.invokeAndWait(manyMeasures::moveToLastMeasure);

        assertEquals(0, pane.getViewport().getViewPosition().x,
                "sin layout todavia (viewport 0x0) no hay que mover el scroll a un lugar sin sentido");
        assertEquals(0, pane.getViewport().getViewPosition().y,
                "sin layout todavia (viewport 0x0) no hay que mover el scroll a un lugar sin sentido");
    }

    /**
     * Defensa en profundidad, mas alla del guard anterior: si algun dia algo mueve el cursor
     * desde otro hilo, la reaccion del canvas (revalidate/repaint/scroll) tiene que llegar por
     * el EDT, nunca en el acto sobre el hilo que llamo.
     */
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
                "todavia no llego al EDT: el scroll de otro hilo no se puede haber aplicado ya");

        releaseEdt.countDown();
        SwingUtilities.invokeAndWait(() -> { });

        assertTrue(pane.getViewport().getViewPosition().x > 0,
                "una vez que el EDT proceso la cola, el scroll real tiene que haber llegado");
    }

    /**
     * Encola en el EDT una tarea que no vuelve hasta que se cuente abajo el latch devuelto:
     * cualquier aviso que otro hilo encole despues queda esperando detras, asi la prueba puede
     * mirar el estado de antes de que ese aviso se procese sin que sea una carrera.
     */
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

    /** Treinta compases en Pantalla Horizontal -que nunca envuelve- para que el ultimo quede
     * bien lejos del origen y un scroll de verdad haga falta para llegar a el. */
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
        return new Editor(new Score("Prueba", 120, List.of(guitar)));
    }

    /** Un JScrollPane real, medido y layouteado sin necesidad de mostrar ninguna ventana. */
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
        return new Editor(new Score("Prueba", 120, List.of(guitar)));
    }

    /**
     * Auditoria de rendimiento, hallazgo 4: cualquier movimiento de cursor disparaba un
     * relayout+repaint completo, sin distinguir "cambio el modelo" de "solo se movio el cursor".
     */
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

        /** El propio constructor de JComponent dispara un repaint (setBackground); lo que
         * importa para estas pruebas es lo que pasa despues, con el lienzo ya armado. */
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

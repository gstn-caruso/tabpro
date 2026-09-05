package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.Map;
import java.util.Optional;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class KeyboardEditingTest {

    private final long[] now = {0L};

    private KeyboardEditing keyboardEditing(Editor editor) {
        return new KeyboardEditing(editor, new FretDigits(() -> now[0]));
    }

    @Test
    void typingADigitWritesTheFret() {
        Editor editor = new Editor(Score.blank());
        keyboardEditing(editor).keyTyped('5');
        assertEquals(Optional.of(new Note(1, 5)), editor.currentBeat().noteOn(1));
    }

    @Test
    void typingTwoDigitsQuicklyWritesATwoDigitFret() {
        Editor editor = new Editor(Score.blank());
        KeyboardEditing keyboard = keyboardEditing(editor);
        keyboard.keyTyped('1');
        now[0] += 100;
        keyboard.keyTyped('2');
        assertEquals(Optional.of(new Note(1, 12)), editor.currentBeat().noteOn(1));
    }

    @Test
    void bindsArrowsToCursorMoves() {
        Editor editor = new Editor(Score.blank());
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();

        bindings.get(KeyStroke.getKeyStroke("RIGHT")).run();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());

        bindings.get(KeyStroke.getKeyStroke("DOWN")).run();
        assertEquals(new Cursor(0, 0, 1, 2), editor.cursor());

        bindings.get(KeyStroke.getKeyStroke("UP")).run();
        assertEquals(new Cursor(0, 0, 1, 1), editor.cursor());

        bindings.get(KeyStroke.getKeyStroke("LEFT")).run();
        assertEquals(new Cursor(0, 0, 0, 1), editor.cursor());
    }

    @Test
    void bindsHomeAndEndToMeasureEdges() {
        Editor editor = new Editor(Score.blank());
        editor.moveRight();
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();

        bindings.get(KeyStroke.getKeyStroke("HOME")).run();
        assertEquals(0, editor.cursor().beat());

        bindings.get(KeyStroke.getKeyStroke("END")).run();
        assertEquals(1, editor.cursor().beat());
    }

    @Test
    void bindsPlusAndMinusToDuration() {
        Editor editor = new Editor(Score.blank());
        KeyboardEditing keyboard = keyboardEditing(editor);
        Duration original = editor.currentBeat().duration();

        keyboard.keyTyped('+');
        assertEquals(original.longer(), editor.currentBeat().duration());

        keyboard.keyTyped('-');
        assertEquals(original, editor.currentBeat().duration());
    }

    @Test
    void bindsPeriodToTheDot() {
        Editor editor = new Editor(Score.blank());
        keyboardEditing(editor).keyTyped('.');
        assertTrue(editor.currentBeat().duration().dotted());
    }

    @Test
    void bindsRToRest() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('r');
        assertTrue(editor.currentBeat().isRest());

        editor.setFret(3);
        keyboard.keyTyped('R');
        assertTrue(editor.currentBeat().isRest());
    }

    @Test
    void bindsBackspaceToClearNote() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();

        bindings.get(KeyStroke.getKeyStroke("BACK_SPACE")).run();

        assertEquals(Optional.empty(), editor.currentBeat().noteOn(1));
    }

    @Test
    void bindsInsertAndDeleteToBeats() {
        Editor editor = new Editor(Score.blank());
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();
        int before = editor.score().track(0).measure(0).beats().size();

        bindings.get(KeyStroke.getKeyStroke("INSERT")).run();
        assertEquals(before + 1, editor.score().track(0).measure(0).beats().size());

        bindings.get(KeyStroke.getKeyStroke("DELETE")).run();
        assertEquals(before, editor.score().track(0).measure(0).beats().size());
    }

    @Test
    void bindsCtrlInsertAndCtrlDeleteToMeasures() {
        Editor editor = new Editor(Score.blank());
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();
        int before = editor.score().track(0).measures().size();

        bindings.get(KeyStroke.getKeyStroke("ctrl INSERT")).run();
        assertEquals(before + 1, editor.score().track(0).measures().size());

        bindings.get(KeyStroke.getKeyStroke("ctrl DELETE")).run();
        assertEquals(before, editor.score().track(0).measures().size());
    }

    @Test
    void bindsCtrlZAndCtrlYToUndoRedo() {
        Editor editor = new Editor(Score.blank());
        KeyboardEditing keyboard = keyboardEditing(editor);
        keyboard.keyTyped('5');
        Map<KeyStroke, Runnable> bindings = keyboard.bindings();

        bindings.get(KeyStroke.getKeyStroke("ctrl Z")).run();
        assertEquals(Optional.empty(), editor.currentBeat().noteOn(1));

        bindings.get(KeyStroke.getKeyStroke("ctrl Y")).run();
        assertEquals(Optional.of(new Note(1, 5)), editor.currentBeat().noteOn(1));
    }

    @Test
    void movingTheCursorResetsTheDigitBuffer() {
        Editor editor = new Editor(Score.blank());
        KeyboardEditing keyboard = keyboardEditing(editor);
        Map<KeyStroke, Runnable> bindings = keyboard.bindings();

        keyboard.keyTyped('2');
        bindings.get(KeyStroke.getKeyStroke("RIGHT")).run();
        now[0] += 100;
        keyboard.keyTyped('0');

        assertEquals(
                Optional.of(new Note(1, 2)), editor.score().track(0).measure(0).beat(0).noteOn(1));
        assertEquals(Optional.of(new Note(1, 0)), editor.currentBeat().noteOn(1));
    }

    @Test
    void aNonDigitKeyResetsTheDigitBuffer() {
        Editor editor = new Editor(Score.blank());
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('1');
        now[0] += 100;
        keyboard.keyTyped('+');
        now[0] += 100;
        keyboard.keyTyped('2');

        assertEquals(Optional.of(new Note(1, 2)), editor.currentBeat().noteOn(1));
    }
}

package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.List;
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
    void bindsBackspaceToClearNote() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        Map<KeyStroke, Runnable> bindings = keyboardEditing(editor).bindings();

        bindings.get(KeyStroke.getKeyStroke("BACK_SPACE")).run();

        assertEquals(Optional.empty(), editor.currentBeat().noteOn(1));
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

    @Test
    void typingTwoDigitsOnAPercussionTrackWritesTheSoundNumber() {
        Editor editor = percussionEditor();
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('4');
        now[0] += 100;
        keyboard.keyTyped('9');

        assertEquals(Optional.of(new Note(1, 49)), editor.currentBeat().noteOn(1));
    }

    @Test
    void typingTheLowestPercussionSoundCombines() {
        Editor editor = percussionEditor();
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('3');
        now[0] += 100;
        keyboard.keyTyped('5');

        assertEquals(
                Optional.of(new Note(1, PercussionKit.LOWEST_SOUND)), editor.currentBeat().noteOn(1));
    }

    @Test
    void typingTheHighestPercussionSoundCombines() {
        Editor editor = percussionEditor();
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('8');
        now[0] += 100;
        keyboard.keyTyped('1');

        assertEquals(
                Optional.of(new Note(1, PercussionKit.HIGHEST_SOUND)), editor.currentBeat().noteOn(1));
    }

    @Test
    void typingBelowTheLowestPercussionSoundStartsANewSoundNumber() {
        Editor editor = percussionEditor();
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('2');
        now[0] += 100;
        keyboard.keyTyped('0');

        assertEquals(Optional.of(new Note(1, 0)), editor.currentBeat().noteOn(1));
    }

    @Test
    void typingAboveTheHighestPercussionSoundStartsANewSoundNumber() {
        Editor editor = percussionEditor();
        KeyboardEditing keyboard = keyboardEditing(editor);

        keyboard.keyTyped('9');
        now[0] += 100;
        keyboard.keyTyped('9');

        assertEquals(Optional.of(new Note(1, 9)), editor.currentBeat().noteOn(1));
    }

    private static Editor percussionEditor() {
        return new Editor(new Score("Cancion", 120, List.of(Track.percussion("Bateria"))));
    }
}

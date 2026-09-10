package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class KeyboardViewTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 92;

    @Test
    void tellsTheWhiteKeysFromTheBlackOnes() {
        assertTrue(KeyboardView.isWhite(60));
        assertTrue(KeyboardView.isWhite(64));
        assertFalse(KeyboardView.isWhite(61));
        assertFalse(KeyboardView.isWhite(66));
    }

    @Test
    void laysTheWhiteKeysOutInOrder() {
        KeyboardView view = sized();

        Rectangle c4 = view.keyBounds(60).orElseThrow();
        Rectangle d4 = view.keyBounds(62).orElseThrow();
        Rectangle c5 = view.keyBounds(72).orElseThrow();

        assertTrue(c4.x < d4.x);
        assertTrue(d4.x < c5.x);
        assertTrue(Math.abs(c4.width - d4.width) <= 1, "las teclas blancas miden casi lo mismo");
    }

    @Test
    void theWhiteKeysTileWithoutGaps() {
        KeyboardView view = sized();

        for (int key = KeyboardView.LOWEST; key < KeyboardView.HIGHEST; key++) {
            if (!KeyboardView.isWhite(key)) {
                continue;
            }
            int next = key + 1;
            while (!KeyboardView.isWhite(next)) {
                next++;
            }
            Rectangle current = view.keyBounds(key).orElseThrow();
            Rectangle following = view.keyBounds(next).orElseThrow();
            assertEquals(
                    current.x + current.width,
                    following.x,
                    "entre la tecla " + key + " y la " + next + " quedo un hueco");
        }
    }

    @Test
    void hangsTheBlackKeysBetweenTheWhiteOnesAndShorter() {
        KeyboardView view = sized();

        Rectangle c4 = view.keyBounds(60).orElseThrow();
        Rectangle cSharp4 = view.keyBounds(61).orElseThrow();
        Rectangle d4 = view.keyBounds(62).orElseThrow();

        assertTrue(cSharp4.x > c4.x && cSharp4.x < d4.x);
        assertTrue(cSharp4.height < c4.height);
        assertTrue(cSharp4.width < c4.width);
    }

    @Test
    void coversTheRangeAGuitarAndABassCanReach() {
        KeyboardView view = sized();

        assertTrue(view.keyBounds(Tuning.standardBass().pitchOfString(4).midiNumber()).isPresent());
        assertTrue(view.keyBounds(new Pitch(64 + Tuning.MAX_FRET).midiNumber()).isPresent());
    }

    @Test
    void hasNoKeyOutsideItsRange() {
        KeyboardView view = sized();

        assertEquals(Optional.empty(), view.keyBounds(0));
        assertEquals(Optional.empty(), view.keyBounds(127));
    }

    @Test
    void pressesTheKeysOfTheBeat() {
        KeyboardView view = sized();
        Beat chord = Beat.of(Duration.quarter(), new Note(6, 0), new Note(1, 0));

        view.show(locationOf(Track.standardGuitar("g"), chord));
        BufferedImage image = paint(view);

        Rectangle lowE = view.keyBounds(40).orElseThrow();
        Rectangle highE = view.keyBounds(64).orElseThrow();
        assertTrue(isPressed(image, lowE), "falta la nota de la sexta cuerda");
        assertTrue(isPressed(image, highE), "falta la nota de la primera cuerda");
    }

    @Test
    void aRestPressesNothing() {
        KeyboardView view = sized();

        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        BufferedImage image = paint(view);

        assertFalse(isPressed(image, view.keyBounds(60).orElseThrow()));
    }

    @Test
    void theMeasureModeMarksTheOtherBeatInTheContextColor() {
        KeyboardView view = sized();
        Track track = Track.standardGuitar("g").withMeasure(0, new Measure(
                TimeSignature.fourFour(),
                List.of(Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 3)))));
        view.show(new BeatLocation(track, 0, VoicePart.LEAD, 0));
        view.setDisplayMode(KeyboardDisplayMode.BEAT_AND_MEASURE);
        BufferedImage image = paint(view);

        Rectangle context = view.keyBounds(67).orElseThrow();
        assertTrue(isMarked(image, context, InstrumentColors.CONTEXT), "el traste del beat siguiente es contexto");
    }

    @Test
    void marksThePressedKeyWithAPointNotTheWholeKey() {
        KeyboardView view = sized();
        view.show(locationOf(Track.standardGuitar("g"), Beat.of(Duration.quarter(), new Note(1, 0))));
        BufferedImage image = paint(view);

        Rectangle key = view.keyBounds(64).orElseThrow();

        assertEquals(InstrumentColors.WHITE_KEY.getRGB(), image.getRGB(key.x + key.width / 2, key.y + 4),
                "el punto no puede llegar hasta arriba de la tecla");
        assertTrue(isPressed(image, key), "el punto tiene que verse cerca de la base de la tecla");
    }

    @Test
    void thePointDoesNotSpanTheWholeWidthOfTheKey() {
        KeyboardView view = sized();
        view.show(locationOf(Track.standardGuitar("g"), Beat.of(Duration.quarter(), new Note(1, 0))));
        BufferedImage image = paint(view);

        Rectangle key = view.keyBounds(64).orElseThrow();
        int dotRow = key.y + key.height - 6;

        assertEquals(InstrumentColors.WHITE_KEY.getRGB(), image.getRGB(key.x + 1, dotRow),
                "el punto no puede llegar hasta el borde izquierdo de la tecla");
    }

    @Test
    void marksAPressedBlackKeyWithAPointThatKeepsItBlackAroundIt() {
        KeyboardView view = sized();
        view.show(locationOf(Track.standardGuitar("g"), Beat.of(Duration.quarter(), new Note(6, 2))));
        BufferedImage image = paint(view);

        Rectangle key = view.keyBounds(42).orElseThrow();

        assertEquals(InstrumentColors.BLACK_KEY.getRGB(), image.getRGB(key.x + key.width / 2, key.y + 2),
                "el punto no puede llegar hasta arriba de la tecla negra");
        assertTrue(isPressed(image, key), "el punto tiene que verse en la tecla negra marcada");
    }

    @Test
    void tracksTheKeyUnderTheMouseWithoutClicking() {
        KeyboardView view = sized();
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        Rectangle c4 = view.keyBounds(60).orElseThrow();
        view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                c4.x + c4.width / 2, c4.y + c4.height - 4, 0, false));

        assertEquals(OptionalInt.of(60), view.hoveredKey());
    }

    @Test
    void readsBackTheKeyYouPointAt() {
        KeyboardView view = sized();

        for (int key = KeyboardView.LOWEST; key <= KeyboardView.HIGHEST; key++) {
            Rectangle bounds = view.keyBounds(key).orElseThrow();
            int x = bounds.x + bounds.width / 2;
            int y = KeyboardView.isWhite(key) ? bounds.y + bounds.height - 4 : bounds.y + bounds.height / 2;

            assertEquals(OptionalInt.of(key), view.keyAt(x, y), "la tecla " + key);
        }
    }

    @Test
    void theBlackKeyWinsWhereItSitsOverTheWhiteOne() {
        KeyboardView view = sized();
        Rectangle c4 = view.keyBounds(60).orElseThrow();
        Rectangle cSharp4 = view.keyBounds(61).orElseThrow();
        int whereTheyOverlap = c4.x + c4.width - 2;

        assertEquals(OptionalInt.of(61), view.keyAt(whereTheyOverlap, cSharp4.y + cSharp4.height / 2));
    }

    @Test
    void underTheBlackKeyTheWhiteOneShowsAgain() {
        KeyboardView view = sized();
        Rectangle c4 = view.keyBounds(60).orElseThrow();
        int whereTheyOverlap = c4.x + c4.width - 2;

        assertEquals(OptionalInt.of(60), view.keyAt(whereTheyOverlap, c4.y + c4.height - 4));
    }

    @Test
    void hasNoKeyOffTheKeyboard() {
        KeyboardView view = sized();

        assertEquals(OptionalInt.empty(), view.keyAt(0, HEIGHT / 2));
        assertEquals(OptionalInt.empty(), view.keyAt(WIDTH / 2, 0));
        assertEquals(OptionalInt.empty(), view.keyAt(WIDTH / 2, HEIGHT - 1));
    }

    @Test
    void theRightArrowKeyMovesTheCaretToTheNextSemitone() {
        KeyboardView view = sized();

        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(OptionalInt.of(KeyboardView.LOWEST + 1), view.caretKey());
    }

    @Test
    void theLeftArrowKeyMovesTheCaretToThePreviousSemitone() {
        KeyboardView view = sized();
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(view, KeyStroke.getKeyStroke("LEFT"));

        assertEquals(OptionalInt.of(KeyboardView.LOWEST + 1), view.caretKey());
    }

    @Test
    void theEnterKeyNotifiesTheKeyUnderTheCaret() {
        KeyboardView view = sized();
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));
        List<Integer> activated = new java.util.ArrayList<>();
        view.onCaretActivated(activated::add);

        pressShortcut(view, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(List.of(KeyboardView.LOWEST + 1), activated);
    }

    @Test
    void theSpaceKeyAlsoNotifiesTheKeyUnderTheCaret() {
        KeyboardView view = sized();
        List<Integer> activated = new java.util.ArrayList<>();
        view.onCaretActivated(activated::add);

        pressShortcut(view, KeyStroke.getKeyStroke("SPACE"));

        assertEquals(List.of(KeyboardView.LOWEST), activated);
    }

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        KeyboardView view = sized();
        BufferedImage withoutFocus = paint(view);

        gainFocus(view);
        BufferedImage withFocus = paint(view);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(KeyboardView view) {
        for (var listener : view.getFocusListeners()) {
            listener.focusGained(new FocusEvent(view, FocusEvent.FOCUS_GAINED));
        }
    }

    private static boolean differsSomewhere(BufferedImage a, BufferedImage b) {
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void theAccessibleDescriptionNamesTheKeyUnderTheCaret() {
        KeyboardView view = sized();

        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals("A#0", view.getAccessibleContext().getAccessibleDescription());
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    private static BeatLocation locationOf(Track track, Beat beat) {
        Track updated = track.withMeasure(0, new Measure(TimeSignature.fourFour(), List.of(beat)));
        return new BeatLocation(updated, 0, VoicePart.LEAD, 0);
    }

    private static KeyboardView sized() {
        KeyboardView view = new KeyboardView();
        view.setSize(WIDTH, HEIGHT);
        return view;
    }

    private static BufferedImage paint(KeyboardView view) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    private static boolean isPressed(BufferedImage image, Rectangle key) {
        return isMarked(image, key, InstrumentColors.PRESSED);
    }

    private static boolean isMarked(BufferedImage image, Rectangle key, java.awt.Color color) {
        int x = key.x + key.width / 2;
        int y = key.y + key.height - 6;
        return image.getRGB(x, y) == color.getRGB();
    }
}

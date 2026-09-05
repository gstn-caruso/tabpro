package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.OptionalInt;
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

        view.show(Tuning.standard(), chord);
        BufferedImage image = paint(view);

        Rectangle lowE = view.keyBounds(40).orElseThrow();
        Rectangle highE = view.keyBounds(64).orElseThrow();
        assertTrue(isPressed(image, lowE), "falta la nota de la sexta cuerda");
        assertTrue(isPressed(image, highE), "falta la nota de la primera cuerda");
    }

    @Test
    void aRestPressesNothing() {
        KeyboardView view = sized();

        view.show(Tuning.standard(), Beat.rest(Duration.quarter()));
        BufferedImage image = paint(view);

        assertFalse(isPressed(image, view.keyBounds(60).orElseThrow()));
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
        int x = key.x + key.width / 2;
        int y = key.y + key.height - 6;
        return image.getRGB(x, y) == InstrumentColors.PRESSED.getRGB();
    }
}

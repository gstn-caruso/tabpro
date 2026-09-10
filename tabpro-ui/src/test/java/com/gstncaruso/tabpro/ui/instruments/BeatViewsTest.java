package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.AwaitEdt;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class BeatViewsTest {

    @Test
    void closingTheFretboardTitleBarHidesTheFretboard() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        findButtonNamed(views, "Cerrar diapasón").orElseThrow().doClick();

        assertFalse(views.isFretboardVisible());
    }

    @Test
    void closingTheKeyboardTitleBarHidesTheKeyboard() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        findButtonNamed(views, "Cerrar teclado").orElseThrow().doClick();

        assertFalse(views.isKeyboardVisible());
    }

    @Test
    void theCloseButtonsShowAnIconNotATextGlyph() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        JButton close = findButtonNamed(views, "Cerrar diapasón").orElseThrow();

        assertTrue(close.getIcon() != null, "el boton de cerrar tiene que mostrar un icono, no un caracter");
    }

    @Test
    void theFretboardCloseButtonCanBeRewiredToTheSameCommandAsTheMenu() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());
        java.util.concurrent.atomic.AtomicBoolean invoked = new java.util.concurrent.atomic.AtomicBoolean(false);
        views.setOnCloseFretboard(() -> invoked.set(true));

        findButtonNamed(views, "Cerrar diapasón").orElseThrow().doClick();

        assertTrue(invoked.get(), "el boton tiene que disparar el comando que le paso quien lo cablea");
    }

    @Test
    void theKeyboardCloseButtonCanBeRewiredToTheSameCommandAsTheMenu() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());
        java.util.concurrent.atomic.AtomicBoolean invoked = new java.util.concurrent.atomic.AtomicBoolean(false);
        views.setOnCloseKeyboard(() -> invoked.set(true));

        findButtonNamed(views, "Cerrar teclado").orElseThrow().doClick();

        assertTrue(invoked.get(), "el boton tiene que disparar el comando que le paso quien lo cablea");
    }

    @Test
    void theFretboardAndTheKeyboardEachHaveTheirOwnTitleBand() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        assertEquals(2, opaquePanelsBackedBy(views, ScoreColors.TITLE_BAR).size());
    }

    private static List<JPanel> opaquePanelsBackedBy(Container container, java.awt.Color background) {
        List<JPanel> found = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel panel && panel.isOpaque() && background.equals(panel.getBackground())) {
                found.add(panel);
            }
            if (component instanceof Container nested) {
                found.addAll(opaquePanelsBackedBy(nested, background));
            }
        }
        return found;
    }

    private static java.util.Optional<JButton> findButtonNamed(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button
                    && name.equals(button.getAccessibleContext().getAccessibleName())) {
                return java.util.Optional.of(button);
            }
            if (component instanceof Container nested) {
                java.util.Optional<JButton> found = findButtonNamed(nested, name);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return java.util.Optional.empty();
    }

    @Test
    void ningunControlDelDiapasonNiDelTecladoQuedaSinNombreNiTooltipAccesible() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        AccessibilityAssertions.assertNoViolations(views);
    }

    @Test
    void showsTheBeatUnderTheCursorWhileNothingSounds() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(7);

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, Playhead.silent()));
    }

    @Test
    void showsTheBeatThatSoundsWhileItPlays() {
        Editor editor = new Editor(Score.blank());
        editor.setFret(3);
        editor.moveRight();
        editor.setFret(5);
        editor.moveTo(0, 0, 1);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(0, 0, 1));

        assertEquals(editor.currentTrack().measure(0).beat(1), BeatViews.beatToShow(editor, playhead));
    }

    @Test
    void followsTheTrackTheCursorIsOnAndNotAnotherOne() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.setFret(9);
        editor.selectTrack(0);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, playhead));
    }

    @Test
    void fallsBackToTheCursorWhenThePlayheadPointsPastTheScore() {
        Editor editor = new Editor(Score.blank());

        Playhead stale = Playhead.silent().advancedTo(new BeatPosition(0, 9, 9));

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, stale));
    }

    @Test
    void letsYouWriteWhileNothingSounds() {
        Editor editor = new Editor(Score.blank());

        assertTrue(BeatViews.showsTheCursorBeat(editor, Playhead.silent()));
    }

    @Test
    void doesNotLetYouWriteOnABeatYouAreNotSeeing() {
        Editor editor = new Editor(Score.blank());
        editor.moveRight();

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(0, 0, 0));

        assertFalse(
                BeatViews.showsTheCursorBeat(editor, playhead),
                "mientras suena se ve el beat que suena, no el del cursor");
    }

    @Test
    void letsYouWriteWhileAnotherTrackSounds() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        editor.selectTrack(0);

        Playhead playhead = Playhead.silent().advancedTo(new BeatPosition(1, 0, 0));

        assertTrue(BeatViews.showsTheCursorBeat(editor, playhead));
    }

    @Test
    void usesTheTuningOfTheTrackTheCursorIsOn() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));

        assertEquals(4, BeatViews.tuningToShow(editor).stringCount());

        editor.selectTrack(0);

        assertEquals(6, BeatViews.tuningToShow(editor).stringCount());
    }

    @Test
    void bothViewsCanBeHidden() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());

        assertTrue(views.isFretboardVisible());
        assertTrue(views.isKeyboardVisible());

        views.setFretboardVisible(false);
        views.setKeyboardVisible(false);

        assertFalse(views.isFretboardVisible());
        assertFalse(views.isKeyboardVisible());
    }

    @Test
    void preparingForTheScalesToolShowsTheKeyboardAndSwitchesBothViewsToScaleMode() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());
        views.setKeyboardVisible(false);

        views.prepareForScalesTool();

        assertTrue(views.isKeyboardVisible(), "el manual dice que abrir la herramienta abre el teclado solo");
        assertEquals(FretboardDisplayMode.BEAT_AND_SCALE, views.fretboard().displayMode());
        assertEquals(KeyboardDisplayMode.BEAT_AND_SCALE, views.keyboard().displayMode());
    }

    @Test
    void preparingForTheScalesToolDoesNotForceTheFretboardOpen() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());
        views.setFretboardVisible(false);

        views.prepareForScalesTool();

        assertFalse(
                views.isFretboardVisible(),
                "el manual pide usar View > Fretboard antes: la herramienta no lo fuerza");
    }

    @Test
    void clickingSeveralKeysBuildsTheChordOfTheBeat() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());

        clickKey(views, 60);
        clickKey(views, 64);
        clickKey(views, 67);

        assertEquals(
                List.of(60, 64, 67),
                editor.currentBeat().notes().stream()
                        .map(note -> editor.currentTrack().tuning().pitchOf(note).midiNumber())
                        .sorted()
                        .toList(),
                "las tres teclas suenan juntas en el mismo beat");
    }

    @Test
    void clickingTheSameKeyAgainTakesItOutOfTheChord() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());
        clickKey(views, 60);
        clickKey(views, 64);

        clickKey(views, 64);

        assertEquals(1, editor.currentBeat().notes().size());
    }

    @Test
    void pressingEnterOnTheFretboardWritesTheNoteUnderTheCaretLikeAClickWould() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());
        FretboardView fretboard = views.fretboard();
        fretboard.setSize(900, FretboardView.PREFERRED_HEIGHT);
        pressShortcut(fretboard, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(fretboard, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(List.of(new Note(1, 1)), editor.currentBeat().notes());
    }

    @Test
    void pressingEnterOnTheKeyboardWritesTheKeyUnderTheCaretLikeAClickWould() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());
        KeyboardView keyboard = views.keyboard();
        keyboard.setSize(900, KeyboardView.PREFERRED_HEIGHT);
        moveCaretTo(keyboard, 60);

        pressShortcut(keyboard, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(
                List.of(60),
                editor.currentBeat().notes().stream()
                        .map(note -> editor.currentTrack().tuning().pitchOf(note).midiNumber())
                        .toList());
    }

    private static void moveCaretTo(KeyboardView keyboard, int midiNumber) {
        for (int key = KeyboardView.LOWEST; key < midiNumber; key++) {
            pressShortcut(keyboard, KeyStroke.getKeyStroke("RIGHT"));
        }
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    /** El gesto real: un clic en el centro de esa tecla del piano. */
    private static void clickKey(BeatViews views, int midiNumber) {
        KeyboardView keyboard = views.keyboard();
        keyboard.setSize(900, 92);
        Rectangle key = keyboard.keyBounds(midiNumber).orElseThrow();
        keyboard.dispatchEvent(new MouseEvent(
                keyboard, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0,
                key.x + key.width / 2, key.y + key.height - 4, 1, false, MouseEvent.BUTTON1));
    }

    @Test
    void theHandednessButtonFlipsTheFretboardWithAnIcon() {
        BeatViews views = new BeatViews(new Editor(Score.blank()), new RecordingPlayer());
        JToggleButton toggle = findHandednessToggle(views)
                .orElseThrow(() -> new AssertionError("no encontre el boton de zurdo"));

        toggle.doClick();

        assertEquals(Handedness.LEFT_HANDED, views.fretboard().handedness());
        assertTrue(toggle.getIcon() != null, "el boton de zurdo tiene que mostrar un icono, no una casilla de texto");
    }

    private static java.util.Optional<JToggleButton> findHandednessToggle(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JToggleButton toggle) {
                return java.util.Optional.of(toggle);
            }
            if (component instanceof Container nested) {
                java.util.Optional<JToggleButton> found = findHandednessToggle(nested);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return java.util.Optional.empty();
    }

    @Test
    void followsTheEditorWithoutBlowingUp() {
        Editor editor = new Editor(Score.blank());
        BeatViews views = new BeatViews(editor, new RecordingPlayer());

        editor.setFret(Tuning.MAX_FRET);
        editor.addTrack(Track.standardBass("Bajo"));
        editor.setFret(4);
        views.showPlayhead(Playhead.silent().advancedTo(new BeatPosition(1, 0, 0)));
        AwaitEdt.flush();

        assertEquals(editor.currentBeat(), BeatViews.beatToShow(editor, Playhead.silent()));
    }
}

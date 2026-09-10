package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

class FretboardViewTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 120;

    @Test
    void spreadsTheStringsFromTheHighestAtTheTop() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        assertTrue(view.stringY(1) < view.stringY(6), "la primera cuerda va arriba");
        assertEquals(
                view.stringY(2) - view.stringY(1),
                view.stringY(6) - view.stringY(5),
                "las cuerdas van parejas");
    }

    @Test
    void aBassSpreadsItsFourStringsOverTheSameNeck() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardBass("b"), Beat.rest(Duration.quarter())));

        assertEquals(4, view.stringCount());
        assertTrue(view.stringY(4) > view.stringY(1));
    }

    @Test
    void putsTheOpenStringsBeforeTheNut() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        assertTrue(view.fretCenterX(0) < view.nutX());
        assertTrue(view.fretCenterX(1) > view.nutX());
    }

    @Test
    void placesEachFretAfterTheOneBefore() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        for (int fret = 1; fret < view.fretCount(); fret++) {
            assertTrue(
                    view.fretCenterX(fret) < view.fretCenterX(fret + 1),
                    "el traste " + fret + " tiene que estar antes que el siguiente");
        }
    }

    @Test
    void reachesTheLastFretOfAStandardInstrument() {
        FretboardView view = new FretboardView();

        assertEquals(TrackSettings.DEFAULT_FRET_COUNT, view.fretCount());
    }

    @Test
    void respectsTheFretCountOfTheActiveTrack() {
        FretboardView view = sized(new FretboardView());
        Track shortNeck = Track.standardGuitar("g").mappingSettings(settings -> settings.withFretCount(12));
        view.show(locationOf(shortNeck, Beat.rest(Duration.quarter())));

        assertEquals(12, view.fretCount());
        assertEquals(Optional.empty(), view.noteAt(view.fretCenterX(12) + 40, view.stringY(1)));
    }

    @Test
    void namesANoteWithTheCapoAlreadyAdded() {
        FretboardView view = sized(new FretboardView());
        Track capoed = Track.standardGuitar("g").mappingSettings(settings -> settings.withCapo(2));
        view.show(locationOf(capoed, Beat.rest(Duration.quarter())));

        assertEquals("F#", view.labelFor(new FretPosition(1, 0)));
    }

    @Test
    void scaleNotesShowTheirNameByDefault() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setDisplayMode(FretboardDisplayMode.BEAT_AND_SCALE);
        view.setScale(Scale.cMajor());

        assertEquals("E", view.labelFor(new FretPosition(1, 0), MarkKind.SECONDARY));
    }

    @Test
    void scaleNotesShowTheIntervalWhenAskedTo() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setDisplayMode(FretboardDisplayMode.BEAT_AND_SCALE);
        view.setScale(Scale.cMajor());
        view.setScaleLabelMode(ScaleLabelMode.INTERVAL);

        assertEquals("3", view.labelFor(new FretPosition(1, 0), MarkKind.SECONDARY));
    }

    @Test
    void scaleNotesShowTheDegreeWhenAskedTo() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setDisplayMode(FretboardDisplayMode.BEAT_AND_SCALE);
        view.setScale(Scale.cMajor());
        view.setScaleLabelMode(ScaleLabelMode.DEGREE);

        assertEquals("3", view.labelFor(new FretPosition(1, 0), MarkKind.SECONDARY));
    }

    @Test
    void primaryMarksAlwaysShowTheirNameEvenInScaleMode() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setDisplayMode(FretboardDisplayMode.BEAT_AND_SCALE);
        view.setScale(Scale.cMajor());
        view.setScaleLabelMode(ScaleLabelMode.DEGREE);

        assertEquals("E", view.labelFor(new FretPosition(1, 0), MarkKind.PRIMARY));
    }

    @Test
    void outsideScaleModeTheLabelIsAlwaysTheName() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setScale(Scale.cMajor());
        view.setScaleLabelMode(ScaleLabelMode.DEGREE);

        assertEquals("E", view.labelFor(new FretPosition(1, 0), MarkKind.SECONDARY));
    }

    @Test
    void marksTheNotesOfTheBeatAndNothingElse() {
        FretboardView view = sized(new FretboardView());
        Beat chord = Beat.of(Duration.quarter(), new Note(6, 3), new Note(1, 0));
        view.show(locationOf(Track.standardGuitar("g"), chord));
        BufferedImage image = paint(view);

        assertTrue(hasMarkNear(image, view.fretCenterX(3), view.stringY(6)), "falta la nota del traste 3");
        assertTrue(hasMarkNear(image, view.fretCenterX(0), view.stringY(1)), "falta la cuerda al aire");
        assertTrue(!hasMarkNear(image, view.fretCenterX(7), view.stringY(4)), "marco una nota que no suena");
    }

    @Test
    void aRestLeavesTheNeckBare() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        BufferedImage image = paint(view);

        for (int string = 1; string <= 6; string++) {
            assertTrue(
                    !hasMarkNear(image, view.fretCenterX(5), view.stringY(string)),
                    "un silencio no tiene que marcar nada");
        }
    }

    @Test
    void theMeasureModeMarksTheOtherBeatsInTheContextColor() {
        FretboardView view = sized(new FretboardView());
        Track track = Track.standardGuitar("g").withMeasure(0, new Measure(
                TimeSignature.fourFour(),
                List.of(Beat.of(Duration.quarter(), new Note(1, 0)), Beat.of(Duration.quarter(), new Note(1, 5)))));
        view.show(new BeatLocation(track, 0, VoicePart.LEAD, 0));
        view.setDisplayMode(FretboardDisplayMode.BEAT_AND_MEASURE);
        BufferedImage image = paint(view);

        assertTrue(hasMark(image, view.fretCenterX(5), view.stringY(1), InstrumentColors.CONTEXT),
                "el beat siguiente tiene que verse distinto, como contexto");
    }

    @Test
    void readsBackTheNoteUnderEachFretAndString() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        for (int string = 1; string <= 6; string++) {
            for (int fret = 0; fret <= view.fretCount(); fret++) {
                assertEquals(
                        Optional.of(new Note(string, fret)),
                        view.noteAt(view.fretCenterX(fret), view.stringY(string)),
                        "el punto de la cuerda " + string + " traste " + fret);
            }
        }
    }

    @Test
    void hasNoNoteOffTheNeck() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        assertEquals(Optional.empty(), view.noteAt(view.fretCenterX(5), 0));
        assertEquals(Optional.empty(), view.noteAt(view.fretCenterX(5), HEIGHT - 1));
    }

    @Test
    void hasNoNoteBeyondTheLastFret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        assertEquals(Optional.empty(), view.noteAt(WIDTH - 1, view.stringY(1)));
    }

    @Test
    void hasNoNoteBeforeTheOpenStrings() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        assertEquals(Optional.empty(), view.noteAt(0, view.stringY(1)));
    }

    @Test
    void aBassStopsAtItsFourthString() {
        FretboardView bass = sized(new FretboardView());
        bass.show(locationOf(Track.standardBass("b"), Beat.rest(Duration.quarter())));

        int lastString = bass.stringY(4);
        int aWholeStringFurtherDown = lastString + (lastString - bass.stringY(3));

        assertEquals(Optional.of(new Note(4, 3)), bass.noteAt(bass.fretCenterX(3), lastString));
        assertEquals(Optional.empty(), bass.noteAt(bass.fretCenterX(3), aWholeStringFurtherDown));
    }

    @Test
    void leftHandedFlipsTheNeckAroundWithoutLosingAnyFret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.setHandedness(Handedness.LEFT_HANDED);

        assertTrue(view.fretCenterX(0) > view.fretCenterX(view.fretCount()), "el diapason quedo al reves");
        assertEquals(
                Optional.of(new Note(1, 3)),
                view.noteAt(view.fretCenterX(3), view.stringY(1)),
                "el clic tiene que seguir cayendo sobre el mismo traste, ya espejado");
    }

    @Test
    void tracksTheNoteUnderTheMouseWithoutClicking() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                view.fretCenterX(4), view.stringY(2), 0, false));

        assertEquals(Optional.of(new Note(2, 4)), view.hoveredNote());
    }

    @Test
    void losesTheHoverWhenTheMouseLeaves() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
                view.fretCenterX(4), view.stringY(2), 0, false));

        view.dispatchEvent(new MouseEvent(view, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0,
                -1, -1, 0, false));

        assertEquals(Optional.empty(), view.hoveredNote());
    }

    @Test
    void theRightArrowKeyMovesTheCaretToTheNextFret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals(Optional.of(new Note(1, 1)), view.caretNote());
    }

    @Test
    void theLeftArrowKeyMovesTheCaretToThePreviousFret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        pressShortcut(view, KeyStroke.getKeyStroke("LEFT"));

        assertEquals(Optional.of(new Note(1, 1)), view.caretNote());
    }

    @Test
    void theDownArrowKeyMovesTheCaretToTheNextString() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        pressShortcut(view, KeyStroke.getKeyStroke("DOWN"));

        assertEquals(Optional.of(new Note(2, 0)), view.caretNote());
    }

    @Test
    void theUpArrowKeyMovesTheCaretToThePreviousString() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        pressShortcut(view, KeyStroke.getKeyStroke("DOWN"));
        pressShortcut(view, KeyStroke.getKeyStroke("DOWN"));

        pressShortcut(view, KeyStroke.getKeyStroke("UP"));

        assertEquals(Optional.of(new Note(2, 0)), view.caretNote());
    }

    @Test
    void theEnterKeyNotifiesTheNoteUnderTheCaret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));
        List<Note> activated = new java.util.ArrayList<>();
        view.onCaretActivated(activated::add);

        pressShortcut(view, KeyStroke.getKeyStroke("ENTER"));

        assertEquals(List.of(new Note(1, 1)), activated);
    }

    @Test
    void theSpaceKeyAlsoNotifiesTheNoteUnderTheCaret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        List<Note> activated = new java.util.ArrayList<>();
        view.onCaretActivated(activated::add);

        pressShortcut(view, KeyStroke.getKeyStroke("SPACE"));

        assertEquals(List.of(new Note(1, 0)), activated);
    }

    @Test
    void paintsAVisibleCaretRingWhenItGetsFocus() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        BufferedImage withoutFocus = paint(view);

        gainFocus(view);
        BufferedImage withFocus = paint(view);

        assertTrue(differsSomewhere(withoutFocus, withFocus), "el foco tiene que verse en el dibujo");
    }

    private static void gainFocus(FretboardView view) {
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
    void theAccessibleDescriptionNamesTheNoteUnderTheCaret() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        pressShortcut(view, KeyStroke.getKeyStroke("RIGHT"));

        assertEquals("F", view.getAccessibleContext().getAccessibleDescription());
    }

    @Test
    void theNeckShowsAWoodGrainPattern() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        BufferedImage image = paint(view);

        int x = view.fretCenterX(2);
        boolean sawTheGrain = false;
        for (int y = view.stringY(1) + 3; y <= view.stringY(2) - 3; y++) {
            if (image.getRGB(x, y) != FretboardType.ELECTRIC.woodColor().getRGB()) {
                sawTheGrain = true;
                break;
            }
        }
        assertTrue(sawTheGrain, "el mastil tiene que mostrar veta, no un color plano");
    }

    @Test
    void theWoodGrainNeverChangesBetweenRepaints() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));

        BufferedImage first = paint(view);
        BufferedImage second = paint(view);

        assertTrue(!differsSomewhere(first, second), "la veta tiene que ser reproducible, no aleatoria");
    }

    @Test
    void theFretWiresLookMetallicWithAHighlightAndAShadow() {
        FretboardView view = sized(new FretboardView());
        view.show(locationOf(Track.standardGuitar("g"), Beat.rest(Duration.quarter())));
        BufferedImage image = paint(view);

        int wireX = (view.fretCenterX(5) + view.fretCenterX(6)) / 2;
        int y = (view.stringY(3) + view.stringY(4)) / 2;
        Color fretWire = FretboardType.ELECTRIC.fretWireColor();

        assertEquals(fretWire.brighter().getRGB(), image.getRGB(wireX - 1, y), "falta el brillo del traste");
        assertEquals(fretWire.getRGB(), image.getRGB(wireX, y), "el cuerpo del traste tiene que seguir igual");
        assertEquals(fretWire.darker().getRGB(), image.getRGB(wireX + 1, y), "falta la sombra del traste");
    }

    private static void pressShortcut(JComponent component, KeyStroke keyStroke) {
        Object name = component.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        component.getActionMap().get(name).actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, ""));
    }

    private static BeatLocation locationOf(Track track, Beat beat) {
        Track updated = track.withMeasure(0, new Measure(TimeSignature.fourFour(), List.of(beat)));
        return new BeatLocation(updated, 0, VoicePart.LEAD, 0);
    }

    private static FretboardView sized(FretboardView view) {
        view.setSize(WIDTH, HEIGHT);
        return view;
    }

    private static BufferedImage paint(FretboardView view) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    private static boolean hasMarkNear(BufferedImage image, int x, int y) {
        return hasMark(image, x, y, InstrumentColors.PRESSED);
    }

    private static boolean hasMark(BufferedImage image, int x, int y, Color mark) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int px = x + dx;
                int py = y + dy;
                if (px >= 0 && py >= 0 && px < image.getWidth() && py < image.getHeight()
                        && image.getRGB(px, py) == mark.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }
}

package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El manual, linea 923: el agrupamiento por barra de union y la direccion de la plica son
 * automaticos, pero "es posible cambiar[los] a mano... usando el menu Nota". Lo que importa aca
 * no es que el override haya quedado guardado (eso lo prueba JsonScoreFilesTest) sino que de
 * verdad cambia el dibujo, igual que OctaveMarkPaintingTest para la marca de octava.
 */
class BeamBreakAndStemOverridePaintingTest {

    private static final int WIDTH = 900;
    /** SPACE*3.4 (ver StaffPainter.STEM_LENGTH); alcanza como aproximacion, el radio de busqueda
     * de pixeles perdona el redondeo. */
    private static final int STEM_LENGTH = 27;

    // Grado 4 (la linea del medio): la plica automatica apunta para abajo, y con ese largo de
    // plica la barra cae bien adentro del hueco entre el pentagrama y la tablatura -lejos de
    // cualquier linea, asi que la unica tinta posible ahi es la barra misma.
    private static final Note MIDDLE_NOTE = new Note(2, 0);
    private static final int BEAM_SEARCH_RADIUS = 4;

    @Test
    void forcingABeamBreakEndsTheConnectingBeamBetweenTwoNotesThatWouldOtherwiseShareOne() {
        Painted plain = paintEighthsWithOverrideAt(1, BeamBreak.AUTOMATIC);
        Painted forced = paintEighthsWithOverrideAt(1, BeamBreak.FORCED);

        assertFalse(plain.looksLike(forced), "forzar el corte tiene que cambiar la hoja pintada");

        int midX = plain.midXBetween(0, 1);
        int y = plain.stemTipBelow(MIDDLE_NOTE);
        assertTrue(plain.hasInkNear(midX, y, BEAM_SEARCH_RADIUS), "sin forzar, la barra conecta el primer par de corcheas");
        assertFalse(forced.hasInkNear(midX, y, BEAM_SEARCH_RADIUS),
                "forzado, ya no tiene que quedar barra entre esas dos corcheas");
    }

    @Test
    void preventingABeamBreakJoinsTwoGroupsAcrossABeatBoundary() {
        Painted plain = paintEighthsWithOverrideAt(2, BeamBreak.AUTOMATIC);
        Painted prevented = paintEighthsWithOverrideAt(2, BeamBreak.PREVENTED);

        assertFalse(plain.looksLike(prevented), "impedir el corte tiene que cambiar la hoja pintada");

        int midX = plain.midXBetween(1, 2);
        int y = plain.stemTipBelow(MIDDLE_NOTE);
        assertFalse(plain.hasInkNear(midX, y, BEAM_SEARCH_RADIUS),
                "automaticamente el primer y el segundo par no comparten barra");
        assertTrue(prevented.hasInkNear(midX, y, BEAM_SEARCH_RADIUS), "impedido, el corte entre esos dos pares desaparece");
    }

    @Test
    void forcingTheStemUpFlipsAStemThatWouldPointDownAutomatically() {
        Painted automatic = paintSingleQuarter(StemOverride.AUTOMATIC);
        Painted forcedUp = paintSingleQuarter(StemOverride.UP);

        assertFalse(automatic.looksLike(forcedUp), "forzar la plica tiene que cambiar la hoja pintada");

        // La plica se ata a un costado de la cabeza de la nota, no a su centro (NOTE_WIDTH/2 mas
        // el margen de StaffPainter.stemOf), asi que el radio de busqueda tiene que cubrir ese
        // corrimiento horizontal ademas del vertical.
        int x = automatic.noteX(0);
        assertTrue(automatic.hasInkNear(x, automatic.stemTipBelow(MIDDLE_NOTE), 6),
                "automatica, la plica de esta nota apunta para abajo");
        assertFalse(automatic.hasInkNear(x, automatic.stemTipAbove(MIDDLE_NOTE), 6),
                "automatica, no hay nada arriba de la nota");

        assertTrue(forcedUp.hasInkNear(x, forcedUp.stemTipAbove(MIDDLE_NOTE), 6),
                "forzada para arriba, la plica ahora tiene que apuntar para arriba");
        assertFalse(forcedUp.hasInkNear(x, forcedUp.stemTipBelow(MIDDLE_NOTE), 6),
                "forzada para arriba, ya no puede quedar nada abajo de la nota");
    }

    /** Ocho corcheas llenan el compas de 4/4 -dos pares automaticos por cada mitad- con el
     * override puesto en un solo beat, para aislar su efecto. */
    private static Painted paintEighthsWithOverrideAt(int beatIndex, BeamBreak beamBreak) {
        Duration eighth = new Duration(NoteValue.EIGHTH, false);
        Beat[] beats = new Beat[8];
        for (int i = 0; i < beats.length; i++) {
            Beat beat = Beat.of(eighth, MIDDLE_NOTE);
            if (i == beatIndex && beamBreak != BeamBreak.AUTOMATIC) {
                beat = beat.withEffects(BeatEffects.none().withBeamBreak(beamBreak));
            }
            beats[i] = beat;
        }
        return paint(new Measure(TimeSignature.fourFour(), List.of(beats)));
    }

    private static Painted paintSingleQuarter(StemOverride stemOverride) {
        Beat first = Beat.of(Duration.quarter(), MIDDLE_NOTE)
                .withEffects(BeatEffects.none().withStemOverride(stemOverride));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                first, Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        return paint(measure);
    }

    private static Painted paint(Measure measure) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));

        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(-1, 0, 0, 1), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        int noteX(int beatIndex) {
            Rectangle beat = layout.beatBounds(0, 0, beatIndex);
            return beat.x + beat.width / 2;
        }

        int midXBetween(int firstBeatIndex, int secondBeatIndex) {
            return (noteX(firstBeatIndex) + noteX(secondBeatIndex)) / 2;
        }

        int stemTipAbove(Note note) {
            return layout.stepY(0, 0, stepOf(note)) - STEM_LENGTH;
        }

        int stemTipBelow(Note note) {
            return layout.stepY(0, 0, stepOf(note)) + STEM_LENGTH;
        }

        private int stepOf(Note note) {
            return StaffPosition.of(Tuning.standard().pitchOf(note), Clef.TREBLE).step();
        }

        boolean hasInkNear(int x, int y, int radius) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (isInside(x + dx, y + dy)
                            && image.getRGB(x + dx, y + dy) != ScoreColors.BACKGROUND.getRGB()) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean looksLike(Painted other) {
            if (image.getWidth() != other.image.getWidth() || image.getHeight() != other.image.getHeight()) {
                return false;
            }
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(x, y) != other.image.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isInside(int x, int y) {
            return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
        }
    }
}

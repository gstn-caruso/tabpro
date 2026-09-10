package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.instruments.FretboardType;
import com.gstncaruso.tabpro.ui.instruments.InstrumentColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck;
import com.gstncaruso.tabpro.ui.theme.PaletteCheck.Pair;
import com.gstncaruso.tabpro.ui.tracks.TrackColors;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * La paleta oscura: la partitura, el panel de pistas, el diapason y el teclado se ven siempre
 * sobre fondo oscuro, la elija o no el tema de la ventana. {@code ChordDiagramColorsContrastTest}
 * cubre el diagrama de acorde, que vive en otro paquete por su propio fondo package-private.
 */
class ScoreColorsContrastTest {

    @Test
    void everyMarkOfTheDarkPaletteReadsOverItsBackground() {
        PaletteCheck.assertEveryPairReads(pairs());
    }

    private static List<Pair> pairs() {
        List<Pair> pairs = new ArrayList<>(List.of(
                new Pair("tinta (INK) / fondo", ScoreColors.INK, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("etiqueta (LABEL) / fondo", ScoreColors.LABEL, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("tinta atenuada (MUTED_INK) / fondo", ScoreColors.MUTED_INK, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("voz inactiva (VOICE_INACTIVE) / fondo", ScoreColors.VOICE_INACTIVE, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("etiqueta (LABEL) / panel de pistas", ScoreColors.LABEL, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("tinta atenuada (MUTED_INK) / panel de pistas", ScoreColors.MUTED_INK, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("aviso (WARNING) / fondo de la partitura", ScoreColors.WARNING, ScoreColors.BACKGROUND,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("aviso (WARNING) / panel de pistas", ScoreColors.WARNING, ScoreColors.SURFACE,
                        Contrast.TEXT_MINIMUM_RATIO),
                new Pair("linea del pentagrama (STAFF_LINE) / fondo", ScoreColors.STAFF_LINE, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("linea de compas (BAR_LINE) / fondo", ScoreColors.BAR_LINE, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("linea del pentagrama (STAFF_LINE) / panel de percusion", ScoreColors.STAFF_LINE,
                        ScoreColors.SURFACE, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde (BORDER) / panel de pistas", ScoreColors.BORDER, ScoreColors.SURFACE,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cursor de edicion (CURSOR) / fondo", ScoreColors.CURSOR, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("estela del cursor (CURSOR_DIMMED) / fondo",
                        PaletteCheck.compositeOver(ScoreColors.CURSOR_DIMMED, ScoreColors.BACKGROUND),
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("linea de reproduccion (PLAYING) / fondo", ScoreColors.PLAYING, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("marca de pista elegida (ACCENT) / fondo", ScoreColors.ACCENT, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cambio de parametro (PARAMETER_CHANGE) / fondo", ScoreColors.PARAMETER_CHANGE,
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde de compas incompleto (INCOMPLETE_MEASURE) / fondo", ScoreColors.INCOMPLETE_MEASURE,
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde de seleccion (ACCENT) / fondo", ScoreColors.ACCENT, ScoreColors.BACKGROUND,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota correspondiente (CORRESPONDING_NOTE) / fondo",
                        PaletteCheck.compositeOver(ScoreColors.CORRESPONDING_NOTE, ScoreColors.BACKGROUND),
                        ScoreColors.BACKGROUND, Contrast.GRAPHICAL_MINIMUM_RATIO)));

        for (int track = 0; track < TrackColors.COUNT; track++) {
            Color colour = TrackColors.of(track);
            pairs.add(new Pair("color de pista " + track + " / panel de pistas", colour, ScoreColors.SURFACE,
                    Contrast.GRAPHICAL_MINIMUM_RATIO));
            pairs.add(new Pair("color de pista " + track + " / panel elegido", colour, ScoreColors.SURFACE_HIGHLIGHT,
                    Contrast.GRAPHICAL_MINIMUM_RATIO));
        }

        pairs.addAll(instrumentPairs());
        return pairs;
    }

    private static List<Pair> instrumentPairs() {
        Color whiteKey = InstrumentColors.WHITE_KEY;
        Color blackKey = InstrumentColors.BLACK_KEY;
        List<Pair> pairs = new ArrayList<>(List.of(
                new Pair("nota marcada (PRESSED) / tecla blanca", InstrumentColors.PRESSED, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota marcada (PRESSED) / tecla negra", InstrumentColors.PRESSED, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota de contexto (CONTEXT) / tecla blanca", InstrumentColors.CONTEXT, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota de contexto (CONTEXT) / tecla negra", InstrumentColors.CONTEXT, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("anillo del mouse (HOVER) / tecla blanca", InstrumentColors.HOVER, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("anillo del mouse (HOVER) / tecla negra", InstrumentColors.HOVER, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde de tecla (KEY_EDGE) / tecla blanca", InstrumentColors.KEY_EDGE, whiteKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde de tecla (KEY_EDGE) / tecla negra", InstrumentColors.KEY_EDGE, blackKey,
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("cejilla (NUT) / mastil electrico", InstrumentColors.NUT, FretboardType.ELECTRIC.woodColor(),
                        Contrast.GRAPHICAL_MINIMUM_RATIO)));
        pairs.addAll(fretboardPairs(FretboardType.ELECTRIC));
        return pairs;
    }

    private static List<Pair> fretboardPairs(FretboardType type) {
        Color wood = type.woodColor();
        String neck = "mastil " + type.label().toLowerCase();
        return List.of(
                new Pair("digito sobre nota marcada / marca - " + type.label(), type.markInkColor(),
                        type.markColor(), Contrast.TEXT_MINIMUM_RATIO),
                new Pair("digito de contexto / marca de contexto - " + type.label(), type.contextInkColor(),
                        type.contextColor(), Contrast.TEXT_MINIMUM_RATIO),
                new Pair("cuerda / " + neck, type.stringColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("traste / " + neck, type.fretWireColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("incrustacion / " + neck, type.inlayColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("borde / " + neck, type.edgeColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota marcada / " + neck, type.markColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("nota de contexto / " + neck, type.contextColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("anillo de foco / " + neck, type.hoverColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO),
                new Pair("caret / " + neck, type.hoverColor(), wood, Contrast.GRAPHICAL_MINIMUM_RATIO));
    }
}

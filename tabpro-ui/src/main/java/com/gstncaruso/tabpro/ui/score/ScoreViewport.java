package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;

/**
 * Por donde se mira la partitura: en que modo, con cuanto zoom, con cuanto ancho disponible,
 * con que pistas a la vista, con que notaciones y sobre que papel. Viaja junta porque todas
 * hacen falta a la vez para saber donde cae cada compas.
 */
public record ScoreViewport(
        ViewMode mode, Zoom zoom, int width, VisibleTracks visibleTracks, VisibleNotations visibleNotations,
        boolean graysTheInactiveVoice, PageSetup pageSetup) {

    public static ScoreViewport of(ViewMode mode, Zoom zoom, int width) {
        return new ScoreViewport(
                mode, zoom, width, VisibleTracks.all(), VisibleNotations.both(), true, PageSetup.defaults());
    }

    public ScoreViewport withVisibleTracks(VisibleTracks visibleTracks) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup);
    }

    public ScoreViewport withVisibleNotations(VisibleNotations visibleNotations) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup);
    }

    public ScoreViewport withGrayingTheInactiveVoice(boolean graysTheInactiveVoice) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup);
    }

    public ScoreViewport withPageSetup(PageSetup pageSetup) {
        return new ScoreViewport(
                mode, zoom, width, visibleTracks, visibleNotations, graysTheInactiveVoice, pageSetup);
    }

    /** La voz que se dibuja entera; la otra queda atenuada. Sin atenuado, ninguna se destaca. */
    public java.util.Optional<com.gstncaruso.tabpro.core.model.VoicePart> highlighted(
            com.gstncaruso.tabpro.core.model.VoicePart editedVoice) {
        return graysTheInactiveVoice ? java.util.Optional.of(editedVoice) : java.util.Optional.empty();
    }

    /** La hoja de papel elegida, medida en pixeles. */
    public PageMetrics sheet() {
        return PageMetrics.of(pageSetup);
    }

    public double factor() {
        return zoom.factor();
    }
}

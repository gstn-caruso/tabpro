package com.gstncaruso.tabpro.ui.score;

/**
 * Por donde se mira la partitura: en que modo, con cuanto zoom, con cuanto ancho disponible,
 * con que pistas a la vista y con que notaciones. Viaja junta porque todas hacen falta a la vez
 * para saber donde cae cada compas.
 */
public record ScoreViewport(
        ViewMode mode, Zoom zoom, int width, VisibleTracks visibleTracks, VisibleNotations visibleNotations) {

    public static ScoreViewport of(ViewMode mode, Zoom zoom, int width) {
        return new ScoreViewport(mode, zoom, width, VisibleTracks.all(), VisibleNotations.both());
    }

    public ScoreViewport withVisibleTracks(VisibleTracks visibleTracks) {
        return new ScoreViewport(mode, zoom, width, visibleTracks, visibleNotations);
    }

    public ScoreViewport withVisibleNotations(VisibleNotations visibleNotations) {
        return new ScoreViewport(mode, zoom, width, visibleTracks, visibleNotations);
    }

    public double factor() {
        return zoom.factor();
    }
}

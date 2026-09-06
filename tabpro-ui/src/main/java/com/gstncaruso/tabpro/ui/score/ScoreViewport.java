package com.gstncaruso.tabpro.ui.score;

/**
 * Por donde se mira la partitura: en que modo, con cuanto zoom, con cuanto ancho disponible y
 * con que pistas a la vista. Viaja junta porque las cuatro cosas hacen falta a la vez para
 * saber donde cae cada compas.
 */
public record ScoreViewport(ViewMode mode, Zoom zoom, int width, VisibleTracks visibleTracks) {

    public static ScoreViewport of(ViewMode mode, Zoom zoom, int width) {
        return new ScoreViewport(mode, zoom, width, VisibleTracks.all());
    }

    public ScoreViewport withVisibleTracks(VisibleTracks visibleTracks) {
        return new ScoreViewport(mode, zoom, width, visibleTracks);
    }

    public double factor() {
        return zoom.factor();
    }
}

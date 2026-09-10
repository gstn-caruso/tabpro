package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.TrackVisibility;

/**
 * Lo que decide como se ve la mesa de mezcla, separado de quien la dibuja: que pistas se ven en
 * la vista multipista y si los parametros de sonido estan reducidos u ocultos. Las pistas
 * visibles las lleva {@link TrackVisibility}, que comparte con la partitura. Nada de esto es un
 * dato de la partitura: no se guarda en el archivo.
 */
public final class MixTableModel {

    private final TrackVisibility visibleTracks;
    private boolean reduced;

    public MixTableModel() {
        this(new TrackVisibility());
    }

    public MixTableModel(TrackVisibility visibleTracks) {
        this.visibleTracks = visibleTracks;
    }

    public boolean isVisibleInMultitrackView(int trackIndex) {
        return visibleTracks.isTurnedOn(trackIndex);
    }

    public void toggleVisibleInMultitrackView(int trackIndex) {
        setVisibleInMultitrackView(trackIndex, !isVisibleInMultitrackView(trackIndex));
    }

    public void setVisibleInMultitrackView(int trackIndex, boolean visible) {
        visibleTracks.setTurnedOn(trackIndex, visible);
    }

    public boolean isReduced() {
        return reduced;
    }

    public void reduceAllParameters() {
        reduced = true;
    }

    public void restoreAllParameters() {
        reduced = false;
    }
}

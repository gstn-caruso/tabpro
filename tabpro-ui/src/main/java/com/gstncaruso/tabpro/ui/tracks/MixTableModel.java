package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.TrackVisibility;
import java.util.EnumMap;
import java.util.Map;

/**
 * Lo que decide como se ve la mesa de mezcla, separado de quien la dibuja: si cada parametro se
 * muestra como potenciometro o como numero y si esta todo reducido. Que pistas se ven en la
 * vista multipista lo lleva {@link TrackVisibility}, que comparte con la partitura. Nada de
 * esto es un dato de la partitura: no se guarda en el archivo.
 */
public final class MixTableModel {

    private final Map<MixParameter, DisplayMode> displayModes = new EnumMap<>(MixParameter.class);
    private final TrackVisibility visibleTracks;
    private boolean reduced;

    public MixTableModel() {
        this(new TrackVisibility());
    }

    public MixTableModel(TrackVisibility visibleTracks) {
        this.visibleTracks = visibleTracks;
        for (MixParameter parameter : MixParameter.values()) {
            displayModes.put(parameter, DisplayMode.KNOB);
        }
    }

    public DisplayMode displayModeOf(MixParameter parameter) {
        return displayModes.get(parameter);
    }

    public void toggleDisplayMode(MixParameter parameter) {
        displayModes.put(parameter, displayModes.get(parameter).toggled());
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

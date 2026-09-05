package com.gstncaruso.tabpro.ui.tracks;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lo que decide como se ve la mesa de mezcla, separado de quien la dibuja: si cada parametro se
 * muestra como potenciometro o como numero, si esta todo reducido, y que pistas se ven en la
 * vista multipista. No es un dato de la partitura: no se guarda en el archivo.
 */
public final class MixTableModel {

    private final Map<MixParameter, DisplayMode> displayModes = new EnumMap<>(MixParameter.class);
    private final Set<Integer> hiddenInMultitrackView = new HashSet<>();
    private boolean reduced;

    public MixTableModel() {
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
        return !hiddenInMultitrackView.contains(trackIndex);
    }

    public void toggleVisibleInMultitrackView(int trackIndex) {
        setVisibleInMultitrackView(trackIndex, !isVisibleInMultitrackView(trackIndex));
    }

    public void setVisibleInMultitrackView(int trackIndex, boolean visible) {
        if (visible) {
            hiddenInMultitrackView.remove(trackIndex);
        } else {
            hiddenInMultitrackView.add(trackIndex);
        }
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

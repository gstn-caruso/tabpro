package com.gstncaruso.tabpro.core.harmony;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * La zona E de la ventana de acordes: la lista de acordes que ya se usaron en una pista,
 * en el orden en que aparecen, para que el usuario los pueda revisar o editar.
 */
public final class TrackChords {

    private TrackChords() {
    }

    /** Un diagrama por nombre, con el primero que aparece en la pista. */
    public static List<ChordDiagram> usedIn(Track track) {
        Map<String, ChordDiagram> byName = new LinkedHashMap<>();
        for (Measure measure : track.measures()) {
            for (Voice voice : measure.voices()) {
                for (Beat beat : voice.beats()) {
                    beat.effects().chord().ifPresent(chord -> byName.putIfAbsent(chord.name(), chord));
                }
            }
        }
        return List.copyOf(byName.values());
    }

    /**
     * Manual, linea 2800: la ventana de propiedades de la pista permite mostrar los diagramas
     * "at the top of the score", una sola vez por partitura -no compas por compas-. Junta los
     * acordes de cada pista que eligio {@link com.gstncaruso.tabpro.core.model.DiagramPlacement
     * UNDER_THE_TITLE} o {@code BOTH}, en el orden de las pistas y, dentro de cada una, en el
     * orden en que aparecen sus acordes; si dos pistas repiten el mismo nombre, se cuenta una
     * sola vez con el diagrama de la primera que lo uso.
     */
    public static List<ChordDiagram> underTheTitle(Score score) {
        Map<String, ChordDiagram> byName = new LinkedHashMap<>();
        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            Track track = score.track(trackIndex);
            if (!track.settings().display().diagrams().showsUnderTheTitle()) {
                continue;
            }
            for (ChordDiagram chord : usedIn(track)) {
                byName.putIfAbsent(chord.name(), chord);
            }
        }
        return List.copyOf(byName.values());
    }
}

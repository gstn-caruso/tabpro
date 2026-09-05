package com.gstncaruso.tabpro.core.harmony;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
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
}

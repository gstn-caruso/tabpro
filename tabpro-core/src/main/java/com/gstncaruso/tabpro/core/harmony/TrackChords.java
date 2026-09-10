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

public final class TrackChords {

    private TrackChords() {
    }

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

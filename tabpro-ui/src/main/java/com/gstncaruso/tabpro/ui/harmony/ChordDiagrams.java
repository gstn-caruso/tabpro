package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.ArrayList;
import java.util.List;

public final class ChordDiagrams {

    private ChordDiagrams() {
    }

    public static ChordDiagram fromBeat(Beat beat, Tuning tuning) {
        List<Integer> frets = new ArrayList<>(tuning.stringCount());
        for (int string = 1; string <= tuning.stringCount(); string++) {
            frets.add(ChordDiagram.MUTED);
        }
        for (Note note : beat.notes()) {
            if (note.string() <= tuning.stringCount()) {
                frets.set(note.string() - 1, note.fret());
            }
        }
        return withBaseFret(ChordDiagram.named("", frets), lowestSensibleBaseFret(frets));
    }

    private static int lowestSensibleBaseFret(List<Integer> frets) {
        boolean hasOpenString = frets.stream().anyMatch(fret -> fret == 0);
        int lowestFretted = frets.stream().filter(fret -> fret > 0).mapToInt(Integer::intValue).min().orElse(0);
        return !hasOpenString && lowestFretted > 1 ? lowestFretted : 1;
    }

    public static ChordDiagram withBaseFret(ChordDiagram diagram, int baseFret) {
        return new ChordDiagram(diagram.name(), baseFret, diagram.frets(), diagram.fingering(), diagram.shown());
    }
}

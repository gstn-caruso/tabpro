package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import java.util.ArrayList;
import java.util.List;

public final class PitchClasses {

    private PitchClasses() {
    }

    public static List<PitchClass> chromatic() {
        List<PitchClass> notes = new ArrayList<>(12);
        for (int semitone = 0; semitone < 12; semitone++) {
            notes.add(PitchClass.fromSemitone(semitone));
        }
        return List.copyOf(notes);
    }
}

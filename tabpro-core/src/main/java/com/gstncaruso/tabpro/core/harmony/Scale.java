package com.gstncaruso.tabpro.core.harmony;

import java.util.ArrayList;
import java.util.List;

/**
 * Una escala: sus semitonos desde la tonica (que es lo que suena) y que letra usar para
 * cada uno (que es como se deletrea bien, sin saltear ni repetir letras de mas). La
 * misma letra puede aparecer dos veces en una escala (una nota "blue" o una escala
 * disminuida reusan una letra con dos alteraciones distintas), pero no hace falta usarlas
 * todas: una pentatonica, por ejemplo, deja afuera dos de las siete.
 */
public record Scale(String name, List<Integer> semitones, List<Integer> letterSteps) {

    public Scale {
        if (semitones.isEmpty()) {
            throw new IllegalArgumentException("una escala necesita al menos una nota");
        }
        if (semitones.size() != letterSteps.size()) {
            throw new IllegalArgumentException("cada semitono necesita su propia letra");
        }
        semitones = List.copyOf(semitones);
        letterSteps = List.copyOf(letterSteps);
    }

    public int degreeCount() {
        return semitones.size();
    }

    /** Las notas de la escala a partir de esa tonica: nombre, intervalo y grado de cada una. */
    public List<ScaleTone> notesFrom(PitchClass tonic) {
        List<ScaleTone> notes = new ArrayList<>(degreeCount());
        for (int i = 0; i < degreeCount(); i++) {
            int letterStep = letterSteps.get(i);
            int semitoneOffset = semitones.get(i);
            PitchClass note = tonic.steppedBy(letterStep, semitoneOffset);
            Interval interval = Interval.matching(letterStep, semitoneOffset)
                    .orElseThrow(() -> new IllegalStateException(
                            "la escala " + name + " tiene un grado sin intervalo conocido: " + letterStep + "/" + semitoneOffset));
            notes.add(new ScaleTone(note, interval, i + 1));
        }
        return notes;
    }
}

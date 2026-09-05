package com.gstncaruso.tabpro.core.model.chords;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Un acorde tal como se dibuja arriba de la tablatura: su nombre y, para cada
 * cuerda, el traste que se pisa.
 */
public record ChordDiagram(String name, int baseFret, List<Integer> frets, List<Finger> fingering, boolean shown) {

    /** El traste con que se anota una cuerda que no se toca. */
    public static final int MUTED = -1;

    public ChordDiagram {
        if (baseFret < 1) {
            throw new IllegalArgumentException("baseFret debe ser >= 1: " + baseFret);
        }
        if (frets.isEmpty()) {
            throw new IllegalArgumentException("un diagrama necesita al menos una cuerda");
        }
        frets = List.copyOf(frets);
        fingering = List.copyOf(fingering);
    }

    public static ChordDiagram named(String name, List<Integer> frets) {
        return new ChordDiagram(name, 1, frets, List.of(), true);
    }

    /** Solo el nombre, sin diagrama dibujado. */
    public static ChordDiagram justTheName(String name) {
        return new ChordDiagram(name, 1, List.of(MUTED), List.of(), false);
    }

    public int stringCount() {
        return frets.size();
    }

    /** El traste de esa cuerda, contando la cuerda 1 como la mas aguda. */
    public int fretOfString(int string) {
        return frets.get(string - 1);
    }

    public boolean isPlayed(int string) {
        return fretOfString(string) != MUTED;
    }

    public boolean isOpen(int string) {
        return fretOfString(string) == 0;
    }

    public Optional<Finger> fingerOfString(int string) {
        if (string > fingering.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(fingering.get(string - 1));
    }

    /** El traste mas alto que se pisa, para saber cuantos trastes dibujar. */
    public int highestFret() {
        return frets.stream().filter(fret -> fret > 0).mapToInt(Integer::intValue).max().orElse(0);
    }

    public int lowestFret() {
        return frets.stream().filter(fret -> fret > 0).mapToInt(Integer::intValue).min().orElse(0);
    }

    public ChordDiagram withName(String name) {
        return new ChordDiagram(name, baseFret, frets, fingering, shown);
    }

    public ChordDiagram withFingering(List<Finger> fingering) {
        return new ChordDiagram(name, baseFret, frets, fingering, shown);
    }

    public ChordDiagram shownAs(boolean shown) {
        return new ChordDiagram(name, baseFret, frets, fingering, shown);
    }

    public ChordDiagram withFretOnString(int string, int fret) {
        List<Integer> updated = new ArrayList<>(frets);
        updated.set(string - 1, fret);
        return new ChordDiagram(name, baseFret, updated, fingering, shown);
    }
}

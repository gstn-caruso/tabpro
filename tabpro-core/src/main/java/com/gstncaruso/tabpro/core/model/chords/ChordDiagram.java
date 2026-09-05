package com.gstncaruso.tabpro.core.model.chords;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
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
        // No usamos List.copyOf: una cuerda sin dedo se representa con null en esta lista,
        // y List.copyOf no admite nulls.
        fingering = Collections.unmodifiableList(new ArrayList<>(fingering));
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

    /** Cuantas cuerdas hay que pisar (sin contar las al aire ni las mudas). */
    public int frettedStringCount() {
        return (int) frets.stream().filter(fret -> fret > 0).count();
    }

    /** El estiramiento de la mano: la distancia entre el traste mas bajo y el mas alto que se pisa. */
    public int fretSpan() {
        return highestFret() - lowestFret();
    }

    /**
     * Si hacen falta mas de cuatro dedos para pisar todo, un dedo tiene que cubrir varias
     * cuerdas al mismo traste: eso es una cejilla.
     */
    public boolean requiresBarre() {
        return frettedStringCount() > 4;
    }

    /** El traste de la cejilla, si hace falta una: siempre el mas bajo que se pisa. */
    public OptionalInt barreFret() {
        if (!requiresBarre()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(lowestFret());
    }

    /**
     * Cuantas cuerdas mudas quedan atrapadas entre dos cuerdas que si suenan: silenciarlas
     * sin tocar las vecinas es lo que hace dificil a un diagrama.
     */
    public int interiorMutedStringCount() {
        int count = 0;
        for (int string = 2; string < stringCount(); string++) {
            if (!isPlayed(string) && hasPlayedStringBefore(string) && hasPlayedStringAfter(string)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasPlayedStringBefore(int string) {
        for (int other = 1; other < string; other++) {
            if (isPlayed(other)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPlayedStringAfter(int string) {
        for (int other = string + 1; other <= stringCount(); other++) {
            if (isPlayed(other)) {
                return true;
            }
        }
        return false;
    }

    /** Un puntaje de dificultad: mas alto cuanto mas cuesta digitarlo. Solo sirve para ordenar y filtrar. */
    public int difficultyScore() {
        int score = 0;
        if (requiresBarre()) {
            score += 3;
        }
        score += fretSpan();
        score += lowestFret() / 3;
        score += 2 * interiorMutedStringCount();
        return score;
    }

    /** En que categoria de dificultad cae, para el filtro Simple / Media / Todas del buscador. */
    public ChordComplexity complexity() {
        int score = difficultyScore();
        if (score <= 2) {
            return ChordComplexity.SIMPLE;
        }
        if (score <= 6) {
            return ChordComplexity.MEDIUM;
        }
        return ChordComplexity.COMPLEX;
    }

    /**
     * Digita el diagrama solo: si hace falta cejilla la cubre con el indice, y reparte el
     * resto de los dedos de mas grave a mas agudo entre las cuerdas que quedan por pisar.
     */
    public ChordDiagram autoFingered() {
        OptionalInt barre = barreFret();
        List<Finger> remainingFingers = barre.isPresent()
                ? List.of(Finger.MIDDLE, Finger.RING, Finger.LITTLE)
                : List.of(Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.LITTLE);

        Finger[] byString = new Finger[stringCount()];
        if (barre.isPresent()) {
            for (int string = 1; string <= stringCount(); string++) {
                if (fretOfString(string) == barre.getAsInt()) {
                    byString[string - 1] = Finger.INDEX;
                }
            }
        }

        List<Integer> remainingStrings = new ArrayList<>();
        for (int string = 1; string <= stringCount(); string++) {
            if (fretOfString(string) > 0 && fretOfString(string) != barre.orElse(-1)) {
                remainingStrings.add(string);
            }
        }
        remainingStrings.sort(Comparator.<Integer>comparingInt(this::fretOfString).thenComparingInt(s -> s));

        for (int i = 0; i < remainingStrings.size(); i++) {
            int finger = Math.min(i, remainingFingers.size() - 1);
            byString[remainingStrings.get(i) - 1] = remainingFingers.get(finger);
        }

        return withFingering(Arrays.asList(byString));
    }
}

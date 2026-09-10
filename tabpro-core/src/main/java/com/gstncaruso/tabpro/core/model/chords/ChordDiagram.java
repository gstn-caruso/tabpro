package com.gstncaruso.tabpro.core.model.chords;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Optional;

public record ChordDiagram(String name, int baseFret, List<Integer> frets, List<Finger> fingering, boolean shown) {

    public static final int MUTED = -1;

    public ChordDiagram {
        if (baseFret < 1) {
            throw new IllegalArgumentException("baseFret must be >= 1: " + baseFret);
        }
        if (frets.isEmpty()) {
            throw new IllegalArgumentException("a diagram needs at least one string");
        }
        frets = List.copyOf(frets);
        fingering = Collections.unmodifiableList(new ArrayList<>(fingering));
    }

    public static ChordDiagram named(String name, List<Integer> frets) {
        return new ChordDiagram(name, 1, frets, List.of(), true);
    }

    public static ChordDiagram justTheName(String name) {
        return new ChordDiagram(name, 1, List.of(MUTED), List.of(), false);
    }

    public int stringCount() {
        return frets.size();
    }

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

    public List<Integer> shape() {
        return frets.stream().map(fret -> fret > 0 ? fret - baseFret + 1 : fret).toList();
    }

    public ChordDiagram withFretOnString(int string, int fret) {
        List<Integer> updated = new ArrayList<>(frets);
        updated.set(string - 1, fret);
        return new ChordDiagram(name, baseFret, updated, fingering, shown);
    }

    public int frettedStringCount() {
        return (int) frets.stream().filter(fret -> fret > 0).count();
    }

    public int fretSpan() {
        return highestFret() - lowestFret();
    }

    public boolean requiresBarre() {
        return frettedStringCount() > 4;
    }

    public OptionalInt barreFret() {
        if (!requiresBarre()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(lowestFret());
    }

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

package com.gstncaruso.tabpro.ui.instruments;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** Lo mismo que {@code FretMarks}, pero para teclas del teclado, en numero MIDI. */
public record KeyMarks(Set<Integer> primary, Set<Integer> secondary) {

    public static KeyMarks of(Set<Integer> primary, Set<Integer> secondary) {
        Set<Integer> onlyContext = new HashSet<>(secondary);
        onlyContext.removeAll(primary);
        return new KeyMarks(Set.copyOf(primary), Set.copyOf(onlyContext));
    }

    public Optional<MarkKind> kindOf(int midiNumber) {
        if (primary.contains(midiNumber)) {
            return Optional.of(MarkKind.PRIMARY);
        }
        if (secondary.contains(midiNumber)) {
            return Optional.of(MarkKind.SECONDARY);
        }
        return Optional.empty();
    }
}

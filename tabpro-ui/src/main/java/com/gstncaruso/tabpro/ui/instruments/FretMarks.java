package com.gstncaruso.tabpro.ui.instruments;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Que posiciones del mastil marca un modo de vista: las del beat (primarias) y
 * las que suma el modo como contexto (secundarias). Una posicion primaria nunca
 * aparece tambien como secundaria.
 */
public record FretMarks(Set<FretPosition> primary, Set<FretPosition> secondary) {

    public static FretMarks of(Set<FretPosition> primary, Set<FretPosition> secondary) {
        Set<FretPosition> onlyContext = new HashSet<>(secondary);
        onlyContext.removeAll(primary);
        return new FretMarks(Set.copyOf(primary), Set.copyOf(onlyContext));
    }

    public Optional<MarkKind> kindOf(FretPosition position) {
        if (primary.contains(position)) {
            return Optional.of(MarkKind.PRIMARY);
        }
        if (secondary.contains(position)) {
            return Optional.of(MarkKind.SECONDARY);
        }
        return Optional.empty();
    }
}

package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.prefs.Preferences;

/**
 * La digitacion que el usuario corrigio a mano, guardada por la forma del acorde -no por su
 * nombre ni por donde esta en el mastil-, como describe el manual: "si la modificas, la
 * memoriza y la usa para cualquier acorde parecido que encuentre". La misma cejilla corrida
 * a otro traste comparte la forma (ver {@link com.gstncaruso.tabpro.core.model.chords.ChordDiagram#shape()})
 * y por lo tanto la digitacion guardada.
 */
public final class FingeringMemory {

    private static final String ITEMS_NODE = "items";
    private static final String SEPARATOR = ",";
    private static final String NO_FINGER = "-";

    private final Preferences store;

    public FingeringMemory(Preferences store) {
        this.store = store;
    }

    public static FingeringMemory userMemory() {
        return new FingeringMemory(Preferences.userNodeForPackage(FingeringMemory.class).node("fingeringMemory"));
    }

    /** La digitacion que se recuerda para esa forma, si alguna vez se corrigio una igual. */
    public Optional<List<Finger>> fingeringFor(List<Integer> shape) {
        String saved = store.node(ITEMS_NODE).get(encode(shape, String::valueOf), null);
        return saved == null ? Optional.empty() : Optional.of(splitFingers(saved));
    }

    /** Guarda (o reemplaza) la digitacion asociada a esa forma. */
    public void remember(List<Integer> shape, List<Finger> fingering) {
        store.node(ITEMS_NODE).put(
                encode(shape, String::valueOf), encode(fingering, finger -> finger == null ? NO_FINGER : finger.name()));
    }

    private static <T> String encode(List<T> values, Function<T, String> toText) {
        return values.stream().map(toText).reduce((a, b) -> a + SEPARATOR + b).orElse("");
    }

    private static List<Finger> splitFingers(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        return List.of(text.split(SEPARATOR, -1)).stream()
                .map(token -> NO_FINGER.equals(token) ? null : Finger.valueOf(token))
                .toList();
    }
}

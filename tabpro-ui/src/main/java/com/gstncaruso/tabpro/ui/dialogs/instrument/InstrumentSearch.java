package com.gstncaruso.tabpro.ui.dialogs.instrument;

import com.gstncaruso.tabpro.core.model.Instruments;
import java.util.ArrayList;
import java.util.List;

/** Que programas General MIDI matchean lo que se escribio en el buscador. */
public final class InstrumentSearch {

    private InstrumentSearch() {
    }

    public static List<Integer> matching(String query) {
        String needle = query.strip().toLowerCase();
        List<Integer> matches = new ArrayList<>();
        for (int program = 0; program < Instruments.COUNT; program++) {
            if (needle.isEmpty() || Instruments.nameOf(program).toLowerCase().contains(needle)) {
                matches.add(program);
            }
        }
        return List.copyOf(matches);
    }
}

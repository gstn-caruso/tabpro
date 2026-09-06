package com.gstncaruso.tabpro.ui.dialogs.instrument;

import com.gstncaruso.tabpro.core.model.InstrumentPatch;
import com.gstncaruso.tabpro.core.model.Instruments;
import java.util.ArrayList;
import java.util.List;

/** Que programas matchean lo que se escribio en el buscador, con los nombres del patch elegido. */
public final class InstrumentSearch {

    private InstrumentSearch() {
    }

    public static List<Integer> matching(String query) {
        return matching(query, InstrumentPatch.generalMidi());
    }

    public static List<Integer> matching(String query, InstrumentPatch patch) {
        String needle = query.strip().toLowerCase();
        List<Integer> matches = new ArrayList<>();
        for (int program = 0; program < Instruments.COUNT; program++) {
            if (needle.isEmpty() || patch.nameOf(program).toLowerCase().contains(needle)) {
                matches.add(program);
            }
        }
        return List.copyOf(matches);
    }
}

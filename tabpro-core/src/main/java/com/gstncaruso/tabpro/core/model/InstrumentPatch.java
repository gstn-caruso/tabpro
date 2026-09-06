package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * El patch de instrumentos de "Configure the Sound &gt; MIDI Setup": una
 * lista de nombres que reemplaza como se muestra cada programa MIDI, sin
 * tocar el sonido. Texto plano, un nombre por linea, sin encabezado; el
 * numero de linea (arrancando en cero) es el numero de programa. Una linea
 * en blanco, o no tener patch, deja el nombre General MIDI de ese programa.
 */
public final class InstrumentPatch {

    private static final InstrumentPatch GENERAL_MIDI = new InstrumentPatch(List.of());

    private final List<String> names;

    private InstrumentPatch(List<String> names) {
        this.names = names;
    }

    public static InstrumentPatch generalMidi() {
        return GENERAL_MIDI;
    }

    public static InstrumentPatch parse(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            lines.add(withoutTrailingCarriageReturn(line));
        }
        return new InstrumentPatch(List.copyOf(lines));
    }

    /** El nombre que hay que mostrar para ese programa: el del patch, o si no el de General MIDI. */
    public String nameOf(int program) {
        if (program < 0 || program >= Instruments.COUNT) {
            throw new IllegalArgumentException("program debe estar entre 0 y " + (Instruments.COUNT - 1) + ": " + program);
        }
        if (program < names.size() && !names.get(program).isBlank()) {
            return names.get(program);
        }
        return Instruments.nameOf(program);
    }

    private static String withoutTrailingCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }
}

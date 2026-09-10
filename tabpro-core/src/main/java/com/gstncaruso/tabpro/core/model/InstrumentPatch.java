package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The instrument patch format of "Configure the Sound &gt; MIDI Setup": a
 * list of names that replaces how each MIDI program is displayed, without
 * touching the sound. Plain text, one name per line, no header; the line
 * number (starting at zero) is the program number. A blank line, or having
 * no patch at all, keeps that program's General MIDI name.
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

    public String nameOf(int program) {
        if (program < 0 || program >= Instruments.COUNT) {
            throw new IllegalArgumentException("program must be between 0 and " + (Instruments.COUNT - 1) + ": " + program);
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

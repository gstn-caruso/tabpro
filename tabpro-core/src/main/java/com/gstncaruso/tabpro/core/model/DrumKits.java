package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The drum kits standardized by General MIDI Level 1 for channel 10. There, the
 * program change does not pick an instrument: it picks the set of percussion
 * sounds to use.
 */
public final class DrumKits {

    private static final Map<Integer, String> NAMES = namesByProgram();

    private DrumKits() {
    }

    public static List<String> names() {
        return List.copyOf(NAMES.values());
    }

    public static int programAt(int index) {
        return new ArrayList<>(NAMES.keySet()).get(index);
    }

    public static int indexOf(int program) {
        int index = new ArrayList<>(NAMES.keySet()).indexOf(program);
        return index >= 0 ? index : 0;
    }

    private static Map<Integer, String> namesByProgram() {
        Map<Integer, String> names = new LinkedHashMap<>();
        names.put(0, "Standard");
        names.put(8, "Room");
        names.put(16, "Power");
        names.put(24, "Electronic");
        names.put(25, "TR-808");
        names.put(32, "Jazz");
        names.put(40, "Brush");
        names.put(48, "Orchestra");
        names.put(56, "SFX");
        return Collections.unmodifiableMap(names);
    }
}

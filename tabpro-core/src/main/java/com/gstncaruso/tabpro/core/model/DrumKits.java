package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Los kits de bateria que estandariza General MIDI Level 1 para el canal 10. Ahi el program
 * change no elige un instrumento: elige el conjunto de sonidos de percusion a usar.
 */
public final class DrumKits {

    private static final Map<Integer, String> NAMES = namesByProgram();

    private DrumKits() {
    }

    public static List<String> names() {
        return List.copyOf(NAMES.values());
    }

    /** El programa de General MIDI del kit que esta en esa posicion de {@link #names()}. */
    public static int programAt(int index) {
        return new ArrayList<>(NAMES.keySet()).get(index);
    }

    /** La posicion en {@link #names()} de ese programa, o la del kit Standard si no es un kit conocido. */
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

package com.gstncaruso.tabpro.core.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Los sonidos de bateria de la especificacion General MIDI. En una pista de
 * percusion los numeros de la tablatura son estos sonidos, no trastes.
 */
public final class PercussionKit {

    /** El rango que toda placa de sonido garantiza. */
    public static final int LOWEST_SOUND = 35;
    public static final int HIGHEST_SOUND = 81;

    /** La tablatura de percusion tiene seis lineas, una por sonido simultaneo. */
    public static final int LINE_COUNT = 6;

    private static final Map<Integer, String> NAMES = names();

    private PercussionKit() {
    }

    /** Una afinacion ficticia: las lineas de percusion no tienen altura propia. */
    public static Tuning tuning() {
        return Tuning.of("Percusion", 0, 0, 0, 0, 0, 0);
    }

    public static Optional<String> nameOf(int sound) {
        return Optional.ofNullable(NAMES.get(sound));
    }

    public static boolean isPlayable(int sound) {
        return NAMES.containsKey(sound);
    }

    public static List<Integer> sounds() {
        return List.copyOf(NAMES.keySet());
    }

    private static Map<Integer, String> names() {
        Map<Integer, String> names = new LinkedHashMap<>();
        names.put(35, "Bombo acustico");
        names.put(36, "Bombo");
        names.put(37, "Aro de caja");
        names.put(38, "Caja acustica");
        names.put(39, "Palmas");
        names.put(40, "Caja electrica");
        names.put(41, "Tom de piso grave");
        names.put(42, "Hi-hat cerrado");
        names.put(43, "Tom de piso agudo");
        names.put(44, "Hi-hat con pedal");
        names.put(45, "Tom grave");
        names.put(46, "Hi-hat abierto");
        names.put(47, "Tom medio grave");
        names.put(48, "Tom medio agudo");
        names.put(49, "Crash 1");
        names.put(50, "Tom agudo");
        names.put(51, "Ride 1");
        names.put(52, "Platillo chino");
        names.put(53, "Campana del ride");
        names.put(54, "Pandereta");
        names.put(55, "Splash");
        names.put(56, "Cencerro");
        names.put(57, "Crash 2");
        names.put(58, "Vibraslap");
        names.put(59, "Ride 2");
        names.put(60, "Bongo agudo");
        names.put(61, "Bongo grave");
        names.put(62, "Conga aguda apagada");
        names.put(63, "Conga aguda abierta");
        names.put(64, "Conga grave");
        names.put(65, "Timbal agudo");
        names.put(66, "Timbal grave");
        names.put(67, "Agogo agudo");
        names.put(68, "Agogo grave");
        names.put(69, "Cabasa");
        names.put(70, "Maracas");
        names.put(71, "Silbato corto");
        names.put(72, "Silbato largo");
        names.put(73, "Guiro corto");
        names.put(74, "Guiro largo");
        names.put(75, "Claves");
        names.put(76, "Cajita china aguda");
        names.put(77, "Cajita china grave");
        names.put(78, "Cuica apagada");
        names.put(79, "Cuica abierta");
        names.put(80, "Triangulo apagado");
        names.put(81, "Triangulo abierto");
        return java.util.Collections.unmodifiableMap(names);
    }
}

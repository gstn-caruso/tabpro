package com.gstncaruso.tabpro.format.powertab;

/**
 * Un marcador de tempo de PowerTab. tabpro solo guarda un tempo por partitura
 * entera (no un cambio a mitad de camino), asi que de todos los marcadores
 * del archivo solo se usa el primero que sea un marcador estandar (negra =
 * tantos); los de tipo listesso o "acelerando/ritardando", y la descripcion,
 * no tienen destino en el modelo.
 */
record PowerTabTempoMarker(int system, int position, int type, int beatsPerMinute) {

    static final int STANDARD_MARKER = 1;

    boolean isStandardMarker() {
        return type == STANDARD_MARKER;
    }
}

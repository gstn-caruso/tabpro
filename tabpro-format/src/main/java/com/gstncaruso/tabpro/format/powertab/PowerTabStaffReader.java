package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee un pentagrama: su cantidad de cuerdas (el clave y los espaciados de
 * dibujo no tienen lugar en el modelo) y sus dos voces de posiciones.
 */
final class PowerTabStaffReader {

    private static final int VOICE_COUNT = 2;
    private static final int STRING_COUNT_MASK = 0x0f;

    private final PowerTabPositionReader positionReader = new PowerTabPositionReader();

    PowerTabStaff read(PowerTabByteReader reader) {
        int data = reader.readUnsignedByte();
        int stringCount = data & STRING_COUNT_MASK;
        reader.readUnsignedByte(); // espaciado sobre el pentagrama de notacion.
        reader.readUnsignedByte(); // espaciado bajo el pentagrama de notacion.
        reader.readUnsignedByte(); // espaciado de simbolos.
        reader.readUnsignedByte(); // espaciado bajo la tablatura.

        List<List<PowerTabPosition>> voices = new ArrayList<>(VOICE_COUNT);
        for (int voice = 0; voice < VOICE_COUNT; voice++) {
            voices.add(reader.readVector(positionReader::read));
        }
        return new PowerTabStaff(stringCount, voices);
    }
}

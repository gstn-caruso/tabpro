package com.gstncaruso.tabpro.format.powertab;

/** Lee un "guitar in": sistema, pentagrama, posicion y la mascara de guitarras activas en el. */
final class PowerTabGuitarInReader {

    PowerTabGuitarIn read(PowerTabByteReader reader) {
        int system = reader.readUnsignedShort();
        int staff = reader.readUnsignedByte();
        int position = reader.readUnsignedByte();
        int data = reader.readUnsignedShort();
        int staffGuitarsMask = (data >>> 8) & 0xFF; // el byte alto: el bajo es para rhythm slash.
        return new PowerTabGuitarIn(system, staff, position, staffGuitarsMask);
    }
}

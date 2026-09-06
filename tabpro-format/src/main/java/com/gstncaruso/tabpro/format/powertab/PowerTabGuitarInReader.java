package com.gstncaruso.tabpro.format.powertab;

/** Lee un "guitar in": sistema, pentagrama, posicion y la mascara de guitarras activas en el. */
final class PowerTabGuitarInReader {

    PowerTabGuitarIn read(PowerTabByteReader reader) {
        int system = reader.readUnsignedShort();
        int staff = reader.readUnsignedByte();
        int position = reader.readUnsignedByte();
        int data = reader.readUnsignedShort();
        // Al reves de lo intuitivo: uno esperaria que la mascara "principal" (la del
        // pentagrama) fuera el byte bajo. En guitarin.cpp GetStaffGuitars() devuelve
        // HIBYTE(m_data) y GetRhythmSlashGuitars() devuelve LOBYTE(m_data) — lo
        // confirma el propio constructor, que arma el dato como
        // MAKEWORD(rhythmSlashGuitars, staffGuitars). La primera version de este
        // lector los tenia al reves (probado contra fixtures reales, no adivinado).
        int staffGuitarsMask = (data >>> 8) & 0xFF;
        return new PowerTabGuitarIn(system, staff, position, staffGuitarsMask);
    }
}

package com.gstncaruso.tabpro.format.powertab;

/** Reads a "guitar in": system, staff, position, and the mask of guitars active on it. */
final class PowerTabGuitarInReader {

    PowerTabGuitarIn read(PowerTabByteReader reader) {
        int system = reader.readUnsignedShort();
        int staff = reader.readUnsignedByte();
        int position = reader.readUnsignedByte();
        int data = reader.readUnsignedShort();
        // Counter-intuitively, one would expect the "main" mask (the staff's) to be the
        // low byte. In guitarin.cpp GetStaffGuitars() returns HIBYTE(m_data) and
        // GetRhythmSlashGuitars() returns LOBYTE(m_data) -- confirmed by the constructor
        // itself, which builds the datum as MAKEWORD(rhythmSlashGuitars, staffGuitars).
        int staffGuitarsMask = (data >>> 8) & 0xFF;
        return new PowerTabGuitarIn(system, staff, position, staffGuitarsMask);
    }
}

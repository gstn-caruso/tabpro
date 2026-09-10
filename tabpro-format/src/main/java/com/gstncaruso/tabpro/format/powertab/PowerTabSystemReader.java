package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Reads a system: its complete start barline, the internal barlines, the type and
 * repeat count of its end barline, and its staves. Directions and chord text have no
 * place in the model and are discarded; rhythm slashes are only counted, because they
 * would represent real music we do not yet know how to convert: whoever assembles the
 * score decides whether that is enough to reject the file.
 */
final class PowerTabSystemReader {

    private final PowerTabBarlineReader barlineReader = new PowerTabBarlineReader();
    private final PowerTabStaffReader staffReader = new PowerTabStaffReader();

    PowerTabSystem read(PowerTabByteReader reader) {
        reader.skip(16); // system rectangle: has no place in the model.
        int endBarByte = reader.readUnsignedByte();
        int endBarType = (endBarByte >>> 5) & 0x07;
        int endBarRepeatCount = endBarByte & 0x1f;
        reader.readUnsignedByte(); // spacing between positions.
        reader.readUnsignedByte(); // rhythm slash spacing, above.
        reader.readUnsignedByte(); // rhythm slash spacing, below.
        reader.readUnsignedByte(); // extra spacing.

        PowerTabBarline startBar = barlineReader.read(reader);

        reader.skipVector(PowerTabAuxiliaryReader::skipDirection);
        reader.skipVector(PowerTabAuxiliaryReader::skipChordText);
        int rhythmSlashCount = reader.skipVector(PowerTabAuxiliaryReader::skipRhythmSlash);

        List<PowerTabStaff> staves = reader.readVector(staffReader::read);
        List<PowerTabBarline> internalBarlines = reader.readVector(barlineReader::read);

        return new PowerTabSystem(startBar, internalBarlines, endBarType, endBarRepeatCount, staves, rhythmSlashCount);
    }
}

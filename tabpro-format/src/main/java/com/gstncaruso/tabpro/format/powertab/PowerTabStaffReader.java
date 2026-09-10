package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads a staff: its string count (the clef and the drawing spacings have no place in
 * the model) and its two voices of positions.
 */
final class PowerTabStaffReader {

    private static final int VOICE_COUNT = 2;
    private static final int STRING_COUNT_MASK = 0x0f;

    private final PowerTabPositionReader positionReader = new PowerTabPositionReader();

    PowerTabStaff read(PowerTabByteReader reader) {
        int data = reader.readUnsignedByte();
        int stringCount = data & STRING_COUNT_MASK;
        reader.readUnsignedByte(); // spacing above the notation staff.
        reader.readUnsignedByte(); // spacing below the notation staff.
        reader.readUnsignedByte(); // symbol spacing.
        reader.readUnsignedByte(); // spacing below the tablature.

        List<List<PowerTabPosition>> voices = new ArrayList<>(VOICE_COUNT);
        for (int voice = 0; voice < VOICE_COUNT; voice++) {
            voices.add(reader.readVector(positionReader::read));
        }
        return new PowerTabStaff(stringCount, voices);
    }
}

package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.VoicePart;

/**
 * Reads the seven payload bytes of a rest. Unlike the note, the duration occupies the
 * whole byte: it is not mixed with any other flag.
 */
final class TabEditRestReader {

    TabEditRestFields read(TabEditByteReader input) {
        int durationCode = input.readUnsignedByte();

        int flags = input.readUnsignedByte();
        boolean bit4 = (flags & 0x10) != 0;
        boolean bit5 = (flags & 0x20) != 0;
        VoicePart voice = bit5 && bit4 ? VoicePart.BASS : VoicePart.LEAD;

        input.skip(5); // vertical position, secondary beam cut, and drawing flags: unused.

        return new TabEditRestFields(TabEditDurationMapper.toDuration(durationCode), voice);
    }
}

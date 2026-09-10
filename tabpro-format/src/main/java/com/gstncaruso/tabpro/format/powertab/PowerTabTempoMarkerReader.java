package com.gstncaruso.tabpro.format.powertab;

/** Reads a tempo marker: inherits from "system symbol" and adds its description. */
final class PowerTabTempoMarkerReader {

    private static final int TYPE_SHIFT = 27;
    private static final int TYPE_MASK = 0x03;
    private static final int BEATS_PER_MINUTE_MASK = 0xffff;

    PowerTabTempoMarker read(PowerTabByteReader reader) {
        int system = reader.readUnsignedShort();
        int position = reader.readUnsignedByte();
        int data = reader.readInt();
        reader.readMfcString(); // description: has no place in the model.

        int type = (data >>> TYPE_SHIFT) & TYPE_MASK;
        int beatsPerMinute = data & BEATS_PER_MINUTE_MASK;
        return new PowerTabTempoMarker(system, position, type, beatsPerMinute);
    }
}

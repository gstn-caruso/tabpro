package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;

/**
 * Reads a barline: its position, its type, its key signature, and its time signature.
 * The rehearsal mark it can carry (letter + description) has no place in the tabpro
 * model; it is consumed all the same to not lose the file's sync. It also does not
 * distinguish whether the key signature or time signature are marked to show or not
 * ("show"/"cancellation"): tabpro always applies them.
 */
final class PowerTabBarlineReader {

    private static final int COMMON_TIME = 0x400000;
    private static final int CUT_TIME = 0x800000;

    PowerTabBarline read(PowerTabByteReader reader) {
        int position = reader.readUnsignedByte();
        int data = reader.readUnsignedByte();
        int type = (data >>> 5) & 0x07;
        int repeatCount = data & 0x1f;

        KeySignature keySignature = readKeySignature(reader);
        TimeSignature timeSignature = readTimeSignature(reader);
        skipRehearsalSign(reader);

        return new PowerTabBarline(position, type, repeatCount, timeSignature, keySignature);
    }

    private KeySignature readKeySignature(PowerTabByteReader reader) {
        int data = reader.readUnsignedByte();
        int rawAccidentals = data & 0x0f;
        int accidentals = rawAccidentals <= 7 ? rawAccidentals : -(rawAccidentals - 7);
        Mode mode = ((data >>> 6) & 0x01) == 0 ? Mode.MAJOR : Mode.MINOR;
        return new KeySignature(accidentals, mode);
    }

    private TimeSignature readTimeSignature(PowerTabByteReader reader) {
        int data = reader.readInt();
        reader.readUnsignedByte(); // pulses per measure: has no place in the model.

        if ((data & COMMON_TIME) != 0) {
            return new TimeSignature(4, 4);
        }
        if ((data & CUT_TIME) != 0) {
            return new TimeSignature(2, 2);
        }
        int beats = ((data >>> 27) & 0x1f) + 1;
        int beatUnit = 1 << ((data >>> 24) & 0x07);
        return new TimeSignature(beats, beatUnit);
    }

    private void skipRehearsalSign(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // letter.
        reader.readMfcString(); // description.
    }
}

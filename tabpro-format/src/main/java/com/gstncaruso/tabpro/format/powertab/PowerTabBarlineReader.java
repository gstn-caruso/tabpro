package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;

/**
 * Lee una barra: su posicion, su tipo, su armadura y su medida. La marca de
 * ensayo que puede traer (letra + descripcion) no tiene lugar en el modelo de
 * tabpro; se consume igual para no perder la sincronia del archivo. Tampoco
 * se distingue si la armadura o la medida estan marcadas para mostrarse o no
 * ("show"/"cancellation"): tabpro siempre las aplica.
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
        reader.readUnsignedByte(); // pulsos por compas: no tiene lugar en el modelo.

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
        reader.readUnsignedByte(); // letra.
        reader.readMfcString(); // descripcion.
    }
}

package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;

/**
 * Lee y valida el encabezado de 256 bytes de un archivo TEF3. TablEdit exige
 * revisar varios campos fijos para confirmar el formato antes de confiar en el
 * resto del archivo: no hay una sola marca magica, sino un puñado de ellas.
 */
final class TabEditHeaderReader {

    private static final int SIZE = 256;

    private static final int OFFSET_MAJOR_VERSION = 3;
    private static final int OFFSET_INITIAL_BPM = 6;
    private static final int OFFSET_MUST_BE_ZERO = 16;
    private static final int MUST_BE_ZERO_LENGTH = 8;
    private static final int OFFSET_MAGIC_TBED = 56;
    private static final int OFFSET_POS_TEXT_EVENTS = 84;
    private static final int OFFSET_POS_CHORDS = 88;
    private static final int OFFSET_POS_READING_LIST = 128;
    private static final int OFFSET_POS_URL = 132;
    private static final int OFFSET_POS_COPYRIGHT = 140;
    private static final int OFFSET_OLD_NUM = 202;
    private static final int OFFSET_FORMAT_LO = 204;
    private static final int OFFSET_FORMAT_HI = 205;

    TabEditHeader read(TabEditByteReader input) {
        byte[] header = input.readBlock(SIZE);
        requireValidHeader(header);

        int initialBpm = shortAt(header, OFFSET_INITIAL_BPM);
        return new TabEditHeader(
                initialBpm,
                intAt(header, OFFSET_POS_TEXT_EVENTS) != 0,
                intAt(header, OFFSET_POS_CHORDS) != 0,
                intAt(header, OFFSET_POS_READING_LIST) != 0,
                intAt(header, OFFSET_POS_URL) != 0,
                intAt(header, OFFSET_POS_COPYRIGHT) != 0);
    }

    /**
     * TablEdit no tiene una sola marca magica: hay que revisar la version mayor,
     * un bloque que siempre es cero, el texto "tbed" y un par de campos fijos
     * de formato. Si alguno no coincide, arriesgar una lectura seria adivinar.
     */
    private static void requireValidHeader(byte[] header) {
        if (header[OFFSET_MAJOR_VERSION] == 3
                && allZero(header, OFFSET_MUST_BE_ZERO, MUST_BE_ZERO_LENGTH)
                && header[OFFSET_MAGIC_TBED] == 't'
                && header[OFFSET_MAGIC_TBED + 1] == 'b'
                && header[OFFSET_MAGIC_TBED + 2] == 'e'
                && header[OFFSET_MAGIC_TBED + 3] == 'd'
                && shortAt(header, OFFSET_OLD_NUM) == 4
                && header[OFFSET_FORMAT_LO] == 4
                && header[OFFSET_FORMAT_HI] == 10) {
            return;
        }
        throw new ScoreFileException(
                "el archivo no se reconoce como TablEdit (formato TEF3): puede ser un TEF v2"
                        + " (anterior a la version 3.00 de TablEdit, que no soportamos) u otro archivo.");
    }

    private static boolean allZero(byte[] bytes, int offset, int length) {
        for (int i = 0; i < length; i++) {
            if (bytes[offset + i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static int shortAt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    private static int intAt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF)
                | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16)
                | ((bytes[offset + 3] & 0xFF) << 24);
    }
}

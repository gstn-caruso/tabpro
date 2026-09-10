package com.gstncaruso.tabpro.format.powertab;

/**
 * Skips, byte by byte, the PowerTab sections that still have no place in the tabpro
 * model: chord diagrams, floating text, dynamics, directions, chord text, and the
 * document's fonts. None of them changes a note's pitch, duration, or string: they are
 * annotations and decoration, so they are read to not lose the file's sync and are
 * discarded on purpose, not by oversight.
 */
final class PowerTabAuxiliaryReader {

    private PowerTabAuxiliaryReader() {
    }

    /** A "chord name": key and bass, formula, modifications, type, and fret. Always 6 bytes. */
    static void skipChordName(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** A "font setting": the typeface name and 15 fixed bytes (size, weight, style, color). */
    static void skipFontSetting(PowerTabByteReader reader) {
        reader.readMfcString();
        reader.skip(15);
    }

    /** A direction: its position and a small vector of 16-bit symbols. */
    static void skipDirection(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // position.
        int count = reader.readUnsignedByte();
        reader.skip(count * 2);
    }

    /** A chord text: its position and a "chord name" (6 fixed bytes). */
    static void skipChordText(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // position.
        skipChordName(reader);
    }

    /** A rhythm slash: position, beaming, and data, all fixed. */
    static void skipRhythmSlash(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** A dynamic: system, staff, position, and volume, all fixed. */
    static void skipDynamic(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** Floating text: the text, a fixed rectangle, a flag, and a "font setting". */
    static void skipFloatingText(PowerTabByteReader reader) {
        reader.readMfcString();
        reader.skip(16); // rectangle: four 32-bit integers.
        reader.readUnsignedByte(); // alignment and border flags.
        skipFontSetting(reader);
    }

    /** A chord diagram: its name (6 fixed bytes), the top fret, and the frets per string. */
    static void skipChordDiagram(PowerTabByteReader reader) {
        skipChordName(reader);
        reader.readUnsignedByte(); // top fret.
        reader.readSmallVectorOfUnsignedBytes();
    }
}

package com.gstncaruso.tabpro.format.tabledit;

import java.util.List;

final class TabEditFixtures {

    static final int HEADER_SIZE = 256;
    private static final int MAX_TRACK_SIZE = 64;

    private TabEditFixtures() {
    }

    static TabEditFileWriter minimalHeader(int initialBpm) {
        TabEditFileWriter writer = new TabEditFileWriter();
        writer.padTo(3);
        int majorVersion = 3;
        writer.writeUnsignedByte(majorVersion);
        writer.padTo(6);
        writer.writeShort(initialBpm);
        writer.padTo(56);
        writer.writeUnsignedByte('t').writeUnsignedByte('b').writeUnsignedByte('e').writeUnsignedByte('d');
        writer.padTo(202);
        int wOldNum = 4;
        int wFormatLo = 4;
        int wFormatHi = 10;
        writer.writeShort(wOldNum);
        writer.writeUnsignedByte(wFormatLo);
        writer.writeUnsignedByte(wFormatHi);
        writer.padTo(HEADER_SIZE);
        return writer;
    }

    static TabEditFileWriter oneTrackOneMeasureScore(String title, int bpm, String trackName, List<Integer> frets) {
        TabEditFileWriter writer = minimalHeader(bpm);
        writeSongMetadata(writer, title, "", "", "");
        writeOneMeasure(writer, 4, 4);
        writeOneTrack(writer, 6, 25, 0, 8, 12, new int[] {64, 59, 55, 50, 45, 40}, trackName);
        writePrintMetadata(writer);
        writeNotesEveryQuarter(writer, 6, frets);
        int endOfFileMarker = -1;
        writer.writeInt(endOfFileMarker);
        return writer;
    }

    static byte[] scoreWithPercussionTrack() {
        TabEditFileWriter writer = minimalHeader(120);
        writeSongMetadata(writer, "With drums", "", "", "");
        writeOneMeasure(writer, 4, 4);
        int percussionMidiInstrument = 96;
        writeOneTrack(writer, 4, percussionMidiInstrument, 0, 8, 8, new int[] {49, 41, 32, 42}, "Drums");
        writePrintMetadata(writer);
        writer.writeInt(-1);
        return writer.bytes();
    }

    private static void writeSongMetadata(TabEditFileWriter writer, String title, String author, String comments, String notes) {
        writer.writeShortString(title);
        writer.writeShortString(author);
        writer.writeShortString(comments);
        writer.writeShortString(notes);
        writer.writeShortString("");
    }

    private static void writeOneMeasure(TabEditFileWriter writer, int numerator, int denominator) {
        int sizeOfMeasure = 12;
        int measureCount = 1;
        writer.writeShort(sizeOfMeasure);
        writer.writeShort(measureCount);
        writer.writeInt(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeSignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(denominator);
        writer.writeUnsignedByte(numerator);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
    }

    private static void writeOneTrack(
            TabEditFileWriter writer, int stringCount, int midiInstrument, int capo, int pan, int volume,
            int[] tuningMidi, String name) {
        int trackCount = 1;
        writer.writeShort(MAX_TRACK_SIZE);
        writer.writeShort(trackCount);
        int start = writer.size();
        writer.writeUnsignedByte(stringCount);
        writer.padTo(start + 8);
        writer.writeUnsignedByte(midiInstrument);
        writer.padTo(start + 11);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(capo);
        writer.padTo(start + 14);
        int centralCOffset = 12;
        writer.writeUnsignedByte(centralCOffset);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0x30);
        writer.writeUnsignedByte(pan);
        writer.writeUnsignedByte(volume);
        writer.writeUnsignedByte(0);
        for (int midi : tuningMidi) {
            writer.writeUnsignedByte(96 - midi);
        }
        writer.padTo(start + 20 + 12);
        writer.writeNullTerminatedString(name);
        writer.padTo(start + MAX_TRACK_SIZE);
    }

    private static void writePrintMetadata(TabEditFileWriter writer) {
        int printDataLength = 4;
        writer.writeUnsignedByte(printDataLength);
        writer.writeUnsignedByte(1);
        writer.padTo(writer.size() + printDataLength);
        int maxHeaderSize = 128;
        writer.padTo(writer.size() + maxHeaderSize);
        writer.padTo(writer.size() + maxHeaderSize);
    }

    private static void writeNotesEveryQuarter(TabEditFileWriter writer, int durationCode, List<Integer> frets) {
        int valuePerPosition = 32 * 6;
        int sixthStringZeroBased = 5;
        int gridPositionsPerQuarterNote = 4;
        int gridPosition = 0;
        for (int fret : frets) {
            writer.writeInt(gridPosition * valuePerPosition + sixthStringZeroBased * 8);
            writer.writeUnsignedByte(fret + 1);
            writer.writeUnsignedByte(durationCode);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            gridPosition += gridPositionsPerQuarterNote;
        }
    }
}

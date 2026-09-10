package com.gstncaruso.tabpro.app.audit;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A minimal, valid TEF3 file, built by hand: there is no real TablEdit sample in the repository.
 * This is the same binary layout that the real reader ({@code TabEditByteReader}) understands,
 * reduced to one guitar track in standard tuning with a single 4/4 measure.
 */
final class TabEditMinimalFixture {

    private static final int HEADER_SIZE = 256;
    private static final int MAX_TRACK_SIZE = 64;

    private TabEditMinimalFixture() {
    }

    static byte[] oneTrackOneMeasureScore(String title, int bpm, String trackName, List<Integer> frets) {
        Writer writer = minimalHeader(bpm);
        writeSongMetadata(writer, title, "", "", "");
        writeOneMeasure(writer, 4, 4);
        writeOneTrack(writer, 6, 25, 0, 8, 12, new int[] {64, 59, 55, 50, 45, 40}, trackName);
        writePrintMetadata(writer);
        writeNotesEveryQuarter(writer, 6, frets);
        writer.writeInt(-1);
        return writer.bytes();
    }

    private static Writer minimalHeader(int initialBpm) {
        Writer writer = new Writer();
        writer.padTo(3);
        writer.writeUnsignedByte(3);
        writer.padTo(6);
        writer.writeShort(initialBpm);
        writer.padTo(56);
        writer.writeUnsignedByte('t').writeUnsignedByte('b').writeUnsignedByte('e').writeUnsignedByte('d');
        writer.padTo(202);
        writer.writeShort(4);
        writer.writeUnsignedByte(4);
        writer.writeUnsignedByte(10);
        writer.padTo(HEADER_SIZE);
        return writer;
    }

    private static void writeSongMetadata(Writer writer, String title, String author, String comments, String notes) {
        writer.writeShortString(title);
        writer.writeShortString(author);
        writer.writeShortString(comments);
        writer.writeShortString(notes);
        writer.writeShortString("");
    }

    private static void writeOneMeasure(Writer writer, int numerator, int denominator) {
        writer.writeShort(12);
        writer.writeShort(1);
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
            Writer writer, int stringCount, int midiInstrument, int capo, int pan, int volume,
            int[] tuningMidi, String name) {
        writer.writeShort(MAX_TRACK_SIZE);
        writer.writeShort(1);
        int start = writer.size();
        writer.writeUnsignedByte(stringCount);
        writer.padTo(start + 8);
        writer.writeUnsignedByte(midiInstrument);
        writer.padTo(start + 11);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(capo);
        writer.padTo(start + 14);
        writer.writeUnsignedByte(12);
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

    private static void writePrintMetadata(Writer writer) {
        int printDataLength = 4;
        writer.writeUnsignedByte(printDataLength);
        writer.writeUnsignedByte(1);
        writer.padTo(writer.size() + printDataLength);
        int maxHeaderSize = 128;
        writer.padTo(writer.size() + maxHeaderSize);
        writer.padTo(writer.size() + maxHeaderSize);
    }

    private static void writeNotesEveryQuarter(Writer writer, int durationCode, List<Integer> frets) {
        int valuePerPosition = 32 * 6;
        int gridPosition = 0;
        for (int fret : frets) {
            writer.writeInt(gridPosition * valuePerPosition + 5 * 8);
            writer.writeUnsignedByte(fret + 1);
            writer.writeUnsignedByte(durationCode);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            writer.writeUnsignedByte(0);
            gridPosition += 4;
        }
    }

    private static final class Writer {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] bytes() {
            return buffer.toByteArray();
        }

        int size() {
            return buffer.size();
        }

        Writer writeUnsignedByte(int value) {
            buffer.write(value & 0xFF);
            return this;
        }

        Writer writeSignedByte(int value) {
            return writeUnsignedByte(value & 0xFF);
        }

        Writer writeShort(int value) {
            writeUnsignedByte(value & 0xFF);
            writeUnsignedByte((value >> 8) & 0xFF);
            return this;
        }

        Writer writeInt(int value) {
            writeUnsignedByte(value & 0xFF);
            writeUnsignedByte((value >> 8) & 0xFF);
            writeUnsignedByte((value >> 16) & 0xFF);
            writeUnsignedByte((value >> 24) & 0xFF);
            return this;
        }

        Writer writeShortString(String text) {
            byte[] raw = ascii(text);
            writeShort(raw.length);
            buffer.writeBytes(raw);
            return this;
        }

        Writer writeNullTerminatedString(String text) {
            buffer.writeBytes(ascii(text));
            writeUnsignedByte(0);
            return this;
        }

        Writer padTo(int totalSize) {
            while (buffer.size() < totalSize) {
                writeUnsignedByte(0);
            }
            return this;
        }

        private static byte[] ascii(String text) {
            return text.getBytes(StandardCharsets.ISO_8859_1);
        }
    }
}

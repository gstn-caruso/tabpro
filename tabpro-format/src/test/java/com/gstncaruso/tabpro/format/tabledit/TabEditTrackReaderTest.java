package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.DefaultNames;
import com.gstncaruso.tabpro.format.TestDefaultNames;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabEditTrackReaderTest {

    private static final int MAX_TRACK_SIZE = 64;

    private final DefaultNames names = new TestDefaultNames();
    private final TabEditTrackReader reader = new TabEditTrackReader(names);

    @Test
    void readsTheMidiTuningAndTheTrackName() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(1);
        writeTrack(writer, 6, 25, 0, new int[] {64, 59, 55, 50, 45, 40}, "Guitar");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(1, tracks.size());
        TabEditTrackHeader track = tracks.get(0);
        assertEquals("Guitar", track.name());
        assertEquals(6, track.stringCount());
        assertEquals(List.of(64, 59, 55, 50, 45, 40), track.tuningMidiNumbers());
        assertEquals(25, track.midiInstrument());
        assertFalse(track.percussion());
    }

    @Test
    void readsTheCapoAndRecognizesPercussionByInstrument96() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(2);
        writeTrack(writer, 6, 25, 3, new int[] {32, 37, 41, 46, 51, 56}, "With capo");
        writeTrack(writer, 4, 96, 0, new int[] {60, 55, 50, 45}, "Drums");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(3, tracks.get(0).capo());
        assertFalse(tracks.get(0).percussion());
        assertTrue(tracks.get(1).percussion());
    }

    @Test
    void severalTracksStayAlignedEachInItsOwnBlock() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(2);
        writeTrack(writer, 4, 33, 0, new int[] {45, 50, 55, 60}, "Bass");
        writeTrack(writer, 6, 25, 0, new int[] {32, 37, 41, 46, 51, 56}, "Guitar 2");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals("Bass", tracks.get(0).name());
        assertEquals(4, tracks.get(0).stringCount());
        assertEquals("Guitar 2", tracks.get(1).name());
        assertEquals(6, tracks.get(1).stringCount());
    }

    @Test
    void aTrackWithABlankNameGetsTheInjectedUnnamedTrackName() {
        TabEditFileWriter writer = new TabEditFileWriter().writeShort(MAX_TRACK_SIZE).writeShort(1);
        writeTrack(writer, 6, 25, 0, new int[] {64, 59, 55, 50, 45, 40}, "");

        List<TabEditTrackHeader> tracks = reader.read(new TabEditByteReader(writer.bytes()));

        assertEquals(names.unnamedTrack(), tracks.get(0).name());
    }

    private static void writeTrack(
            TabEditFileWriter writer, int stringCount, int midiInstrument, int capo, int[] tuningMidi,
            String name) {
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
        writer.writeUnsignedByte(8);
        writer.writeUnsignedByte(12);
        writer.writeUnsignedByte(0);
        for (int semitonesBelow96 : tuningMidi) {
            writer.writeUnsignedByte(96 - semitonesBelow96);
        }
        writer.padTo(start + 20 + 12);
        writer.writeNullTerminatedString(name);
        writer.padTo(start + MAX_TRACK_SIZE);
    }
}

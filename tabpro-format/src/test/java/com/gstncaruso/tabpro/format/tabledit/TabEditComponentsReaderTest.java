package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.List;
import org.junit.jupiter.api.Test;

class TabEditComponentsReaderTest {

    private static final List<TabEditMeasure> ONE_MEASURE_44 =
            List.of(new TabEditMeasure(TimeSignature.fourFour(), KeySignature.cMajor()));
    private static final List<Integer> ONE_TRACK_SIX_STRINGS = List.of(6);

    private final TabEditComponentsReader reader = new TabEditComponentsReader();

    @Test
    void readsANoteAndARestAndValidatesTheFooter() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeNote(writer, 0, 3, 6);
        writeRest(writer, 32 * 6, 9);
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof TabEditNoteEvent);
        assertEquals(3, ((TabEditNoteEvent) events.get(0)).note().fret());
        assertTrue(events.get(1) instanceof TabEditRestEvent);
        assertEquals(NoteValue.EIGHTH, events.get(1).duration().value());
    }

    @Test
    void discardsKnownButUnsupportedComponentsWithoutLosingAlignment() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeUnsupported(writer, 0xFE);
        writeUnsupported(writer, 0xB7);
        writeUnsupported(writer, 0x35);
        writeNote(writer, 0, 5, 6);
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(1, events.size());
        assertEquals(5, ((TabEditNoteEvent) events.get(0)).note().fret());
    }

    @Test
    void discardsGraceNotesForNow() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeGraceNote(writer, 0, 2);
        writeNote(writer, 32 * 6, 5, 6);
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(1, events.size());
        assertEquals(5, ((TabEditNoteEvent) events.get(0)).note().fret());
    }

    @Test
    void anUnknownComponentTypeIsReportedInsteadOfIgnored() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeUnsupported(writer, 0x60);
        writeFooter(writer);

        ScoreFileException exception = assertThrows(ScoreFileException.class,
                () -> reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS));

        assertEquals(ScoreFileProblem.DAMAGED, exception.problem());
        assertEquals("unknown TablEdit component: type 0x60", exception.getMessage());
    }

    @Test
    void aFooterThatIsNotTheExpectedOneIsReported() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeNote(writer, 0, 3, 6);
        writer.writeInt(0);

        ScoreFileException exception = assertThrows(ScoreFileException.class,
                () -> reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS));

        assertEquals(ScoreFileProblem.DAMAGED, exception.problem());
    }

    private static void writeNote(TabEditFileWriter writer, int location, int fret, int durationCode) {
        writer.writeInt(location);
        writer.writeUnsignedByte(fret + 1);
        writer.writeUnsignedByte(durationCode & 0x1F);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
    }

    private static void writeGraceNote(TabEditFileWriter writer, int location, int fret) {
        writer.writeInt(location);
        writer.writeUnsignedByte((fret + 1) | 0x40);
        writer.writeUnsignedByte(12);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
    }

    private static void writeRest(TabEditFileWriter writer, int location, int durationCode) {
        writer.writeInt(location);
        writer.writeUnsignedByte(0x33);
        writer.writeUnsignedByte(durationCode);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
    }

    private static void writeUnsupported(TabEditFileWriter writer, int type) {
        writer.writeInt(0);
        writer.writeUnsignedByte(type);
        for (int i = 0; i < 7; i++) {
            writer.writeUnsignedByte(0);
        }
    }

    private static void writeFooter(TabEditFileWriter writer) {
        writer.writeInt(-1);
    }
}

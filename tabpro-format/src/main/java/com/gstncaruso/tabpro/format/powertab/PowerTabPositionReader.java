package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a PowerTab position: its note value, whether it is a rest, and the notes that
 * sound on it. The note-value type is stored as its denominator (1, 2, 4, 8...), same
 * as {@link NoteValue#denominator()}, so there is no lookup table to translate like in
 * Guitar Pro. Double dots have nowhere to go in the tabpro model (which only knows a
 * single dot): they are approximated to one. Irregular grouping (triplets and the like)
 * is not applied in this version either: the note value is read at its nominal length.
 */
final class PowerTabPositionReader {

    private static final int MAX_COMPLEX_SYMBOLS = 2;

    private static final int FLAG_DOTTED = 0x01;
    private static final int FLAG_DOUBLE_DOTTED = 0x02;
    private static final int FLAG_REST = 0x04;

    private static final int SYMBOL_MULTIBAR_REST = 'j';

    private final PowerTabNoteReader noteReader = new PowerTabNoteReader();

    PowerTabPosition read(PowerTabByteReader reader) {
        int index = reader.readUnsignedByte();
        reader.readUnsignedShort(); // beaming and irregular grouping: not applied in this version.
        int data = reader.readInt();
        int[] symbols = reader.readSmallFixedArrayOfInts(MAX_COMPLEX_SYMBOLS);

        NoteValue value = noteValueOf((data >>> 24) & 0xFF);
        boolean dotted = (data & (FLAG_DOTTED | FLAG_DOUBLE_DOTTED)) != 0;
        boolean rest = (data & FLAG_REST) != 0;
        Duration duration = new Duration(value, dotted);

        List<Note> played = readNotes(reader);
        Beat beat = rest ? Beat.rest(duration) : new Beat(duration, played);

        return new PowerTabPosition(index, beat, multibarRestMeasureCountOf(symbols));
    }

    private List<Note> readNotes(PowerTabByteReader reader) {
        int noteCount = reader.readCount();
        List<Note> notes = new ArrayList<>(noteCount);
        for (int i = 0; i < noteCount; i++) {
            reader.readClassInformation();
            notes.add(noteReader.read(reader));
        }
        return notes;
    }

    private static int multibarRestMeasureCountOf(int[] symbols) {
        for (int symbol : symbols) {
            if (symbol != 0 && ((symbol >>> 24) & 0xFF) == SYMBOL_MULTIBAR_REST) {
                return symbol & 0xFF;
            }
        }
        return 0;
    }

    private static NoteValue noteValueOf(int durationType) {
        for (NoteValue value : NoteValue.values()) {
            if (value.denominator() == durationType) {
                return value;
            }
        }
        throw ScoreFileException.damaged("unknown note value in a PowerTab position: " + durationType);
    }
}

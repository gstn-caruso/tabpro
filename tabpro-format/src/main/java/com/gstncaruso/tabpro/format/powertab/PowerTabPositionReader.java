package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee una posicion de PowerTab: su figura, si es un silencio, y las notas que
 * suenan en ella. El tipo de figura se guarda como su denominador (1, 2, 4,
 * 8...), igual que {@link NoteValue#denominator()}, asi que no hace falta
 * traducir una tabla como en Guitar Pro. El doble puntillo no tiene donde ir
 * en el modelo de tabpro (que solo conoce un puntillo): se aproxima a uno
 * solo. El agrupamiento irregular (tresillos y demas) tampoco se aplica en
 * esta version: la figura se lee a su valor nominal.
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
        reader.readUnsignedShort(); // beaming y agrupamiento irregular: no se aplica en esta version.
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
        throw new ScoreFileException("figura desconocida en una posicion de PowerTab: " + durationType);
    }
}

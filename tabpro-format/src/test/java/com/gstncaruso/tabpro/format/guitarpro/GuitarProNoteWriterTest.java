package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import org.junit.jupiter.api.Test;

/** En Guitar Pro 4 la nota de adorno va traste, dinamica, duracion y recien despues transicion. */
class GuitarProNoteWriterTest {

    /** Traste, tipo, dinamica y traste de la nota, mas los dos bytes de banderas del efecto. */
    private static final int BYTES_BEFORE_THE_GRACE_NOTE = 6;

    private static final int THIRTY_SECOND = 1;
    private static final int HAMMER = 3;

    private final GuitarProNoteWriter writer = new GuitarProNoteWriter();

    @Test
    void laDuracionDelAdornoVaAntesQueLaTransicion() {
        GraceNote grace = new GraceNote(7, NoteValue.THIRTY_SECOND, Dynamic.FORTE, GraceTransition.HAMMER,
                false, false);

        byte[] bytes = write(new Note(1, 5, false, NoteEffects.none().withGrace(grace)));

        int at = BYTES_BEFORE_THE_GRACE_NOTE;
        assertEquals(7, bytes[at] & 0xFF, "traste del adorno");
        assertEquals(Dynamic.FORTE.ordinal() + 1, bytes[at + 1] & 0xFF, "dinamica del adorno");
        assertEquals(THIRTY_SECOND, bytes[at + 2] & 0xFF, "duracion del adorno");
        assertEquals(HAMMER, bytes[at + 3] & 0xFF, "transicion del adorno");
    }

    private byte[] write(Note note) {
        GuitarProByteWriter bytes = new GuitarProByteWriter();
        writer.write(bytes, note);
        return bytes.bytes();
    }
}

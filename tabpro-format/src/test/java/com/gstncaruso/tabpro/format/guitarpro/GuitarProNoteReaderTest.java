package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import org.junit.jupiter.api.Test;

/**
 * La nota de adorno no se guarda igual en todas las generaciones: hasta GP4 la duracion
 * va antes que la transicion, y desde GP5 el orden se invierte.
 */
class GuitarProNoteReaderTest {

    private static final int WITH_EFFECTS = 0x08;
    private static final int HAS_GRACE = 0x10;
    private static final int NO_MORE_EFFECTS = 0x00;

    private static final int FORTE = 6;
    private static final int THIRTY_SECOND = 1;
    private static final int SIXTEENTH = 3;
    private static final int HAMMER = 3;
    private static final int SLIDE = 1;

    private final GuitarProNoteReader reader = new GuitarProNoteReader();

    @Test
    void enGp4LaDuracionDelAdornoVaAntesQueLaTransicion() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(HAS_GRACE).writeUnsignedByte(NO_MORE_EFFECTS)
                .writeUnsignedByte(3).writeUnsignedByte(FORTE)
                .writeUnsignedByte(THIRTY_SECOND).writeUnsignedByte(HAMMER),
                GuitarProVersion.GP4));

        assertEquals(3, grace.fret());
        assertEquals(NoteValue.THIRTY_SECOND, grace.duration());
        assertEquals(GraceTransition.HAMMER, grace.transition());
    }

    @Test
    void enGp3TambienLaDuracionVaPrimero() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(HAS_GRACE)
                .writeUnsignedByte(5).writeUnsignedByte(FORTE)
                .writeUnsignedByte(SIXTEENTH).writeUnsignedByte(SLIDE),
                GuitarProVersion.GP3));

        assertEquals(NoteValue.SIXTEENTH, grace.duration());
        assertEquals(GraceTransition.SLIDE, grace.transition());
    }

    @Test
    void enGp5LaTransicionVaAntesQueLaDuracion() {
        GraceNote grace = graceOf(read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00) // el byte de banderas propio de la nota en GP5
                .writeUnsignedByte(HAS_GRACE).writeUnsignedByte(NO_MORE_EFFECTS)
                .writeUnsignedByte(3).writeUnsignedByte(FORTE)
                .writeUnsignedByte(HAMMER).writeUnsignedByte(THIRTY_SECOND)
                .writeUnsignedByte(0x00), // banderas del adorno: ni muerto ni en el tiempo
                GuitarProVersion.GP5_00));

        assertEquals(NoteValue.THIRTY_SECOND, grace.duration());
        assertEquals(GraceTransition.HAMMER, grace.transition());
    }

    private static GraceNote graceOf(Note note) {
        return note.effects().grace().orElseThrow();
    }

    private Note read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, 1);
    }
}

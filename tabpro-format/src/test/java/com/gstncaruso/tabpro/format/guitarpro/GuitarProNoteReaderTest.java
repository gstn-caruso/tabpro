package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    /** El bit del segundo byte de efectos de la nota que anuncia el slide. */
    private static final int HAS_SLIDE = 0x08;

    /** El bit de la nota que dice que trae su propia dinamica, y el codigo del piano. */
    private static final int NO_FLAGS = 0x00;
    private static final int HAS_DYNAMIC = 0x10;
    private static final int PIANO = 3;

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

    // ---- la dinamica, que solo se escribe cuando no es la de siempre --------

    /**
     * Guitar Pro no escribe la dinamica de la nota cuando es la suya por defecto, que es
     * forte. Darla por mezzo forte deja toda nota sin marcar sonando mas suave de lo que
     * pide el archivo, sin que nada avise.
     */
    @Test
    void aNoteWithoutItsOwnDynamicIsForte() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS),
                GuitarProVersion.GP4);

        assertEquals(Dynamic.FORTE, note.effects().dynamic());
    }

    @Test
    void aNoteThatBringsItsDynamicKeepsIt() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_DYNAMIC)
                .writeSignedByte(PIANO),
                GuitarProVersion.GP4);

        assertEquals(Dynamic.PIANO, note.effects().dynamic());
    }

    // ---- el slide, que cambia de codificacion entre GP4 y GP5 ---------------

    /**
     * Hasta GP4 el slide es un numero: uno de seis, y solo uno por nota. Los que entran
     * o salen de la nota van en negativo o arriba del legato.
     */
    @ParameterizedTest
    @CsvSource({"1, SHIFT", "2, LEGATO", "3, OUT_DOWNWARDS", "4, OUT_UPWARDS",
            "-1, IN_FROM_BELOW", "-2, IN_FROM_ABOVE"})
    void gp4WritesTheSlideAsASingleNumber(int written, SlideType expected) {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeSignedByte(written),
                GuitarProVersion.GP4);

        assertEquals(expected, note.effects().slide().orElseThrow());
    }

    /**
     * En GP5 el mismo byte pasa a ser una mascara de bits, para que una nota pueda traer
     * varios slides a la vez. Los dos primeros bits coinciden por casualidad con los
     * numeros de GP4; los otros cuatro no, y leerlos como numero los pierde en silencio.
     */
    @ParameterizedTest
    @CsvSource({"0x01, SHIFT", "0x02, LEGATO", "0x04, OUT_DOWNWARDS", "0x08, OUT_UPWARDS",
            "0x10, IN_FROM_BELOW", "0x20, IN_FROM_ABOVE"})
    void gp5WritesTheSlideAsAMaskOfBits(int written, SlideType expected) {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00) // el byte de banderas propio de la nota en GP5
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(written),
                GuitarProVersion.GP5_10);

        assertEquals(expected, note.effects().slide().orElseThrow());
    }

    /** El modelo guarda un solo slide por nota: de los que trae la mascara vale el primero. */
    @Test
    void aGp5NoteWithSeveralSlidesKeepsTheFirstOne() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(0x01 | 0x08),
                GuitarProVersion.GP5_10);

        assertEquals(SlideType.SHIFT, note.effects().slide().orElseThrow());
    }

    @Test
    void aGp5NoteWithoutSlidesInItsMaskHasNoSlide() {
        Note note = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_EFFECTS)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_MORE_EFFECTS).writeUnsignedByte(HAS_SLIDE)
                .writeUnsignedByte(0x00),
                GuitarProVersion.GP5_10);

        assertTrue(note.effects().slide().isEmpty());
    }

    private static GraceNote graceOf(Note note) {
        return note.effects().grace().orElseThrow();
    }

    private Note read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, 1);
    }
}

package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Cada componente es un registro de tamano fijo (12 bytes): una posicion, un
 * byte de tipo y siete de carga util. Lo que no es nota ni silencio se
 * descarta sabiendo exactamente que es, sin arriesgar la alineacion del pie
 * del archivo.
 */
class TabEditComponentsReaderTest {

    private static final List<TabEditMeasure> ONE_MEASURE_44 =
            List.of(new TabEditMeasure(TimeSignature.fourFour(), KeySignature.cMajor()));
    private static final List<Integer> ONE_TRACK_SIX_STRINGS = List.of(6);

    private final TabEditComponentsReader reader = new TabEditComponentsReader();

    @Test
    void leeUnaNotaYUnSilencioYValidaElPie() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeNote(writer, 0, 3, 6); // compas 0, posicion 0, cuerda 0, traste 3, negra
        writeRest(writer, 32 * 6, 9); // siguiente posicion, corchea
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof TabEditNoteEvent);
        assertEquals(3, ((TabEditNoteEvent) events.get(0)).note().fret());
        assertTrue(events.get(1) instanceof TabEditRestEvent);
        assertEquals(NoteValue.EIGHTH, events.get(1).duration().value());
    }

    @Test
    void descartaComponentesConocidosPeroNoSoportadosSinPerderLaAlineacion() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeUnsupported(writer, 0xFE); // cambio de tempo
        writeUnsupported(writer, 0xB7); // final alternativo / repeticion
        writeUnsupported(writer, 0x35); // acorde
        writeNote(writer, 0, 5, 6);
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(1, events.size());
        assertEquals(5, ((TabEditNoteEvent) events.get(0)).note().fret());
    }

    @Test
    void descartaLasNotasDeAdornoPorAhora() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeGraceNote(writer, 0, 2);
        writeNote(writer, 32 * 6, 5, 6);
        writeFooter(writer);

        List<TabEditEvent> events = reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS);

        assertEquals(1, events.size());
        assertEquals(5, ((TabEditNoteEvent) events.get(0)).note().fret());
    }

    @Test
    void unTipoDeComponenteDesconocidoSeDeclaraEnVezDeIgnorarse() {
        // 0x60: sus 5 bits bajos dan 0, asi que ni siquiera cae en el rango de nota
        // (que exige un valor entre 1 y 0x19), y no es ninguno de los tipos conocidos.
        TabEditFileWriter writer = new TabEditFileWriter();
        writeUnsupported(writer, 0x60);
        writeFooter(writer);

        ScoreFileException exception = assertThrows(ScoreFileException.class,
                () -> reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS));

        assertTrue(exception.getMessage().contains("0x60") || exception.getMessage().contains("desconocido"));
    }

    @Test
    void unPieDeArchivoQueNoEsElEsperadoSeDeclara() {
        TabEditFileWriter writer = new TabEditFileWriter();
        writeNote(writer, 0, 3, 6);
        writer.writeInt(0); // pie invalido: deberia ser -1 (0xFFFFFFFF)

        assertThrows(ScoreFileException.class,
                () -> reader.read(new TabEditByteReader(writer.bytes()), ONE_MEASURE_44, ONE_TRACK_SIX_STRINGS));
    }

    private static void writeNote(TabEditFileWriter writer, int location, int fret, int durationCode) {
        writer.writeInt(location);
        writer.writeUnsignedByte(fret + 1); // byte de tipo: traste+1, sin nota de adorno
        writer.writeUnsignedByte(durationCode & 0x1F); // duracion; dinamica FFF
        writer.writeUnsignedByte(0); // efecto1/atributos/alteraciones
        writer.writeUnsignedByte(0); // traste de adorno / efecto de adorno
        writer.writeUnsignedByte(0); // efecto2/efecto3
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
        writer.writeUnsignedByte(0);
    }

    private static void writeGraceNote(TabEditFileWriter writer, int location, int fret) {
        writer.writeInt(location);
        writer.writeUnsignedByte((fret + 1) | 0x40); // bit 6: es una nota de adorno
        writer.writeUnsignedByte(12); // negra
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

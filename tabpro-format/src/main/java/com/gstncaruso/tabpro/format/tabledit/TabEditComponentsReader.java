package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Note;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee la lista de componentes del final del archivo: cada uno es un registro
 * de 12 bytes de tamano fijo (una posicion, un byte de tipo y siete de carga
 * util), asi que lo que no es nota ni silencio se puede descartar sin perder
 * la alineacion. Un tipo que no reconocemos en absoluto se declara con una
 * excepcion en vez de arriesgar una interpretacion a ciegas.
 */
final class TabEditComponentsReader {

    private static final int RECORD_SIZE = 12;
    private static final int FOOTER_SIZE = 4;
    private static final int EXPECTED_FOOTER = -1;

    private static final int TYPE_REST = 0x33;

    /**
     * Tipos que reconocemos pero todavia no traducimos al modelo de tabpro:
     * acorde, salto de linea, acento, crescendo, evento de texto, conexion
     * (ligado grafico con duracion y corchete), diagrama de escala, cambio de
     * bateria, marca de espaciado o metadatos de nota de adorno (comparten el
     * mismo tipo), cambio de voz/instrumento, simbolo, final alternativo (las
     * repeticiones), corte de corchete, largo de plica y sincopa.
     */
    private static final int[] KNOWN_BUT_UNSUPPORTED_TYPES = {
            0x35, 0x36, 0x37, 0x38, 0x39, 0x3D, 0x75, 0x78, 0x7D, 0x7E, 0xB6, 0xB7, 0xBD, 0xBE, 0xFD, 0xFE,
    };

    private final TabEditNoteReader noteReader = new TabEditNoteReader();
    private final TabEditRestReader restReader = new TabEditRestReader();

    List<TabEditEvent> read(TabEditByteReader input, List<TabEditMeasure> measures, List<Integer> trackStringCounts) {
        List<TabEditEvent> events = new ArrayList<>();

        while (input.remaining() >= RECORD_SIZE) {
            TabEditByteReader record = new TabEditByteReader(input.readBlock(RECORD_SIZE));
            int location = record.readInt();
            int type = record.readUnsignedByte();
            TabEditPosition position = TabEditPosition.fromLocation(location, measures, trackStringCounts);

            if (type == TYPE_REST) {
                TabEditRestFields fields = restReader.read(record);
                events.add(new TabEditRestEvent(position, fields.duration(), fields.voice()));
            } else if (isKnownButUnsupported(type)) {
                // El bloque de 12 bytes ya quedo consumido entero: no hace falta leer el resto.
            } else if (isNoteType(type)) {
                TabEditNoteFields fields = noteReader.read(record, type);
                if (fields.isGraceNote()) {
                    // TablEdit guarda la nota de adorno como un evento propio, con su propia
                    // posicion; tabpro solo sabe adornar la nota principal con una nota de
                    // adorno previa. Fusionarlas a mano seria adivinar cual es "la principal",
                    // asi que por ahora se declara sin soportar y se descarta.
                    continue;
                }
                events.add(noteEventOf(position, fields));
            } else {
                throw new ScoreFileException(
                        String.format("componente de TablEdit desconocido: tipo 0x%02X", type));
            }
        }

        if (input.remaining() != FOOTER_SIZE) {
            throw new ScoreFileException(
                    "el pie del archivo de TablEdit no tiene el tamano esperado: quedan "
                            + input.remaining() + " bytes sueltos.");
        }
        if (input.readInt() != EXPECTED_FOOTER) {
            throw new ScoreFileException("el pie del archivo de TablEdit no es el esperado.");
        }

        return events;
    }

    private static TabEditNoteEvent noteEventOf(TabEditPosition position, TabEditNoteFields fields) {
        int fret = Math.clamp(fields.fret(), 0, Note.MAX_FRET);
        Note note = new Note(position.stringZeroBased() + 1, fret, fields.tied(), fields.effects());
        return new TabEditNoteEvent(position, fields.duration(), fields.voice(), note, fields.tapping(),
                fields.slapping(), fields.fadeIn());
    }

    /** El rango de nota vale para cualquier byte cuyos 5 bits bajos caigan ahi, mas alla de los otros bits. */
    private static boolean isNoteType(int type) {
        int lowerBits = type & 0x1F;
        return lowerBits > 0 && lowerBits <= 0x19;
    }

    private static boolean isKnownButUnsupported(int type) {
        for (int known : KNOWN_BUT_UNSUPPORTED_TYPES) {
            if (known == type) {
                return true;
            }
        }
        return false;
    }
}

package com.gstncaruso.tabpro.format.tabledit;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee el encabezado de cada pista: un registro de tamano fijo del que hacen
 * falta la cantidad de cuerdas, el instrumento, la cejilla, el pan y el
 * volumen, la afinacion y el nombre. La pinza (clave, corchete de gran
 * pentagrama, doble cuerda, pedal steel, pista de ritmo) no tiene donde vivir
 * en el modelo de tabpro y se descarta junto con el resto del relleno.
 */
final class TabEditTrackReader {

    /** El instrumento MIDI que TablEdit usa como marca de "esto es una pista de percusion". */
    private static final int PERCUSSION_MIDI_INSTRUMENT = 96;

    private static final int TUNING_SLOTS = 12;

    /** Un numero MIDI de referencia: el byte crudo de afinacion es cuanto mas grave suena esa cuerda. */
    private static final int TUNING_REFERENCE_MIDI_NUMBER = 96;

    List<TabEditTrackHeader> read(TabEditByteReader input) {
        int maxTrackSize = input.readUnsignedShort();
        int trackCount = input.readUnsignedShort();

        List<TabEditTrackHeader> tracks = new ArrayList<>(trackCount);
        for (int i = 0; i < trackCount; i++) {
            tracks.add(readOne(new TabEditByteReader(input.readBlock(maxTrackSize)), maxTrackSize));
        }
        return tracks;
    }

    private TabEditTrackHeader readOne(TabEditByteReader record, int maxTrackSize) {
        int stringCount = record.readUnsignedByte();
        record.skip(7);
        int midiInstrument = record.readUnsignedByte();
        record.skip(2);
        record.skip(1); // transposicion: no tiene donde vivir en el modelo de tabpro.
        int capo = record.readUnsignedByte();
        record.skip(1);
        record.skip(1); // desplazamiento del Do central: solo afecta el dibujo en pentagrama.
        record.skip(1); // clave, gran pentagrama, corchete: idem.
        record.skip(1);
        int pan = record.readUnsignedByte();
        int volume = record.readUnsignedByte();
        record.skip(1); // doble cuerda, let ring de pista, pedal steel, pista de ritmo: idem.

        List<Integer> tuning = new ArrayList<>(stringCount);
        for (int string = 0; string < stringCount; string++) {
            tuning.add(TUNING_REFERENCE_MIDI_NUMBER - record.readUnsignedByte());
        }
        record.skip(TUNING_SLOTS - stringCount);

        String name = record.readNullTerminatedString(maxTrackSize);

        return new TabEditTrackHeader(
                name.isBlank() ? "Pista" : name, stringCount, List.copyOf(tuning), midiInstrument, capo, pan, volume,
                midiInstrument == PERCUSSION_MIDI_INSTRUMENT);
    }
}

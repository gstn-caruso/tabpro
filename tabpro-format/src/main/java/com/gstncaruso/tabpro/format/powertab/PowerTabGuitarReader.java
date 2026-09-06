package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee una guitarra: su descripcion, los parametros de su canal y su afinacion.
 * El numero de la guitarra y el nombre y los datos de la afinacion (sostenidos
 * o bemoles, corrimiento de notacion) no tienen lugar en el modelo de tabpro:
 * la afinacion se identifica por sus notas, no por su nombre.
 */
final class PowerTabGuitarReader {

    PowerTabGuitar read(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // numero: el orden del vector ya lo da.
        String description = reader.readMfcString();
        int preset = reader.readUnsignedByte();
        int initialVolume = reader.readUnsignedByte();
        int pan = reader.readUnsignedByte();
        int reverb = reader.readUnsignedByte();
        int chorus = reader.readUnsignedByte();
        int tremolo = reader.readUnsignedByte();
        int phaser = reader.readUnsignedByte();
        int capo = reader.readUnsignedByte();
        List<Integer> tuning = readTuning(reader);
        return new PowerTabGuitar(description, tuning, preset, initialVolume, pan, reverb, chorus, tremolo, phaser, capo);
    }

    private List<Integer> readTuning(PowerTabByteReader reader) {
        reader.readMfcString(); // nombre de la afinacion.
        reader.readUnsignedByte(); // sostenidos/bemoles y corrimiento de notacion.
        int[] notes = reader.readSmallVectorOfUnsignedBytes();
        List<Integer> midiNotes = new ArrayList<>(notes.length);
        for (int note : notes) {
            midiNotes.add(note);
        }
        return midiNotes;
    }
}

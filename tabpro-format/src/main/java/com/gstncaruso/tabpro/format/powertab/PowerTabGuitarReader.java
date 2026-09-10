package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads a guitar: its description, its channel parameters, and its tuning. The guitar
 * number and the tuning's name and data (sharps or flats, notation shift) have no
 * place in the tabpro model: the tuning is identified by its notes, not by its name.
 */
final class PowerTabGuitarReader {

    PowerTabGuitar read(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // number: the vector order already gives it.
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
        reader.readMfcString(); // tuning name.
        reader.readUnsignedByte(); // sharps/flats and notation shift.
        int[] notes = reader.readSmallVectorOfUnsignedBytes();
        List<Integer> midiNotes = new ArrayList<>(notes.length);
        for (int note : notes) {
            midiNotes.add(note);
        }
        return midiNotes;
    }
}

package com.gstncaruso.tabpro.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/** Los dispositivos MIDI que ofrece la maquina, separados por lo que saben hacer. */
public final class MidiDevices {

    private MidiDevices() {
    }

    /** Los que reciben notas: sintetizadores y salidas MIDI. */
    public static List<MidiDevice.Info> outputs() {
        return devicesThat(device -> device.getMaxReceivers() != 0);
    }

    /** Los que envian notas: teclados, guitarras MIDI. */
    public static List<MidiDevice.Info> inputs() {
        return devicesThat(device -> device.getMaxTransmitters() != 0);
    }

    public static Optional<MidiDevice.Info> named(String name) {
        return all().stream().filter(info -> info.getName().equals(name)).findFirst();
    }

    private static List<MidiDevice.Info> all() {
        return List.of(MidiSystem.getMidiDeviceInfo());
    }

    private static List<MidiDevice.Info> devicesThat(java.util.function.Predicate<MidiDevice> condition) {
        List<MidiDevice.Info> found = new ArrayList<>();
        for (MidiDevice.Info info : all()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (condition.test(device)) {
                    found.add(info);
                }
            } catch (MidiUnavailableException ignored) {
                // Un dispositivo que no se puede abrir simplemente no se ofrece.
            }
        }
        return List.copyOf(found);
    }
}

package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.midi.MidiCapture;
import com.gstncaruso.tabpro.midi.MidiDevices;
import com.gstncaruso.tabpro.midi.MidiPlayer;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;

/**
 * Los dispositivos MIDI de la maquina, tal como los ofrece la ventana de
 * configuracion: por donde sale el sonido y por donde entran las notas.
 */
final class MidiDeviceSetup implements Ports.Devices {

    private final Optional<MidiPlayer> player;
    private String output = "";
    private String input = "";
    private MidiCapture capture;

    MidiDeviceSetup(Optional<MidiPlayer> player) {
        this.player = player;
    }

    @Override
    public List<String> outputs() {
        return names(MidiDevices.outputs());
    }

    @Override
    public String output() {
        return output;
    }

    @Override
    public void useOutput(String name) {
        output = name;
        MidiDevices.named(name).ifPresent(info -> player.ifPresent(midi -> midi.useOutput(info)));
    }

    @Override
    public List<String> inputs() {
        return names(MidiDevices.inputs());
    }

    @Override
    public String input() {
        return input;
    }

    @Override
    public void useInput(String name) {
        input = name;
    }

    @Override
    public boolean isCapturing() {
        return capture != null;
    }

    @Override
    public void startCapture(Ports.CapturedNote listener) {
        stopCapture();
        Optional<MidiDevice.Info> chosen = MidiDevices.named(input)
                .or(() -> MidiDevices.inputs().stream().findFirst());
        if (chosen.isEmpty()) {
            return;
        }
        try {
            capture = new MidiCapture(chosen.get(), asCapturedNotes(listener));
            capture.start();
        } catch (MidiUnavailableException e) {
            capture = null;
            System.err.println("No se pudo abrir la entrada MIDI: " + e.getMessage());
        }
    }

    @Override
    public void stopCapture() {
        if (capture != null) {
            capture.close();
            capture = null;
        }
    }

    private static MidiCapture.CapturedNotes asCapturedNotes(Ports.CapturedNote listener) {
        return new MidiCapture.CapturedNotes() {

            @Override
            public void noteInTheSameChord(int midiNumber, int channel) {
                listener.inTheSameChord(midiNumber, channel);
            }

            @Override
            public void noteInANewBeat(int midiNumber, int channel) {
                listener.inANewBeat(midiNumber, channel);
            }
        };
    }

    private static List<String> names(List<MidiDevice.Info> devices) {
        return devices.stream().map(MidiDevice.Info::getName).toList();
    }
}

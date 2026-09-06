package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.midi.MidiCapture;
import com.gstncaruso.tabpro.midi.MidiDevices;
import com.gstncaruso.tabpro.midi.MidiPlayer;
import com.gstncaruso.tabpro.midi.MidiTestTone;
import com.gstncaruso.tabpro.midi.SoundFontBank;
import com.gstncaruso.tabpro.midi.SoundFonts;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 * Los dispositivos MIDI de la maquina, tal como los ofrece la ventana de
 * configuracion: hasta cuatro puertos de salida (cada uno con su banco
 * SoundFont si usa el sintetizador interno), la entrada de captura y su
 * sensibilidad.
 */
final class MidiDeviceSetup implements Ports.Devices {

    private final Optional<MidiPlayer> player;
    private final SoundFontBank soundBank;
    private final String[] outputByPort = new String[Channel.PORT_COUNT];
    private final boolean[] limitPitchVariationByPort = new boolean[Channel.PORT_COUNT];
    private String input = "";
    private int sensitivityMillis = MidiCapture.DEFAULT_SENSITIVITY_MILLIS;
    private MidiCapture capture;

    MidiDeviceSetup(Optional<MidiPlayer> player, SoundFontBank soundBank) {
        this.player = player;
        this.soundBank = soundBank;
        Arrays.fill(outputByPort, "");
    }

    @Override
    public List<String> outputs() {
        return names(MidiDevices.outputs());
    }

    @Override
    public String output(int port) {
        return outputByPort[port - 1];
    }

    @Override
    public void useOutput(int port, String name) {
        outputByPort[port - 1] = name;
        MidiDevices.named(name).ifPresent(info -> player.ifPresent(midi -> midi.useOutputForPort(port, info)));
    }

    @Override
    public void playTestNote(String deviceName) {
        MidiDevices.named(deviceName).ifPresent(this::playTestNoteOn);
    }

    private void playTestNoteOn(MidiDevice.Info info) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            device.open();
            MidiTestTone.play(device.getReceiver(), 0, MidiTestTone.DEFAULT_DURATION_MILLIS, device::close);
        } catch (MidiUnavailableException e) {
            System.err.println("No se pudo probar la salida MIDI " + info.getName() + ": " + e.getMessage());
        }
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
            capture = new MidiCapture(chosen.get(), asCapturedNotes(listener), sensitivityMillis);
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

    @Override
    public int sensitivityMillis() {
        return sensitivityMillis;
    }

    @Override
    public void useSensitivityMillis(int millis) {
        sensitivityMillis = millis;
    }

    @Override
    public boolean limitsPitchVariation(int port) {
        return limitPitchVariationByPort[port - 1];
    }

    @Override
    public void useLimitPitchVariation(int port, boolean limit) {
        limitPitchVariationByPort[port - 1] = limit;
        player.ifPresent(midi -> midi.useLimitPitchVariation(port, limit));
    }

    @Override
    public Optional<String> soundFontFile() {
        return soundBank.file().map(Path::toString);
    }

    /**
     * El banco es global (ver SoundFontBank): un archivo vacio no apaga el sonido, vuelve a
     * dejar que tabpro busque el que tenga instalado el sistema.
     */
    @Override
    public void chooseSoundFontFile(Optional<String> path) {
        Optional<Path> resolved = path.map(Path::of).or(() -> SoundFonts.installed().stream().findFirst());
        soundBank.choose(resolved);
    }

    @Override
    public boolean soundFontActive() {
        return soundBank.active();
    }

    @Override
    public void toggleSoundFont() {
        soundBank.toggle();
    }

    @Override
    public String soundFontStatus() {
        return soundBank.status();
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

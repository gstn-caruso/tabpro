package com.gstncaruso.tabpro.midi;

import com.sun.media.sound.AudioSynthesizer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public final class SoundFontBank implements AutoCloseable {

    private final Supplier<Synthesizer> synthesizers;
    private Optional<Path> file;
    private boolean active = true;
    private boolean anyPortTried;
    private final Map<Integer, SoundFontSynthesizer> synthesizersByPort = new ConcurrentHashMap<>();

    public SoundFontBank(Optional<Path> file) {
        this(file, SoundFontSynthesizer::systemSynthesizer);
    }

    SoundFontBank(Optional<Path> file, Supplier<Synthesizer> synthesizers) {
        this.file = file;
        this.synthesizers = synthesizers;
    }

    public Receiver receiverForPort(int port) {
        return synthesizerForPort(port).map(SoundFontSynthesizer::receiver).orElseGet(SoundFontBank::silentReceiver);
    }

    public void toggle() {
        if (file.isEmpty()) {
            return;
        }
        active = !active;
        synthesizersByPort.values().forEach(SoundFontSynthesizer::toggle);
    }

    public void choose(Optional<Path> newFile) {
        if (newFile.equals(file)) {
            return;
        }
        file = newFile;
        active = true;
        synthesizersByPort.values().forEach(synth -> synth.choose(newFile));
    }

    public boolean active() {
        if (!active || file.isEmpty()) {
            return false;
        }
        if (!anyPortTried) {
            return true;
        }
        return synthesizersByPort.values().stream().anyMatch(synth -> synth.file().isPresent());
    }

    public Optional<Path> file() {
        return file;
    }

    public String status() {
        if (file.isEmpty()) {
            return "Sin ningún banco de sonido: suena el sintetizador interno del JDK";
        }
        String name = file.get().getFileName().toString();
        if (!active) {
            return "Banco de sonido desactivado (" + name + "): suena el sintetizador interno del JDK";
        }
        if (synthesizersByPort.values().stream().anyMatch(synth -> synth.file().isPresent())) {
            return "Sonando con " + name;
        }
        if (anyPortTried) {
            return "No se pudo cargar " + name + ": suena el sintetizador interno del JDK";
        }
        return "Banco elegido: " + name + " (se aplica al reproducir)";
    }

    /**
     * WAVE rendering is offline, so this never needs to open a real audio line
     * (Synthesizer.open()), which throws MidiUnavailableException on a machine with no audio card
     * even though nothing will play live. The bank loads only once the caller opens this
     * synthesizer for offline rendering (AudioSynthesizer.openStream), which Gervill can do
     * without any audio card at all.
     */
    public Synthesizer freshSynthesizer() throws MidiUnavailableException {
        Synthesizer synth = synthesizers.get();
        if (synth == null) {
            throw new MidiUnavailableException("no synthesizer available to render the audio");
        }
        if (!active || file.isEmpty() || !(synth instanceof AudioSynthesizer audioSynth)) {
            return synth;
        }
        Optional<Soundbank> bank = SoundFonts.read(file.get());
        if (bank.isEmpty()) {
            return synth;
        }
        return loadingOnFirstOpen(audioSynth, bank.get());
    }

    @Override
    public void close() {
        synthesizersByPort.values().forEach(SoundFontSynthesizer::close);
        synthesizersByPort.clear();
    }

    private Optional<SoundFontSynthesizer> synthesizerForPort(int port) {
        SoundFontSynthesizer existing = synthesizersByPort.get(port);
        if (existing != null) {
            return Optional.of(existing);
        }
        anyPortTried = true;
        try {
            SoundFontSynthesizer synth = SoundFontSynthesizer.open(file, synthesizers);
            if (!active) {
                synth.toggle();
            }
            synthesizersByPort.put(port, synth);
            return Optional.of(synth);
        } catch (MidiUnavailableException e) {
            System.err.println(
                    "Port " + port + " is left without an internal synthesizer for the sound bank: "
                            + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Wraps the synthesizer in a proxy that loads the bank right after openStream, and never
     * before: Gervill discards instruments loaded before that call, and opening the synthesizer
     * ourselves ahead of time would break the open that the renderer does afterward.
     */
    private static Synthesizer loadingOnFirstOpen(AudioSynthesizer real, Soundbank bank) {
        InvocationHandler handler = (proxy, method, args) -> {
            Object result = method.invoke(real, args);
            if (method.getName().equals("openStream")) {
                real.unloadAllInstruments(real.getDefaultSoundbank());
                real.loadAllInstruments(bank);
            }
            return result;
        };
        return (Synthesizer) Proxy.newProxyInstance(
                SoundFontBank.class.getClassLoader(), new Class<?>[] {AudioSynthesizer.class}, handler);
    }

    private static Receiver silentReceiver() {
        return new Receiver() {
            @Override
            public void send(javax.sound.midi.MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
    }
}

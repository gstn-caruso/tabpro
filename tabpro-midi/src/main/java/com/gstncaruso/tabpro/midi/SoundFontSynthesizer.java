package com.gstncaruso.tabpro.midi;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public final class SoundFontSynthesizer implements AutoCloseable {

    private final Synthesizer synthesizer;
    private final Soundbank defaultBank;
    private Soundbank currentlyLoaded;
    private Soundbank loadedBank;
    private Optional<Path> file = Optional.empty();
    private boolean active;

    private SoundFontSynthesizer(Synthesizer synthesizer) {
        this.synthesizer = synthesizer;
        this.defaultBank = synthesizer.getDefaultSoundbank();
        this.currentlyLoaded = defaultBank;
    }

    public static SoundFontSynthesizer open(Optional<Path> file) throws MidiUnavailableException {
        return open(file, SoundFontSynthesizer::systemSynthesizer);
    }

    static SoundFontSynthesizer open(Optional<Path> file, Supplier<Synthesizer> synthesizers)
            throws MidiUnavailableException {
        Synthesizer synthesizer = synthesizers.get();
        if (synthesizer == null) {
            throw new MidiUnavailableException("no synthesizer available on this machine");
        }
        synthesizer.open();
        SoundFontSynthesizer result = new SoundFontSynthesizer(synthesizer);
        result.choose(file);
        return result;
    }

    static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            return null;
        }
    }

    public Synthesizer synthesizer() {
        return synthesizer;
    }

    public Receiver receiver() {
        try {
            return synthesizer.getReceiver();
        } catch (MidiUnavailableException e) {
            return silentReceiver();
        }
    }

    public Optional<Path> file() {
        return loadedBank != null ? file : Optional.empty();
    }

    public boolean active() {
        return active;
    }

    public void toggle() {
        if (loadedBank == null) {
            return;
        }
        active = !active;
        switchTo(active ? loadedBank : defaultBank);
    }

    public void choose(Optional<Path> newFile) {
        file = newFile;
        loadedBank = newFile.flatMap(SoundFonts::read).orElse(null);
        active = loadedBank != null;
        switchTo(active ? loadedBank : defaultBank);
    }

    public SoundFontStatus status() {
        if (active) {
            return SoundFontStatus.playing(fileName());
        }
        if (loadedBank != null) {
            return SoundFontStatus.disabled(fileName());
        }
        if (file.isPresent()) {
            return SoundFontStatus.failed(fileName());
        }
        return SoundFontStatus.none();
    }

    @Override
    public void close() {
        synthesizer.close();
    }

    private String fileName() {
        return file.orElseThrow().getFileName().toString();
    }

    private void switchTo(Soundbank bank) {
        if (bank == currentlyLoaded) {
            return;
        }
        synthesizer.unloadAllInstruments(currentlyLoaded);
        synthesizer.loadAllInstruments(bank);
        currentlyLoaded = bank;
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

package com.gstncaruso.tabpro.midi;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * El sintetizador que va a sonar: abierto, y con el banco SoundFont puesto si hay uno
 * disponible. Es el reemplazo libre del RSE (Realistic Sound Engine) que describe el manual: F2
 * lo prende y lo apaga sin reiniciar nada, para comparar los dos sonidos. Lo arma una sola vez la
 * reproduccion en vivo, y lo puede reusar cualquier otro renderizador (como la exportacion a
 * WAVE) para sonar siempre igual.
 */
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

    /**
     * Abre el sintetizador del sistema y trata de dejarle puesto el banco de ese archivo. Si no
     * eligieron ninguno, o el archivo no existe o esta corrupto, sigue sonando con el banco
     * interno del JDK: nunca lanza por un SoundFont invalido.
     */
    public static SoundFontSynthesizer open(Optional<Path> file) throws MidiUnavailableException {
        return open(file, SoundFontSynthesizer::systemSynthesizer);
    }

    /**
     * Con el sintetizador del sistema elegido de afuera (null si no hay ninguno disponible), para
     * poder probar que pasa en una maquina sin placa de sonido sin depender de si esta maquina de
     * pruebas tiene una de verdad -la misma costura que ya usa MidiPlayer.PortOutput.
     */
    static SoundFontSynthesizer open(Optional<Path> file, Supplier<Synthesizer> synthesizers)
            throws MidiUnavailableException {
        Synthesizer synthesizer = synthesizers.get();
        if (synthesizer == null) {
            throw new MidiUnavailableException("no hay ningun sintetizador disponible en esta maquina");
        }
        synthesizer.open();
        SoundFontSynthesizer result = new SoundFontSynthesizer(synthesizer);
        result.choose(file);
        return result;
    }

    /** El sintetizador del sistema, o null si la maquina no tiene ninguno disponible. */
    static Synthesizer systemSynthesizer() {
        try {
            return MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            return null;
        }
    }

    /** El sintetizador listo, para que lo use cualquier reproductor o renderizador. */
    public Synthesizer synthesizer() {
        return synthesizer;
    }

    /** Un receiver de ese sintetizador, o uno mudo si ya no se puede pedir ninguno. */
    public Receiver receiver() {
        try {
            return synthesizer.getReceiver();
        } catch (MidiUnavailableException e) {
            return silentReceiver();
        }
    }

    /** El archivo que quedo cargado, este sonando o no (F2 lo prende y lo apaga sin descargarlo). */
    public Optional<Path> file() {
        return loadedBank != null ? file : Optional.empty();
    }

    /** Si el banco elegido esta sonando en vez del sintetizador interno del JDK. */
    public boolean active() {
        return active;
    }

    /** El F2 del manual: prende o apaga el banco cargado, sin reiniciar nada. Sin banco, no hace nada. */
    public void toggle() {
        if (loadedBank == null) {
            return;
        }
        active = !active;
        switchTo(active ? loadedBank : defaultBank);
    }

    /** El usuario elige otro archivo (o ninguno, para volver al sintetizador interno). */
    public void choose(Optional<Path> newFile) {
        file = newFile;
        loadedBank = newFile.flatMap(SoundFonts::read).orElse(null);
        active = loadedBank != null;
        switchTo(active ? loadedBank : defaultBank);
    }

    /** Como esta el banco de sonido ahora, para mostrarselo al usuario. */
    public String status() {
        if (active) {
            return "Sonando con " + fileName();
        }
        if (loadedBank != null) {
            return "Banco de sonido desactivado (" + fileName() + "): suena el sintetizador interno del JDK";
        }
        if (file.isPresent()) {
            return "No se pudo cargar " + fileName() + ": suena el sintetizador interno del JDK";
        }
        return "Sin ningun banco de sonido: suena el sintetizador interno del JDK";
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

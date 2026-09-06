package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public final class MidiPlayer implements Player, AutoCloseable {

    private static final int END_OF_TRACK_META_TYPE = 47;

    /** El puerto que usa una pista nueva; el unico que conduce el tempo, los beats y el fin de la reproduccion. */
    private static final int PRIMARY_PORT = 1;

    private final Sequencer sequencer;
    private final Supplier<Receiver> synthesizers;
    private volatile PlaybackListener listener;
    private NotePreview preview;
    private javax.sound.midi.MidiDevice chosenOutput;
    private final Set<Integer> limitedPitchVariationPorts = new HashSet<>();
    private final Map<Integer, PortOutput> secondaryPorts = new HashMap<>();

    public MidiPlayer(Sequencer sequencer) {
        this(sequencer, MidiPlayer::defaultSynthesizer);
    }

    MidiPlayer(Sequencer sequencer, Supplier<Receiver> synthesizers) {
        this.sequencer = sequencer;
        this.synthesizers = synthesizers;
        sequencer.addMetaEventListener(this::notifyListenerOf);
    }

    /** Deja el sonido listo; conviene llamarlo de antemano porque abrir el sintetizador tarda. */
    public void open() {
        openSequencer();
        preview();
    }

    @Override
    public void play(Timeline timeline, PlaybackListener listener) {
        play(timeline, java.util.List.of(), listener);
    }

    @Override
    public void play(
            Timeline timeline,
            java.util.List<com.gstncaruso.tabpro.core.playback.MetronomeClick> clicks,
            PlaybackListener listener) {
        stopSecondaryPorts();
        open();
        this.listener = listener;
        Map<Integer, Sequence> byPort = MidiSequences.sequencesByPort(timeline, limitedPitchVariationPorts);
        try {
            Sequence primary = byPort.containsKey(PRIMARY_PORT)
                    ? byPort.get(PRIMARY_PORT)
                    : MidiSequences.fromTimeline(withoutTracks(timeline));
            MidiSequences.addMetronomeTrack(primary, clicks);
            sequencer.setSequence(primary);
            sequencer.setTickPosition(0);
            for (Map.Entry<Integer, Sequence> entry : byPort.entrySet()) {
                if (entry.getKey() != PRIMARY_PORT) {
                    secondaryPortOutput(entry.getKey()).play(entry.getValue());
                }
            }
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
        sequencer.start();
    }

    /** El mismo tempo pero sin pistas, para que el puerto principal siempre tenga algo que dirigir. */
    private static Timeline withoutTracks(Timeline timeline) {
        return new Timeline(timeline.tempo(), timeline.ticksPerQuarter(), java.util.List.of());
    }

    /** La secuencia que esta tocando el puerto principal, la que dirige el tempo y avisa los beats. */
    Sequence sequenceInPlay() {
        return sequencer.getSequence();
    }

    @Override
    public void playNote(Pitch pitch, int program) {
        preview().play(pitch, program);
    }

    @Override
    public void stop() {
        if (sequencer.isRunning()) {
            sequencer.stop();
        }
        stopSecondaryPorts();
    }

    @Override
    public boolean isPlaying() {
        return sequencer.isRunning();
    }

    @Override
    public void close() {
        if (preview != null) {
            preview.close();
        }
        sequencer.close();
        closeChosenOutput();
        secondaryPorts.values().forEach(PortOutput::close);
    }

    /**
     * Manda el sonido a otro dispositivo, como pide la ventana de configuracion
     * MIDI. El secuenciador viene enchufado al sintetizador del sistema, asi que
     * primero hay que desenchufarlo.
     */
    public void useOutput(javax.sound.midi.MidiDevice.Info info) {
        try {
            javax.sound.midi.MidiDevice device = MidiSystem.getMidiDevice(info);
            device.open();
            openSequencer();
            for (javax.sound.midi.Transmitter transmitter : sequencer.getTransmitters()) {
                transmitter.close();
            }
            sequencer.getTransmitter().setReceiver(device.getReceiver());
            replacePreviewWith(device.getReceiver());
            closeChosenOutput();
            chosenOutput = device;
        } catch (MidiUnavailableException e) {
            System.err.println("No se pudo usar la salida MIDI " + info.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Lo mismo que useOutput, pero para uno de los otros tres puertos: cada uno
     * necesita su propio secuenciador porque cada uno es un dispositivo aparte.
     */
    public void useOutputForPort(int port, javax.sound.midi.MidiDevice.Info info) {
        if (port == PRIMARY_PORT) {
            useOutput(info);
            return;
        }
        try {
            secondaryPortOutput(port).useDevice(info);
        } catch (MidiUnavailableException e) {
            System.err.println(
                    "No se pudo usar la salida MIDI " + info.getName() + " en el puerto " + port + ": "
                            + e.getMessage());
        }
    }

    /** Options > MIDI Setup: si un puerto tilda Limit Pitch Variation, ver PitchTrajectory.staysWithin. */
    public void useLimitPitchVariation(int port, boolean limit) {
        if (limit) {
            limitedPitchVariationPorts.add(port);
        } else {
            limitedPitchVariationPorts.remove(port);
        }
    }

    private PortOutput secondaryPortOutput(int port) {
        return secondaryPorts.computeIfAbsent(port, ignored -> new PortOutput());
    }

    private void stopSecondaryPorts() {
        secondaryPorts.values().forEach(PortOutput::stop);
    }

    private void replacePreviewWith(Receiver receiver) {
        if (preview != null) {
            preview.close();
        }
        preview = new NotePreview(receiver);
    }

    private void closeChosenOutput() {
        if (chosenOutput != null) {
            chosenOutput.close();
        }
    }

    private NotePreview preview() {
        if (preview == null) {
            preview = new NotePreview(synthesizers.get());
        }
        return preview;
    }

    private void openSequencer() {
        if (sequencer.isOpen()) {
            return;
        }
        try {
            sequencer.open();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }

    /** El sintetizador del sistema, o uno mudo si la maquina no tiene ninguno. */
    private static Receiver defaultSynthesizer() {
        try {
            return MidiSystem.getReceiver();
        } catch (MidiUnavailableException e) {
            return silentReceiver();
        }
    }

    private static Receiver silentReceiver() {
        return new Receiver() {
            @Override
            public void send(MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
    }

    private void notifyListenerOf(MetaMessage message) {
        if (message.getType() == END_OF_TRACK_META_TYPE) {
            listener.playbackFinished();
            return;
        }
        MidiSequences.beatPositionOf(message).ifPresent(listener::beatStarted);
    }

    /**
     * Un puerto que no es el principal: tiene su propio secuenciador porque
     * cada puerto manda a un dispositivo distinto y no se puede mezclar sus
     * canales en una sola secuencia. No dirige el tempo compartido ni avisa
     * beats ni el fin de la reproduccion -de eso se encarga el puerto principal.
     */
    private static final class PortOutput implements AutoCloseable {

        private final Sequencer sequencer;
        private javax.sound.midi.MidiDevice device;

        PortOutput() {
            this.sequencer = newSequencer();
        }

        void useDevice(javax.sound.midi.MidiDevice.Info info) throws MidiUnavailableException {
            javax.sound.midi.MidiDevice chosen = MidiSystem.getMidiDevice(info);
            chosen.open();
            openIfNeeded();
            for (javax.sound.midi.Transmitter transmitter : sequencer.getTransmitters()) {
                transmitter.close();
            }
            sequencer.getTransmitter().setReceiver(chosen.getReceiver());
            closeDevice();
            device = chosen;
        }

        void play(Sequence sequence) {
            openIfNeeded();
            try {
                sequencer.setSequence(sequence);
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException(e);
            }
            sequencer.setTickPosition(0);
            sequencer.start();
        }

        void stop() {
            if (sequencer.isRunning()) {
                sequencer.stop();
            }
        }

        @Override
        public void close() {
            stop();
            sequencer.close();
            closeDevice();
        }

        private void openIfNeeded() {
            if (sequencer.isOpen()) {
                return;
            }
            try {
                sequencer.open();
            } catch (MidiUnavailableException e) {
                throw new IllegalStateException(e);
            }
        }

        private void closeDevice() {
            if (device != null) {
                device.close();
            }
        }

        /** Conectado al sintetizador del sistema por defecto, igual que el puerto principal. */
        private static Sequencer newSequencer() {
            try {
                return MidiSystem.getSequencer();
            } catch (MidiUnavailableException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}

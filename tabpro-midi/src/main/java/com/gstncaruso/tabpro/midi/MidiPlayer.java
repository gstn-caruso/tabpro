package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.function.Supplier;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;

public final class MidiPlayer implements Player, AutoCloseable {

    private static final int END_OF_TRACK_META_TYPE = 47;

    private final Sequencer sequencer;
    private final Supplier<Receiver> synthesizers;
    private volatile PlaybackListener listener;
    private NotePreview preview;
    private javax.sound.midi.MidiDevice chosenOutput;

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
        open();
        this.listener = listener;
        try {
            javax.sound.midi.Sequence sequence = MidiSequences.fromTimeline(timeline);
            MidiSequences.addMetronomeTrack(sequence, clicks);
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
        sequencer.setTickPosition(0);
        sequencer.start();
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
}

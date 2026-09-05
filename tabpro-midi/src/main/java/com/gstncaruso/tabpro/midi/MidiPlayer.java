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
        open();
        this.listener = listener;
        try {
            sequencer.setSequence(MidiSequences.fromTimeline(timeline));
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

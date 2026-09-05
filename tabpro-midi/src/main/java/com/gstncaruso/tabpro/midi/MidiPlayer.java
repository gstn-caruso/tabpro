package com.gstncaruso.tabpro.midi;

import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public final class MidiPlayer implements Player, AutoCloseable {

    private static final int END_OF_TRACK_META_TYPE = 47;

    private final Sequencer sequencer;
    private volatile PlaybackListener listener;

    public MidiPlayer(Sequencer sequencer) {
        this.sequencer = sequencer;
        sequencer.addMetaEventListener(this::notifyListenerOf);
    }

    public void open() {
        if (sequencer.isOpen()) {
            return;
        }
        try {
            sequencer.open();
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
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
        sequencer.close();
    }

    private void notifyListenerOf(MetaMessage message) {
        if (message.getType() == END_OF_TRACK_META_TYPE) {
            listener.playbackFinished();
            return;
        }
        MidiSequences.beatPositionOf(message).ifPresent(listener::beatStarted);
    }
}

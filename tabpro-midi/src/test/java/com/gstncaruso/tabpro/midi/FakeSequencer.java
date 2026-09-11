package com.gstncaruso.tabpro.midi;

import java.io.InputStream;
import java.util.List;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

final class FakeSequencer implements Sequencer {

    private boolean opened;
    private boolean running;
    private long tickPosition;
    private Sequence sequence;
    private final Transmitter silentTransmitter = new Transmitter() {
        @Override
        public void setReceiver(Receiver receiver) {
        }

        @Override
        public Receiver getReceiver() {
            return null;
        }

        @Override
        public void close() {
        }
    };

    @Override
    public void open() {
        opened = true;
    }

    @Override
    public void close() {
        opened = false;
        running = false;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    @Override
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public void setSequence(InputStream stream) {
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setTickPosition(long tick) {
        tickPosition = tick;
    }

    @Override
    public long getTickPosition() {
        return tickPosition;
    }

    @Override
    public MidiDevice.Info getDeviceInfo() {
        return null;
    }

    @Override
    public long getMicrosecondPosition() {
        return 0;
    }

    @Override
    public void setMicrosecondPosition(long microseconds) {
    }

    @Override
    public long getMicrosecondLength() {
        return 0;
    }

    @Override
    public long getTickLength() {
        return 0;
    }

    @Override
    public int getMaxReceivers() {
        return 0;
    }

    @Override
    public int getMaxTransmitters() {
        return 0;
    }

    @Override
    public Receiver getReceiver() {
        return null;
    }

    @Override
    public List<Receiver> getReceivers() {
        return List.of();
    }

    @Override
    public Transmitter getTransmitter() {
        return silentTransmitter;
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return List.of();
    }

    @Override
    public void startRecording() {
    }

    @Override
    public void stopRecording() {
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void recordEnable(Track track, int channel) {
    }

    @Override
    public void recordDisable(Track track) {
    }

    @Override
    public float getTempoInBPM() {
        return 0;
    }

    @Override
    public void setTempoInBPM(float bpm) {
    }

    @Override
    public float getTempoInMPQ() {
        return 0;
    }

    @Override
    public void setTempoInMPQ(float mpq) {
    }

    @Override
    public void setTempoFactor(float factor) {
    }

    @Override
    public float getTempoFactor() {
        return 1;
    }

    @Override
    public void setMasterSyncMode(SyncMode sync) {
    }

    @Override
    public SyncMode getMasterSyncMode() {
        return SyncMode.INTERNAL_CLOCK;
    }

    @Override
    public SyncMode[] getMasterSyncModes() {
        return new SyncMode[0];
    }

    @Override
    public void setSlaveSyncMode(SyncMode sync) {
    }

    @Override
    public SyncMode getSlaveSyncMode() {
        return SyncMode.NO_SYNC;
    }

    @Override
    public SyncMode[] getSlaveSyncModes() {
        return new SyncMode[0];
    }

    @Override
    public void setTrackMute(int track, boolean mute) {
    }

    @Override
    public boolean getTrackMute(int track) {
        return false;
    }

    @Override
    public void setTrackSolo(int track, boolean solo) {
    }

    @Override
    public boolean getTrackSolo(int track) {
        return false;
    }

    @Override
    public boolean addMetaEventListener(MetaEventListener listener) {
        return true;
    }

    @Override
    public void removeMetaEventListener(MetaEventListener listener) {
    }

    @Override
    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        return controllers;
    }

    @Override
    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        return controllers;
    }

    @Override
    public void setLoopStartPoint(long tick) {
    }

    @Override
    public long getLoopStartPoint() {
        return 0;
    }

    @Override
    public void setLoopEndPoint(long tick) {
    }

    @Override
    public long getLoopEndPoint() {
        return -1;
    }

    @Override
    public void setLoopCount(int count) {
    }

    @Override
    public int getLoopCount() {
        return 0;
    }
}

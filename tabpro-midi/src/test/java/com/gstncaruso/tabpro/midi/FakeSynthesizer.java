package com.gstncaruso.tabpro.midi;

import java.util.List;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;

final class FakeSynthesizer implements Synthesizer {

    private final Soundbank defaultBank = new FakeSoundbank();
    private Soundbank currentBank = defaultBank;
    private boolean opened;
    private boolean closed;

    boolean wasOpened() {
        return opened;
    }

    boolean wasClosed() {
        return closed;
    }

    Soundbank loadedBank() {
        return currentBank;
    }

    int loadedInstrumentCount() {
        return getLoadedInstruments().length;
    }

    @Override
    public void open() {
        opened = true;
    }

    @Override
    public void close() {
        opened = false;
        closed = true;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    @Override
    public Soundbank getDefaultSoundbank() {
        return defaultBank;
    }

    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
        currentBank = soundbank;
        return true;
    }

    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
    }

    @Override
    public Instrument[] getLoadedInstruments() {
        return currentBank.getInstruments();
    }

    @Override
    public Receiver getReceiver() {
        return new Receiver() {
            @Override
            public void send(javax.sound.midi.MidiMessage message, long timeStamp) {
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public MidiDevice.Info getDeviceInfo() {
        return null;
    }

    @Override
    public long getMicrosecondPosition() {
        return -1;
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
    public List<Receiver> getReceivers() {
        return List.of();
    }

    @Override
    public Transmitter getTransmitter() {
        return null;
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return List.of();
    }

    @Override
    public int getMaxPolyphony() {
        return 0;
    }

    @Override
    public long getLatency() {
        return 0;
    }

    @Override
    public MidiChannel[] getChannels() {
        return new MidiChannel[0];
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
        return new VoiceStatus[0];
    }

    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
        return true;
    }

    @Override
    public boolean loadInstrument(Instrument instrument) {
        return false;
    }

    @Override
    public void unloadInstrument(Instrument instrument) {
    }

    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
        return false;
    }

    @Override
    public Instrument[] getAvailableInstruments() {
        return new Instrument[0];
    }

    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patches) {
        return false;
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patches) {
    }

    private static final class FakeSoundbank implements Soundbank {

        @Override
        public String getName() {
            return "banco de prueba";
        }

        @Override
        public String getVersion() {
            return "";
        }

        @Override
        public String getVendor() {
            return "";
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public SoundbankResource[] getResources() {
            return new SoundbankResource[0];
        }

        @Override
        public Instrument[] getInstruments() {
            return new Instrument[0];
        }

        @Override
        public Instrument getInstrument(Patch patch) {
            return null;
        }
    }
}

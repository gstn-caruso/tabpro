package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

/** Lee un {@link Sequence} de la biblioteca estandar y lo pasa a la forma que entiende el importador. */
final class MidiFileParser {

    private static final int TEMPO_META = 0x51;
    private static final int TIME_SIGNATURE_META = 0x58;
    private static final int KEY_SIGNATURE_META = 0x59;
    private static final int TRACK_NAME_META = 0x03;
    private static final int MIDI_PORT_META = 0x21;

    private static final int VOLUME_CC = 7;
    private static final int PAN_CC = 10;
    private static final int REVERB_CC = 91;
    private static final int TREMOLO_CC = 92;
    private static final int CHORUS_CC = 93;
    private static final int PHASER_CC = 95;

    private static final int DEFAULT_TEMPO_BPM = 120;

    private MidiFileParser() {
    }

    static ParsedMidiFile parse(Sequence sequence) {
        if (sequence.getDivisionType() != Sequence.PPQ) {
            throw new ScoreFileException("no se soportan archivos MIDI con codigo de tiempo SMPTE");
        }
        double ticksRatio = Duration.TICKS_PER_QUARTER / (double) sequence.getResolution();

        TreeMap<Long, TimeSignature> signatureChanges = new TreeMap<>();
        TreeMap<Long, KeySignature> keyChanges = new TreeMap<>();
        Accumulator accumulator = new Accumulator();
        List<RawMidiTrack> rawTracks = new ArrayList<>();

        javax.sound.midi.Track[] tracks = sequence.getTracks();
        for (int index = 0; index < tracks.length; index++) {
            RawTrackBuilder builder = new RawTrackBuilder(index);
            javax.sound.midi.Track track = tracks[index];
            long lastTick = 0;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                long ourTick = Math.round(event.getTick() * ticksRatio);
                lastTick = ourTick;
                MidiMessage message = event.getMessage();
                if (message instanceof MetaMessage meta) {
                    handleMeta(meta, ourTick, accumulator, signatureChanges, keyChanges, builder);
                } else if (message instanceof ShortMessage shortMessage) {
                    handleShortMessage(shortMessage, ourTick, builder);
                }
            }
            if (builder.hasContent()) {
                rawTracks.add(builder.build(lastTick));
            }
        }
        signatureChanges.putIfAbsent(0L, TimeSignature.fourFour());
        keyChanges.putIfAbsent(0L, KeySignature.cMajor());
        long totalTicks = Math.round(sequence.getTickLength() * ticksRatio);
        MeasureGrid grid = MeasureGrid.build(signatureChanges, keyChanges, totalTicks);
        return new ParsedMidiFile(accumulator.tempoBpm, Optional.ofNullable(accumulator.title), grid, rawTracks);
    }

    private static void handleMeta(
            MetaMessage meta,
            long tick,
            Accumulator accumulator,
            TreeMap<Long, TimeSignature> signatureChanges,
            TreeMap<Long, KeySignature> keyChanges,
            RawTrackBuilder builder) {
        byte[] data = meta.getData();
        switch (meta.getType()) {
            case TEMPO_META -> accumulator.tempoBpm = bpmOf(data);
            case TIME_SIGNATURE_META -> signatureChanges.put(tick, timeSignatureOf(data));
            case KEY_SIGNATURE_META -> keyChanges.put(tick, keySignatureOf(data));
            case TRACK_NAME_META -> {
                String text = new String(data, StandardCharsets.UTF_8);
                builder.name(text);
                if (accumulator.title == null && !text.isBlank()) {
                    accumulator.title = text;
                }
            }
            case MIDI_PORT_META -> builder.port((data[0] & 0xFF) + 1);
            default -> {
            }
        }
    }

    private static void handleShortMessage(ShortMessage message, long tick, RawTrackBuilder builder) {
        switch (message.getCommand()) {
            case ShortMessage.NOTE_ON -> {
                if (message.getData2() > 0) {
                    builder.noteOn(message.getChannel(), tick, message.getData1());
                } else {
                    builder.noteOff(message.getChannel(), tick, message.getData1());
                }
            }
            case ShortMessage.NOTE_OFF -> builder.noteOff(message.getChannel(), tick, message.getData1());
            case ShortMessage.PROGRAM_CHANGE -> builder.programChange(message.getChannel(), message.getData1());
            case ShortMessage.CONTROL_CHANGE -> builder.controlChange(message.getChannel(), message.getData1(), message.getData2());
            default -> {
            }
        }
    }

    private static int bpmOf(byte[] data) {
        int microsecondsPerQuarter = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
        return microsecondsPerQuarter <= 0 ? DEFAULT_TEMPO_BPM : 60_000_000 / microsecondsPerQuarter;
    }

    private static TimeSignature timeSignatureOf(byte[] data) {
        int beats = Math.max(1, data[0] & 0xFF);
        int denominatorPower = Math.min(6, Math.max(0, data[1] & 0xFF));
        return new TimeSignature(beats, 1 << denominatorPower);
    }

    private static KeySignature keySignatureOf(byte[] data) {
        int accidentals = Math.clamp(data[0], -7, 7);
        Mode mode = data.length > 1 && data[1] == 1 ? Mode.MINOR : Mode.MAJOR;
        return new KeySignature(accidentals, mode);
    }

    /** Lo que se junta durante la lectura y no pertenece a ninguna pista en particular. */
    private static final class Accumulator {
        private int tempoBpm = DEFAULT_TEMPO_BPM;
        private String title;
    }

    /**
     * Arma una {@link RawMidiTrack} evento a evento. Empareja cada nota que se prende con la
     * primera que se apaga de la misma altura (FIFO), para saber cuanto sono de verdad y no solo
     * cuando empezo la que sigue; una nota que nunca se apaga se cierra al final de la pista.
     */
    private static final class RawTrackBuilder {
        private final int index;
        private String name;
        private int program;
        private Integer channelNumber;
        private int port = 1;
        private int volume = Channel.DEFAULT_VOLUME;
        private int pan = Channel.CENTER_PAN;
        private int reverb;
        private int tremolo;
        private int chorus;
        private int phaser;
        private final Map<Integer, Deque<Long>> pendingOnsets = new HashMap<>();
        private final TreeMap<Long, List<RawNote>> notesByTick = new TreeMap<>();

        RawTrackBuilder(int index) {
            this.index = index;
        }

        void noteOn(int channel, long tick, int number) {
            rememberChannel(channel);
            pendingOnsets.computeIfAbsent(number, key -> new ArrayDeque<>()).addLast(tick);
        }

        void noteOff(int channel, long tick, int number) {
            rememberChannel(channel);
            Deque<Long> onsets = pendingOnsets.get(number);
            if (onsets == null || onsets.isEmpty()) {
                return;
            }
            long onsetTick = onsets.pollFirst();
            addNote(onsetTick, number, tick - onsetTick);
        }

        void programChange(int channel, int program) {
            rememberChannel(channel);
            this.program = program;
        }

        void controlChange(int channel, int controller, int value) {
            rememberChannel(channel);
            switch (controller) {
                case VOLUME_CC -> volume = value;
                case PAN_CC -> pan = value;
                case REVERB_CC -> reverb = value;
                case TREMOLO_CC -> tremolo = value;
                case CHORUS_CC -> chorus = value;
                case PHASER_CC -> phaser = value;
                default -> {
                }
            }
        }

        void name(String name) {
            this.name = name;
        }

        void port(int port) {
            this.port = port;
        }

        /**
         * Si la pista trajo algun mensaje de canal (nota, programa o control change): eso es lo
         * que distingue una pista de instrumento de la pista de tempo/armadura, que solo trae
         * meta eventos aunque tambien tenga nombre.
         */
        boolean hasContent() {
            return channelNumber != null;
        }

        private void addNote(long onsetTick, int number, long durationTicks) {
            notesByTick.computeIfAbsent(onsetTick, key -> new ArrayList<>()).add(new RawNote(number, Math.max(1, durationTicks)));
        }

        private void rememberChannel(int channel) {
            if (channelNumber == null) {
                channelNumber = channel + 1;
            }
        }

        RawMidiTrack build(long trackEndTick) {
            for (Map.Entry<Integer, Deque<Long>> entry : pendingOnsets.entrySet()) {
                for (Long onsetTick : entry.getValue()) {
                    addNote(onsetTick, entry.getKey(), trackEndTick - onsetTick);
                }
            }
            int channel = channelNumber == null ? 1 : channelNumber;
            boolean percussion = channel == Channel.PERCUSSION_CHANNEL;
            String finalName = name == null || name.isBlank() ? "Pista " + (index + 1) : name;
            return new RawMidiTrack(
                    index, finalName, program, channel, port, volume, pan, reverb, tremolo, chorus, phaser, percussion, notesByTick);
        }
    }
}

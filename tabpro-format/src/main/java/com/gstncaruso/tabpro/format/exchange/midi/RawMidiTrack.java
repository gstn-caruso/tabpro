package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

record RawMidiTrack(
        int index,
        String name,
        int program,
        int channelNumber,
        int port,
        int volume,
        int pan,
        int reverb,
        int tremolo,
        int chorus,
        int phaser,
        boolean percussion,
        SortedMap<Long, List<RawNote>> notesByTick) {

    int noteCount() {
        return notesByTick.values().stream().mapToInt(List::size).sum();
    }

    RawMidiTrack withPositionsQuantizedTo(Optional<NoteValue> grid) {
        if (grid.isEmpty()) {
            return this;
        }
        long gridTicks = Duration.of(grid.get()).ticks();
        TreeMap<Long, List<RawNote>> quantized = new TreeMap<>();
        notesByTick.forEach((tick, notes) -> quantized
                .computeIfAbsent(nearestMultipleOf(tick, gridTicks), key -> new ArrayList<>())
                .addAll(notes));
        return new RawMidiTrack(
                index, name, program, channelNumber, port, volume, pan, reverb, tremolo, chorus, phaser, percussion,
                quantized);
    }

    private static long nearestMultipleOf(long ticks, long unit) {
        return Math.round(ticks / (double) unit) * unit;
    }
}

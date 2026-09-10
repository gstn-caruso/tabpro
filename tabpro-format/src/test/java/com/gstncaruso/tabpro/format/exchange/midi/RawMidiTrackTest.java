package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class RawMidiTrackTest {

    @Test
    void withNoGridLeavesThePositionsUntouched() {
        RawMidiTrack raw = trackWith(notesByTick(720L, new RawNote(64, 100)));

        RawMidiTrack quantized = raw.withPositionsQuantizedTo(Optional.empty());

        assertSame(raw, quantized);
    }

    private static RawMidiTrack trackWith(TreeMap<Long, List<RawNote>> notesByTick) {
        return new RawMidiTrack(0, "Guitarra", 25, 1, 1, 100, 64, 0, 0, 0, 0, false, notesByTick);
    }

    private static TreeMap<Long, List<RawNote>> notesByTick(long tick, RawNote... notes) {
        TreeMap<Long, List<RawNote>> map = new TreeMap<>();
        map.put(tick, List.of(notes));
        return map;
    }
}

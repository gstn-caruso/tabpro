package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.ArrayList;
import java.util.List;

public record Metronome(boolean enabled, int volume) {

    /** General MIDI percussion key 76, Hi Wood Block. */
    public static final int ACCENTED_SOUND = 76;

    /** General MIDI percussion key 77, Low Wood Block. */
    public static final int BEAT_SOUND = 77;

    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 127;
    private static final int DEFAULT_VOLUME = MetronomeClick.DEFAULT_VELOCITY;

    private static final Metronome OFF = new Metronome(false, DEFAULT_VOLUME);
    private static final Metronome ON = new Metronome(true, DEFAULT_VOLUME);

    public Metronome {
        if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
            throw new IllegalArgumentException("volume must be between " + MIN_VOLUME + " and " + MAX_VOLUME + ": " + volume);
        }
    }

    public static Metronome off() {
        return OFF;
    }

    public static Metronome on() {
        return ON;
    }

    public Metronome withVolume(int volume) {
        return new Metronome(enabled, volume);
    }

    public Metronome withEnabled(boolean enabled) {
        return new Metronome(enabled, volume);
    }

    public List<MetronomeClick> clicksFor(Score score) {
        return clicksFor(score, PlayOrder.of(score));
    }

    public List<MetronomeClick> clicksFor(Score score, PlayOrder order) {
        if (!enabled) {
            return List.of();
        }
        List<MetronomeClick> clicks = new ArrayList<>();
        long tick = 0;
        for (int step = 0; step < order.size(); step++) {
            TimeSignature timeSignature = score.timeSignatureOf(order.measureAt(step));
            long beatTicks = timeSignature.ticksPerMeasure() / timeSignature.beats();
            for (int beat = 0; beat < timeSignature.beats(); beat++) {
                clicks.add(new MetronomeClick(tick + beat * beatTicks, beat == 0, volume));
            }
            tick += timeSignature.ticksPerMeasure();
        }
        return clicks;
    }
}

package com.gstncaruso.tabpro.core.model;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The General MIDI percussion sounds. On a percussion track, the tablature
 * numbers are these sounds, not frets.
 */
public final class PercussionKit {

    /** The range every General MIDI sound bank guarantees. */
    public static final int LOWEST_SOUND = 35;
    public static final int HIGHEST_SOUND = 81;

    public static final int LINE_COUNT = 6;

    private static final List<Integer> SOUNDS = IntStream.rangeClosed(LOWEST_SOUND, HIGHEST_SOUND).boxed().toList();

    private PercussionKit() {
    }

    public static Tuning tuning() {
        return Tuning.fromLibrary("percussion", 0, 0, 0, 0, 0, 0);
    }

    public static boolean isPlayable(int sound) {
        return sound >= LOWEST_SOUND && sound <= HIGHEST_SOUND;
    }

    public static List<Integer> sounds() {
        return SOUNDS;
    }
}

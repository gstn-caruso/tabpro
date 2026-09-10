package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.stream.IntStream;

public record PlaybackRange(int fromMeasure, int toMeasure) {

    public PlaybackRange {
        if (fromMeasure < 0) {
            throw new IllegalArgumentException("fromMeasure must be >= 0: " + fromMeasure);
        }
        if (toMeasure < fromMeasure) {
            throw new IllegalArgumentException("toMeasure cannot come before fromMeasure");
        }
    }

    public static PlaybackRange from(int fromMeasure, Score score) {
        return new PlaybackRange(fromMeasure, Math.max(fromMeasure, score.measureCount() - 1));
    }

    public static PlaybackRange whole(Score score) {
        return new PlaybackRange(0, Math.max(0, score.measureCount() - 1));
    }

    public PlayOrder asPlayOrder(Score score) {
        int last = Math.min(toMeasure, score.measureCount() - 1);
        if (last < fromMeasure) {
            return new PlayOrder(java.util.List.of());
        }
        return new PlayOrder(IntStream.rangeClosed(fromMeasure, last).boxed().toList());
    }
}

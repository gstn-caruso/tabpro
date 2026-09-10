package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Lyrics(int trackIndex, List<LyricLine> lines) {

    private static final Lyrics NONE = new Lyrics(0, Collections.nCopies(LyricLine.MAX_LINES, LyricLine.empty()));

    public Lyrics {
        if (lines.size() != LyricLine.MAX_LINES) {
            throw new IllegalArgumentException("the lyrics have " + LyricLine.MAX_LINES + " lines");
        }
        lines = List.copyOf(lines);
    }

    public static Lyrics none() {
        return NONE;
    }

    public boolean isEmpty() {
        return lines.stream().allMatch(LyricLine::isEmpty);
    }

    public LyricLine line(int index) {
        return lines.get(index);
    }

    public Lyrics withLine(int index, LyricLine line) {
        List<LyricLine> updated = new ArrayList<>(lines);
        updated.set(index, line);
        return new Lyrics(trackIndex, updated);
    }

    public Lyrics onTrack(int trackIndex) {
        return new Lyrics(trackIndex, lines);
    }
}

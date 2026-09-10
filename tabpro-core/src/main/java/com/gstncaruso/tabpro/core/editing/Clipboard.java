package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import java.util.List;

public final class Clipboard {

    private final ClipboardStorage storage;

    public Clipboard() {
        this(ClipboardStorage.inMemory());
    }

    public Clipboard(ClipboardStorage storage) {
        this.storage = storage;
    }

    public void hold(Clipping clipping) {
        storage.hold(clipping);
    }

    public Clipping content() {
        return storage.content();
    }

    public boolean isEmpty() {
        return content().isEmpty();
    }

    public void clear() {
        storage.hold(Clipping.EMPTY);
    }

    public record Clipping(List<List<Measure>> measuresByTrack, List<Beat> beats, int stringCount) {

        public static final Clipping EMPTY = new Clipping(List.of(), List.of(), 0);

        public Clipping {
            measuresByTrack = measuresByTrack.stream().map(List::copyOf).toList();
            beats = List.copyOf(beats);
        }

        public static Clipping ofMeasures(List<List<Measure>> measuresByTrack, int stringCount) {
            return new Clipping(measuresByTrack, List.of(), stringCount);
        }

        public static Clipping ofBeats(List<Beat> beats, int stringCount) {
            return new Clipping(List.of(), beats, stringCount);
        }

        public boolean isEmpty() {
            return measuresByTrack.isEmpty() && beats.isEmpty();
        }

        public boolean holdsBeats() {
            return !beats.isEmpty();
        }

        public boolean spansEveryTrack() {
            return measuresByTrack.size() > 1;
        }

        public int measureCount() {
            return measuresByTrack.isEmpty() ? 0 : measuresByTrack.getFirst().size();
        }

        public boolean fitsATrackOf(int strings) {
            return stringCount == strings;
        }
    }
}

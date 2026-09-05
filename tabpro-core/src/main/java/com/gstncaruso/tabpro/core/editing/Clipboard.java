package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import java.util.List;

/**
 * Lo ultimo que se corto o se copio. Dentro de un compas se copian beats; en
 * cualquier otro caso, compases enteros, como explica el manual.
 */
public final class Clipboard {

    private Clipping clipping = Clipping.EMPTY;

    public void hold(Clipping clipping) {
        this.clipping = clipping;
    }

    public Clipping content() {
        return clipping;
    }

    public boolean isEmpty() {
        return clipping.isEmpty();
    }

    public void clear() {
        clipping = Clipping.EMPTY;
    }

    /** Lo copiado, con la cantidad de cuerdas de donde salio para saber donde entra. */
    public record Clipping(List<List<Measure>> measuresByTrack, List<Beat> beats, int stringCount) {

        static final Clipping EMPTY = new Clipping(List.of(), List.of(), 0);

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

        /** Pegar en otra pista solo tiene sentido si tiene la misma cantidad de cuerdas. */
        public boolean fitsATrackOf(int strings) {
            return stringCount == strings;
        }
    }
}

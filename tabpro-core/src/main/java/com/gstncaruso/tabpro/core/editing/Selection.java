package com.gstncaruso.tabpro.core.editing;

/**
 * El rango que abarca la seleccion multiple. Si abarca compases enteros, las
 * acciones valen para las dos voces; si no, solo para la que se esta editando.
 */
public record Selection(int track, int fromMeasure, int fromBeat, int toMeasure, int toBeat, boolean wholeMeasures) {

    public static Selection of(Cursor anchor, Cursor head, boolean wholeMeasures) {
        boolean forwards = anchor.measure() < head.measure()
                || (anchor.measure() == head.measure() && anchor.beat() <= head.beat());
        Cursor first = forwards ? anchor : head;
        Cursor last = forwards ? head : anchor;
        return new Selection(
                anchor.track(), first.measure(), first.beat(), last.measure(), last.beat(), wholeMeasures);
    }

    public static Selection ofMeasures(int track, int fromMeasure, int toMeasure) {
        return new Selection(track, Math.min(fromMeasure, toMeasure), 0, Math.max(fromMeasure, toMeasure), Integer.MAX_VALUE, true);
    }

    public int measureCount() {
        return toMeasure - fromMeasure + 1;
    }

    public boolean spansOneMeasure() {
        return fromMeasure == toMeasure;
    }

    public boolean covers(int measure, int beat) {
        if (measure < fromMeasure || measure > toMeasure) {
            return false;
        }
        if (wholeMeasures) {
            return true;
        }
        boolean afterTheStart = measure > fromMeasure || beat >= fromBeat;
        boolean beforeTheEnd = measure < toMeasure || beat <= toBeat;
        return afterTheStart && beforeTheEnd;
    }

    public boolean coversMeasure(int measure) {
        return measure >= fromMeasure && measure <= toMeasure;
    }
}

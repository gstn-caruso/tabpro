package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Los compases de un archivo MIDI, reconstruidos a partir de sus cambios de medida y de
 * armadura. Cada pista del archivo reparte sus notas sobre esta misma grilla, tal como en un
 * archivo MIDI de verdad el compas lo marca la pista de tempo, no cada pista por separado.
 */
final class MeasureGrid {

    private final List<Long> starts;
    private final List<TimeSignature> signatures;
    private final List<KeySignature> keys;

    private MeasureGrid(List<Long> starts, List<TimeSignature> signatures, List<KeySignature> keys) {
        this.starts = starts;
        this.signatures = signatures;
        this.keys = keys;
    }

    /**
     * Arma la grilla hasta cubrir totalTicks, con al menos un compas. signatureChanges y
     * keyChanges tienen que traer una entrada en el tic 0.
     */
    static MeasureGrid build(TreeMap<Long, TimeSignature> signatureChanges, TreeMap<Long, KeySignature> keyChanges, long totalTicks) {
        List<Long> starts = new ArrayList<>();
        List<TimeSignature> signatures = new ArrayList<>();
        List<KeySignature> keys = new ArrayList<>();
        long tick = 0;
        do {
            TimeSignature signature = signatureChanges.floorEntry(tick).getValue();
            starts.add(tick);
            signatures.add(signature);
            keys.add(keyChanges.floorEntry(tick).getValue());
            tick += signature.ticksPerMeasure();
        } while (tick < totalTicks);
        starts.add(tick);
        return new MeasureGrid(starts, signatures, keys);
    }

    int measureCount() {
        return signatures.size();
    }

    long startTick(int measureIndex) {
        return starts.get(measureIndex);
    }

    long endTick(int measureIndex) {
        return starts.get(measureIndex + 1);
    }

    TimeSignature timeSignatureOf(int measureIndex) {
        return signatures.get(measureIndex);
    }

    KeySignature keySignatureOf(int measureIndex) {
        return keys.get(measureIndex);
    }
}

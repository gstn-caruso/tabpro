package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A que velocidad suena cada tramo de una reproduccion. Una partitura sin
 * cambios de tempo es un unico tramo; en cuanto hay uno, el tiempo real de un
 * tick deja de ser una multiplicacion y pasa a ser la suma de lo que dura cada
 * tramo que ese tick atraviesa.
 */
public record TempoMap(List<TempoChange> changes) {

    public TempoMap {
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("un mapa de tempo tiene que decir a que velocidad arranca");
        }
        if (changes.getFirst().tick() != 0) {
            throw new IllegalArgumentException("el primer tramo arranca en el tick 0");
        }
        changes = collapsed(changes);
    }

    public static TempoMap steady(int bpm) {
        return new TempoMap(List.of(new TempoChange(0, bpm)));
    }

    /** El tempo con el que arranca la reproduccion. */
    public int initialBpm() {
        return changes.getFirst().bpm();
    }

    public boolean isSteady() {
        return changes.size() == 1;
    }

    /** El tempo que esta sonando en ese tick: el del ultimo tramo que ya empezo. */
    public int bpmAt(long tick) {
        int bpm = initialBpm();
        for (TempoChange change : changes) {
            if (change.tick() > tick) {
                return bpm;
            }
            bpm = change.bpm();
        }
        return bpm;
    }

    /** Cuanto tarda en llegar la musica hasta ese tick, acumulando lo que dura cada tramo. */
    public double secondsAt(long tick) {
        double seconds = 0;
        long from = 0;
        for (TempoChange change : changes) {
            if (change.tick() >= tick) {
                break;
            }
            seconds += secondsOf(change.tick() - from, bpmAt(from));
            from = change.tick();
        }
        return seconds + secondsOf(tick - from, bpmAt(from));
    }

    /** El mismo mapa con un tramo nuevo desde ese tick. */
    public TempoMap changingTo(long tick, int bpm) {
        List<TempoChange> updated = new ArrayList<>(changes);
        updated.removeIf(change -> change.tick() >= tick);
        updated.add(new TempoChange(tick, bpm));
        return new TempoMap(updated);
    }

    /** Toda la partitura mas rapida o mas lenta, respetando la proporcion entre sus tramos. */
    public TempoMap scaledBy(double factor) {
        return new TempoMap(changes.stream().map(change -> change.scaledBy(factor)).toList());
    }

    /** Lo mismo, pero dicho por el tempo con el que se quiere arrancar. */
    public TempoMap startingAt(int bpm) {
        return scaledBy((double) bpm / initialBpm());
    }

    /** La misma musica mas tarde: el tempo de arranque cubre lo que se le antepuso. */
    public TempoMap shiftedBy(long ticks) {
        if (ticks == 0) {
            return this;
        }
        List<TempoChange> moved = new ArrayList<>();
        moved.add(new TempoChange(0, initialBpm()));
        for (TempoChange change : changes.subList(1, changes.size())) {
            moved.add(change.shiftedBy(ticks));
        }
        return new TempoMap(moved);
    }

    private static double secondsOf(long ticks, int bpm) {
        return (double) ticks / Duration.TICKS_PER_QUARTER * 60.0 / bpm;
    }

    /** Un tramo por cambio de verdad: sin repetir el tempo que ya sonaba ni pisarse en el mismo tick. */
    private static List<TempoChange> collapsed(List<TempoChange> changes) {
        List<TempoChange> kept = new ArrayList<>();
        for (TempoChange change : changes) {
            if (!kept.isEmpty() && change.tick() < kept.getLast().tick()) {
                throw new IllegalArgumentException("los tramos van en el orden en que suenan");
            }
            if (!kept.isEmpty() && change.tick() == kept.getLast().tick()) {
                kept.removeLast();
            }
            if (kept.isEmpty() || kept.getLast().bpm() != change.bpm()) {
                kept.add(change);
            }
        }
        return List.copyOf(kept);
    }
}

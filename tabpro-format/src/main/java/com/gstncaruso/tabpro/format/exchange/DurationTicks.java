package com.gstncaruso.tabpro.format.exchange;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Convierte cantidades de tics sueltas (las que trae un MIDI, una tablatura ASCII con espaciado
 * libre, o un grupo irregular de MusicXML) a las figuras simples que el modelo sabe representar.
 * El modelo no tiene grupos irregulares para figuras importadas de otro formato: esta clase
 * redondea a la figura simple (con o sin puntillo) mas parecida.
 */
public final class DurationTicks {

    /** La figura mas chica que el modelo distingue: la fusa. Toda grilla de importacion usa esta unidad. */
    public static final long GRID_TICKS = Duration.of(NoteValue.SIXTY_FOURTH).ticks();

    private static final List<Duration> SIMPLE_DURATIONS = simpleDurations();

    private DurationTicks() {
    }

    /** La figura simple cuya duracion en tics mas se acerca a la pedida. */
    public static Duration nearestTo(long ticks) {
        return SIMPLE_DURATIONS.stream()
                .min(Comparator.comparingLong(duration -> Math.abs(duration.ticks() - ticks)))
                .orElseThrow();
    }

    /**
     * Lo mismo, pero exigiendo que la figura elegida sea representable exactamente con esta
     * grilla: la precision que el import de MIDI deja elegir para la posicion y la duracion de
     * las notas de una interpretacion humana. Una corchea con puntillo, por ejemplo, necesita una
     * grilla de semicorchea o mas fina -- pedir una grilla de corchea la descarta.
     */
    public static Duration nearestTo(long ticks, NoteValue finestGrid) {
        long gridTicks = Duration.of(finestGrid).ticks();
        return SIMPLE_DURATIONS.stream()
                .filter(duration -> duration.ticks() % gridTicks == 0)
                .min(Comparator.comparingLong(duration -> Math.abs(duration.ticks() - ticks)))
                .orElseThrow();
    }

    /**
     * Parte una cantidad de tics en una secuencia de figuras simples que suman exactamente eso,
     * de mas larga a mas corta. Si la cantidad ya es una figura simple (con o sin puntillo), el
     * resultado es esa unica figura.
     */
    public static List<Duration> decompose(long ticks) {
        long rounded = Math.round(ticks / (double) GRID_TICKS) * GRID_TICKS;
        if (rounded <= 0) {
            return List.of();
        }
        Optional<Duration> exact = exactMatch(rounded);
        if (exact.isPresent()) {
            return List.of(exact.get());
        }
        return greedyBinaryDecomposition(rounded);
    }

    private static Optional<Duration> exactMatch(long ticks) {
        return SIMPLE_DURATIONS.stream().filter(duration -> duration.ticks() == ticks).findFirst();
    }

    private static List<Duration> greedyBinaryDecomposition(long ticks) {
        List<Duration> result = new ArrayList<>();
        long remaining = ticks;
        for (NoteValue value : NoteValue.values()) {
            long unit = Duration.of(value).ticks();
            while (remaining >= unit) {
                result.add(Duration.of(value));
                remaining -= unit;
            }
        }
        return result;
    }

    private static List<Duration> simpleDurations() {
        List<Duration> durations = new ArrayList<>();
        for (NoteValue value : NoteValue.values()) {
            durations.add(new Duration(value, false));
            durations.add(new Duration(value, true));
        }
        return List.copyOf(durations);
    }
}

package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * El orden real en que hay que tocar los compases de una partitura: de
 * corrido, salvo que haya repeticiones, finales alternativos o direcciones
 * musicales (Da Capo, Segno, Coda, Fine) que digan lo contrario.
 */
public record PlayOrder(List<Integer> measureIndexes) {

    /**
     * Un limite de seguridad: ninguna partitura real necesita mas pasos que
     * esto. Si una partitura mal armada intenta pedir mas, se corta aca en
     * vez de colgarse en un bucle infinito.
     */
    public static final int MAX_STEPS = 20_000;

    public PlayOrder {
        measureIndexes = List.copyOf(measureIndexes);
    }

    public static PlayOrder of(Score score) {
        return new PlayOrder(sequenceOf(score));
    }

    /** Un orden que no toca nada. */
    public static PlayOrder nothing() {
        return new PlayOrder(List.of());
    }

    public int size() {
        return measureIndexes.size();
    }

    public boolean isEmpty() {
        return measureIndexes.isEmpty();
    }

    /**
     * Lo que suena antes de que le llegue el turno a ese compas. Es como se
     * recuperan los cambios de parametro anteriores cuando la reproduccion
     * arranca en el medio: lo anterior es lo anterior en el orden en que suena
     * la partitura, no en el que esta escrita.
     */
    public PlayOrder before(int measure) {
        for (int step = 0; step < size(); step++) {
            if (measureAt(step) == measure) {
                return new PlayOrder(measureIndexes.subList(0, step));
            }
        }
        return nothing();
    }

    public int measureAt(int step) {
        return measureIndexes.get(step);
    }

    private static List<Integer> sequenceOf(Score score) {
        int measureCount = score.measureCount();
        if (measureCount == 0) {
            return List.of();
        }

        List<Integer> played = new ArrayList<>();
        Map<Integer, Integer> passesByOpen = new HashMap<>();
        Map<Integer, Integer> timesClosedAt = new HashMap<>();
        Set<Integer> jumpsAlreadyTaken = new HashSet<>();

        int openIndex = 0;
        Optional<DirectionSymbol> stopAt = Optional.empty();
        int index = 0;

        while (index >= 0 && index < measureCount && played.size() < MAX_STEPS) {
            MeasureAttributes attributes = score.attributesOf(index);

            if (attributes.repeatOpen()) {
                openIndex = index;
            }

            int pass = passesByOpen.getOrDefault(openIndex, 1);
            if (!attributes.playedOnPass(pass)) {
                index++;
                continue;
            }

            played.add(index);

            if (stopAt.isPresent() && attributes.symbol().equals(stopAt)) {
                break;
            }

            OptionalInt jumpDestination = destinationOf(score, attributes, index, jumpsAlreadyTaken);
            if (jumpDestination.isPresent()) {
                stopAt = attributes.jump().flatMap(DirectionJump::stopsAt);
                index = jumpDestination.getAsInt();
                continue;
            }

            if (attributes.repeatCloses()) {
                int timesClosed = timesClosedAt.merge(index, 1, Integer::sum);
                if (timesClosed < attributes.repeatCount()) {
                    passesByOpen.put(openIndex, pass + 1);
                    index = openIndex;
                    continue;
                }
            }

            index++;
        }

        return played;
    }

    /** A donde salta este compas, si tiene una direccion sin usar y su destino existe. */
    private static OptionalInt destinationOf(
            Score score, MeasureAttributes attributes, int index, Set<Integer> jumpsAlreadyTaken) {
        if (attributes.jump().isEmpty() || jumpsAlreadyTaken.contains(index)) {
            return OptionalInt.empty();
        }
        DirectionJump jump = attributes.jump().get();
        OptionalInt destination = jump.jumpsTo().isEmpty()
                ? OptionalInt.of(0)
                : indexOfSymbol(score, jump.jumpsTo().get());
        if (destination.isPresent()) {
            jumpsAlreadyTaken.add(index);
        }
        return destination;
    }

    private static OptionalInt indexOfSymbol(Score score, DirectionSymbol symbol) {
        for (int i = 0; i < score.measureCount(); i++) {
            if (score.attributesOf(i).symbol().equals(Optional.of(symbol))) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }
}

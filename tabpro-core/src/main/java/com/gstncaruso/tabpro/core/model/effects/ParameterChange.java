package com.gstncaruso.tabpro.core.model.effects;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Un cambio de parametros en medio de la partitura, como el que agrega la
 * ventana de mesa de mezcla del manual. Lo que no se toca no se lista.
 */
public record ParameterChange(Map<SoundParameter, Integer> values, int transitionBeats, boolean everyTrack) {

    public ParameterChange {
        values = Map.copyOf(values);
        if (transitionBeats < 0) {
            throw new IllegalArgumentException("la transición no puede durar menos que nada: " + transitionBeats);
        }
    }

    public static ParameterChange nothing() {
        return new ParameterChange(Map.of(), 0, false);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean changes(SoundParameter parameter) {
        return values.containsKey(parameter);
    }

    public OptionalInt valueOf(SoundParameter parameter) {
        Integer value = values.get(parameter);
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public ParameterChange changing(SoundParameter parameter, int value) {
        Map<SoundParameter, Integer> updated = new EnumMap<>(SoundParameter.class);
        updated.putAll(values);
        updated.put(parameter, Math.clamp(value, parameter.minimum(), parameter.maximum()));
        return new ParameterChange(updated, transitionBeats, everyTrack);
    }

    public ParameterChange leaving(SoundParameter parameter) {
        Map<SoundParameter, Integer> updated = new EnumMap<>(SoundParameter.class);
        updated.putAll(values);
        updated.remove(parameter);
        return new ParameterChange(updated, transitionBeats, everyTrack);
    }

    public ParameterChange over(int beats) {
        return new ParameterChange(values, beats, everyTrack);
    }

    public ParameterChange onEveryTrack(boolean everyTrack) {
        return new ParameterChange(values, transitionBeats, everyTrack);
    }
}

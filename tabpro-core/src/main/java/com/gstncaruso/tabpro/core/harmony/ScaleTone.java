package com.gstncaruso.tabpro.core.harmony;

/**
 * Una nota de una escala ya bajada a una tonica concreta: como se llama, que intervalo
 * es respecto de la tonica y que grado ocupa (1 es la tonica, 2 la siguiente...).
 */
public record ScaleTone(PitchClass pitchClass, Interval interval, int degree) {
}

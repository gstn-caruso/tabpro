package com.gstncaruso.tabpro.format.exchange.ascii;

import java.util.Map;

/** Un golpe leido de la tablatura: en que columna (relativa al compas) y que traste por cuerda. */
record Attack(int localStart, Map<Integer, Integer> fretsByString) {
}

package com.gstncaruso.tabpro.format.exchange.ascii;

import java.util.Map;

record TabColumn(int width, boolean isBar, Map<Integer, Integer> fretsByString) {

    static TabColumn bar() {
        return new TabColumn(1, true, Map.of());
    }

    static TabColumn notes(int width, Map<Integer, Integer> fretsByString) {
        return new TabColumn(width, false, fretsByString);
    }
}

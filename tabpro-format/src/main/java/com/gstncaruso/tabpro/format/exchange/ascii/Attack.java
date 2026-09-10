package com.gstncaruso.tabpro.format.exchange.ascii;

import java.util.Map;

record Attack(int localStart, Map<Integer, Integer> fretsByString) {
}

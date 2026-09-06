package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Un final alternativo de PowerTab: en que posicion del sistema empieza, y
 * los numeros de vuelta que le tocan (1a, 2a...). Si ademas marca D.C./D.S.,
 * ese dato todavia no tiene destino en el modelo y se descarta.
 */
record PowerTabAlternateEnding(int position, List<Integer> numbers) {
}

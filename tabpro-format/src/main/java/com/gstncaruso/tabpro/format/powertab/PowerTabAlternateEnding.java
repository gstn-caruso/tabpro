package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Un final alternativo de PowerTab: en que sistema y en que posicion de ese
 * sistema empieza, y los numeros de vuelta que le tocan (1a, 2a...). Vive en
 * un arreglo a nivel partitura que mezcla los finales de todos los sistemas;
 * el campo "sistema" es el que permite repartirlos. Si ademas marca D.C./D.S.,
 * ese dato todavia no tiene destino en el modelo y se descarta.
 */
record PowerTabAlternateEnding(int system, int position, List<Integer> numbers) {
}

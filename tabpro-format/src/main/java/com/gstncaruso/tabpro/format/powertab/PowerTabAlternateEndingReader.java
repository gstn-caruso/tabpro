package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Lee un final alternativo: hereda de "system symbol" (sistema, posicion y un
 * dato de 32 bits) y guarda los numeros de vuelta en la mitad alta de ese
 * dato, un bit por numero. El D.C./D.S. que puede traer el mismo campo
 * (numeros 9, 10 y 11) no tiene destino en el modelo y se ignora.
 */
final class PowerTabAlternateEndingReader {

    private static final int MAX_NUMBER = 8;

    PowerTabAlternateEnding read(PowerTabByteReader reader) {
        int system = reader.readUnsignedShort();
        int position = reader.readUnsignedByte();
        int data = reader.readInt();

        int numbersMask = (data >>> 16) & 0xFFFF;
        List<Integer> numbers = new ArrayList<>();
        for (int number = 1; number <= MAX_NUMBER; number++) {
            if ((numbersMask & (1 << (number - 1))) != 0) {
                numbers.add(number);
            }
        }
        return new PowerTabAlternateEnding(system, position, numbers);
    }
}

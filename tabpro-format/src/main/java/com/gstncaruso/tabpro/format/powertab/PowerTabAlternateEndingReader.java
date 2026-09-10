package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads an alternate ending: inherits from "system symbol" (system, position, and a
 * 32-bit datum) and stores the round numbers in the upper half of that datum, one bit
 * per number. The D.C./D.S. that the same field can carry (numbers 9, 10, and 11) has
 * no place in the model and is ignored.
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

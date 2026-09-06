package com.gstncaruso.tabpro.format.powertab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lee un sistema: su barra de arranque completa, las barras internas, el tipo
 * y la repeticion de su barra final, y sus pentagramas. Las direcciones y el
 * texto de acorde no tienen destino en el modelo y se descartan; las barras
 * de ritmo (rhythm slash) se cuentan nada mas, porque representarian musica
 * de verdad que todavia no sabemos convertir: quien ensambla la partitura
 * decide si eso alcanza para rechazar el archivo.
 */
final class PowerTabSystemReader {

    private final PowerTabBarlineReader barlineReader = new PowerTabBarlineReader();
    private final PowerTabStaffReader staffReader = new PowerTabStaffReader();

    PowerTabSystem read(PowerTabByteReader reader) {
        reader.skip(16); // rectangulo del sistema: no tiene lugar en el modelo.
        int endBarByte = reader.readUnsignedByte();
        int endBarType = (endBarByte >>> 5) & 0x07;
        int endBarRepeatCount = endBarByte & 0x1f;
        reader.readUnsignedByte(); // espaciado entre posiciones.
        reader.readUnsignedByte(); // espaciado de rhythm slash, arriba.
        reader.readUnsignedByte(); // espaciado de rhythm slash, abajo.
        reader.readUnsignedByte(); // espaciado extra.

        PowerTabBarline startBar = barlineReader.read(reader);

        skipVector(reader, PowerTabAuxiliaryReader::skipDirection);
        skipVector(reader, PowerTabAuxiliaryReader::skipChordText);
        int rhythmSlashCount = skipVector(reader, PowerTabAuxiliaryReader::skipRhythmSlash);

        List<PowerTabStaff> staves = readVector(reader, staffReader::read);
        List<PowerTabBarline> internalBarlines = readVector(reader, barlineReader::read);

        return new PowerTabSystem(startBar, internalBarlines, endBarType, endBarRepeatCount, staves, rhythmSlashCount);
    }

    private static int skipVector(PowerTabByteReader reader, Consumer<PowerTabByteReader> skipOne) {
        int count = reader.readCount();
        for (int i = 0; i < count; i++) {
            reader.readClassInformation();
            skipOne.accept(reader);
        }
        return count;
    }

    private static <T> List<T> readVector(PowerTabByteReader reader, Function<PowerTabByteReader, T> readOne) {
        int count = reader.readCount();
        List<T> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            reader.readClassInformation();
            items.add(readOne.apply(reader));
        }
        return items;
    }
}

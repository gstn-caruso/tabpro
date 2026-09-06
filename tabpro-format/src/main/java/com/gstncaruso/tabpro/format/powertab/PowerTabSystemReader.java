package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

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

        reader.skipVector(PowerTabAuxiliaryReader::skipDirection);
        reader.skipVector(PowerTabAuxiliaryReader::skipChordText);
        int rhythmSlashCount = reader.skipVector(PowerTabAuxiliaryReader::skipRhythmSlash);

        List<PowerTabStaff> staves = reader.readVector(staffReader::read);
        List<PowerTabBarline> internalBarlines = reader.readVector(barlineReader::read);

        return new PowerTabSystem(startBar, internalBarlines, endBarType, endBarRepeatCount, staves, rhythmSlashCount);
    }
}

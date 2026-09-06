package com.gstncaruso.tabpro.format.powertab;

/**
 * Deja atras, byte a byte, las secciones de PowerTab que todavia no tienen
 * destino en el modelo de tabpro: diagramas de acorde, texto flotante,
 * dinamicas, direcciones, texto de acorde y las fuentes del documento.
 * Ninguna de ellas cambia el tono, la duracion o la cuerda de una nota: son
 * anotaciones y decoracion, asi que se leen para no perder la sincronia del
 * archivo y se descartan a proposito, no por descuido.
 */
final class PowerTabAuxiliaryReader {

    private PowerTabAuxiliaryReader() {
    }

    /** Un "chord name": clave y bajo, formula, modificaciones, tipo y traste. Siempre 6 bytes. */
    static void skipChordName(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** Un "font setting": el nombre de la tipografia y 15 bytes fijos (tamano, peso, estilo, color). */
    static void skipFontSetting(PowerTabByteReader reader) {
        reader.readMfcString();
        reader.skip(15);
    }

    /** Una direccion: su posicion y un vector chico de simbolos de 16 bits. */
    static void skipDirection(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // posicion.
        int count = reader.readUnsignedByte();
        reader.skip(count * 2);
    }

    /** Un texto de acorde: su posicion y un "chord name" (6 bytes fijos). */
    static void skipChordText(PowerTabByteReader reader) {
        reader.readUnsignedByte(); // posicion.
        skipChordName(reader);
    }

    /** Una barra de ritmo (rhythm slash): posicion, beaming y datos, todo fijo. */
    static void skipRhythmSlash(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** Una dinamica: sistema, pentagrama, posicion y volumen, todo fijo. */
    static void skipDynamic(PowerTabByteReader reader) {
        reader.skip(6);
    }

    /** Un texto flotante: el texto, un rectangulo fijo, una bandera y un "font setting". */
    static void skipFloatingText(PowerTabByteReader reader) {
        reader.readMfcString();
        reader.skip(16); // rectangulo: cuatro enteros de 32 bits.
        reader.readUnsignedByte(); // banderas de alineacion y borde.
        skipFontSetting(reader);
    }

    /** Un diagrama de acorde: su nombre (6 bytes fijos), el traste superior y los trastes por cuerda. */
    static void skipChordDiagram(PowerTabByteReader reader) {
        skipChordName(reader);
        reader.readUnsignedByte(); // traste superior.
        reader.readSmallVectorOfUnsignedBytes();
    }
}

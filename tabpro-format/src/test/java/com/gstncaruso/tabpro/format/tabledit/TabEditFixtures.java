package com.gstncaruso.tabpro.format.tabledit;

/**
 * Arma fixtures minimos de archivos TEF3 validos, para no repetir en cada test
 * los 256 bytes de encabezado que TablEdit exige.
 */
final class TabEditFixtures {

    static final int HEADER_SIZE = 256;

    private TabEditFixtures() {
    }

    /** Un encabezado TEF3 valido, con el tempo inicial pedido y ninguna seccion opcional. */
    static TabEditFileWriter minimalHeader(int initialBpm) {
        TabEditFileWriter writer = new TabEditFileWriter();
        writer.padTo(3);
        writer.writeUnsignedByte(3); // majorVersion
        writer.padTo(6);
        writer.writeShort(initialBpm);
        writer.padTo(56);
        writer.writeUnsignedByte('t').writeUnsignedByte('b').writeUnsignedByte('e').writeUnsignedByte('d');
        writer.padTo(202);
        writer.writeShort(4); // wOldNum
        writer.writeUnsignedByte(4); // wFormatLo
        writer.writeUnsignedByte(10); // wFormatHi
        writer.padTo(HEADER_SIZE);
        return writer;
    }
}

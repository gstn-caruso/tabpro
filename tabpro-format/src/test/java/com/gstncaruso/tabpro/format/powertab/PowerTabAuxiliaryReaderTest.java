package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/**
 * Cada seccion descartada tiene que consumir exactamente sus bytes: un
 * centinela puesto justo despues confirma que no se lee ni de mas ni de
 * menos.
 */
class PowerTabAuxiliaryReaderTest {

    private static final int SENTINEL = 0x99;

    @Test
    void skipsAChordName() {
        assertLeavesTheSentinel(body -> body.write(new byte[6], 0, 6), PowerTabAuxiliaryReader::skipChordName);
    }

    @Test
    void skipsAFontSetting() {
        assertLeavesTheSentinel(body -> {
            writeMfcString(body, "Arial");
            body.write(new byte[15], 0, 15);
        }, PowerTabAuxiliaryReader::skipFontSetting);
    }

    @Test
    void skipsADirectionWithTwoSymbols() {
        assertLeavesTheSentinel(body -> {
            body.write(9); // posicion.
            body.write(2); // dos simbolos de 16 bits.
            body.write(new byte[4], 0, 4);
        }, PowerTabAuxiliaryReader::skipDirection);
    }

    @Test
    void skipsAChordText() {
        assertLeavesTheSentinel(body -> {
            body.write(4); // posicion.
            body.write(new byte[6], 0, 6); // chord name.
        }, PowerTabAuxiliaryReader::skipChordText);
    }

    @Test
    void skipsARhythmSlash() {
        assertLeavesTheSentinel(body -> body.write(new byte[6], 0, 6), PowerTabAuxiliaryReader::skipRhythmSlash);
    }

    @Test
    void skipsADynamic() {
        assertLeavesTheSentinel(body -> body.write(new byte[6], 0, 6), PowerTabAuxiliaryReader::skipDynamic);
    }

    @Test
    void skipsATempoMarker() {
        assertLeavesTheSentinel(body -> {
            body.write(new byte[7], 0, 7); // system symbol.
            writeMfcString(body, "Fast Rock");
        }, PowerTabAuxiliaryReader::skipTempoMarker);
    }

    @Test
    void skipsFloatingText() {
        assertLeavesTheSentinel(body -> {
            writeMfcString(body, "foo\nbaz");
            body.write(new byte[16], 0, 16); // rectangulo.
            body.write(0); // banderas.
            writeMfcString(body, "Arial");
            body.write(new byte[15], 0, 15); // resto del font setting.
        }, PowerTabAuxiliaryReader::skipFloatingText);
    }

    @Test
    void skipsAChordDiagram() {
        assertLeavesTheSentinel(body -> {
            body.write(new byte[6], 0, 6); // chord name.
            body.write(2); // traste superior.
            body.write(6); // seis cuerdas.
            body.write(new byte[6], 0, 6);
        }, PowerTabAuxiliaryReader::skipChordDiagram);
    }

    private static void assertLeavesTheSentinel(Consumer<ByteArrayOutputStream> body, Consumer<PowerTabByteReader> skip) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        body.accept(out);
        out.write(SENTINEL);
        PowerTabByteReader reader = new PowerTabByteReader(out.toByteArray());

        skip.accept(reader);

        assertEquals(SENTINEL, reader.readUnsignedByte());
    }

    private static void writeMfcString(ByteArrayOutputStream out, String text) {
        out.write(text.length());
        out.writeBytes(text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }
}

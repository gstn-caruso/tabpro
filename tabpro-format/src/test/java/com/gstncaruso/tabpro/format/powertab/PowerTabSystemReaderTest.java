package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/** Un sistema minimo armado a mano: sin direcciones ni acordes, un pentagrama vacio y sin barras internas. */
class PowerTabSystemReaderTest {

    private final PowerTabSystemReader reader = new PowerTabSystemReader();

    @Test
    void readsAMinimalSystem() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[16], 0, 16); // rectangulo.
        out.write((PowerTabBarline.REPEAT_END << 5) | 4); // barra final: repite 4 veces.
        out.write(20); // espaciado entre posiciones.
        out.write(0);
        out.write(0);
        out.write(0);

        writeBarline(out); // barra de arranque.

        writeEmptyVector(out); // direcciones.
        writeEmptyVector(out); // texto de acorde.
        writeEmptyVector(out); // rhythm slash.

        // un pentagrama.
        out.write(1);
        out.write(0);
        out.write(0x00); // etiqueta de clase: referencia corta.
        out.write(0x00);
        writeStaff(out);

        writeEmptyVector(out); // barras internas.

        PowerTabSystem system = reader.read(new PowerTabByteReader(out.toByteArray()));

        assertEquals(PowerTabBarline.REPEAT_END, system.endBarType());
        assertEquals(4, system.endBarRepeatCount());
        assertEquals(0, system.startBar().position());
        assertEquals(1, system.staves().size());
        assertEquals(0, system.rhythmSlashCount());
        assertTrue(system.internalBarlines().isEmpty());
    }

    private static void writeBarline(ByteArrayOutputStream out) {
        out.write(0); // posicion.
        out.write(0); // tipo bar, sin repeticion.
        out.write(0); // armadura: Do mayor.
        out.write(new byte[4], 0, 4); // medida: todo en cero (comun/corte apagados).
        out.write(0); // pulsos.
        out.write(0); // letra de marca de ensayo.
        out.write(0); // descripcion vacia.
    }

    private static void writeStaff(ByteArrayOutputStream out) {
        out.write(0x06); // clave treble, 6 cuerdas.
        out.write(9);
        out.write(9);
        out.write(0);
        out.write(0);
        writeEmptyVector(out); // voz principal.
        writeEmptyVector(out); // segunda voz.
    }

    private static void writeEmptyVector(ByteArrayOutputStream out) {
        out.write(0);
        out.write(0);
    }
}

package com.gstncaruso.tabpro.core.model.bars;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/**
 * Verifica que el orden de declaración de DirectionSymbol y DirectionJump no
 * cambia.
 *
 * <strong>Por qué importa:</strong> El lector de archivos Guitar Pro 5 ({@code
 * GuitarProHeaderReader.readDirections()}) depende de la posición ordinal de
 * cada valor del enum para deserializar correctamente el bloque de direcciones
 * musicales del archivo .gp5. Si se reordena un enum (alfabéticamente,
 * agrupando, o cualquier otro motivo), se quiebranta la lectura en silencio: el
 * archivo seguirá abriendo, pero las anotaciones de destino (Coda, Segno,
 * etc.) y los saltos (Da Capo al Coda, Da Segno, etc.) se asignarán a posiciones
 * incorrectas, resultando en partituras con instrucciones de repetición
 * corrupta.
 *
 * <strong>El orden fijo, según la especificación de Guitar Pro 5:</strong>
 * <ul>
 *   <li>Símbolos (5): Coda, Doble Coda, Segno, Segno Segno, Fine</li>
 *   <li>Saltos (14): Da Capo, Da Capo al Coda, Da Capo al Doble Coda, Da Capo
 *       al Fine, Da Segno, Da Segno al Coda, Da Segno al Doble Coda, Da
 *       Segno al Fine, Da Segno Segno, Da Segno Segno al Coda, Da Segno
 *       Segno al Doble Coda, Da Segno Segno al Fine, Da Coda, Da Doble Coda
 *   </li>
 * </ul>
 *
 * <strong>Antes de cambiar el orden, considerar:</strong> ¿el cambio realmente
 * aporta mantenibilidad, legibilidad o correctitud? Si la única razón es
 * estética o alfabética, la fragilidad que introduce no vale. Si realmente hay
 * que cambiar el orden (ej.: porque Guitar Pro 6+ cambió el formato), actualizar
 * este test y asegurar que la lógica de lectura se actualice en consonancia.
 */
final class DirectionEnumOrderTest {

    @Test
    void symbolOrderMatchesGuitarPro5Format() {
        String[] expected = {"CODA", "DOUBLE_CODA", "SEGNO", "SEGNO_SEGNO", "FINE"};
        String[] actual = new String[DirectionSymbol.values().length];

        for (int i = 0; i < DirectionSymbol.values().length; i++) {
            actual[i] = DirectionSymbol.values()[i].name();
        }

        assertArrayEquals(expected, actual,
            "DirectionSymbol enum order has changed. This breaks GuitarProHeaderReader.readDirections() "
                + "because it reads the .gp5 format by relying on the exact ordinal position of each symbol. "
                + "If you reordered this enum, revert it and update GuitarProHeaderReader to handle the new format, "
                + "or ensure you understand the impact on all .gp5 files already written with the old order.");
    }

    @Test
    void jumpOrderMatchesGuitarPro5Format() {
        String[] expected = {
            "DA_CAPO",
            "DA_CAPO_AL_CODA",
            "DA_CAPO_AL_DOUBLE_CODA",
            "DA_CAPO_AL_FINE",
            "DA_SEGNO",
            "DA_SEGNO_AL_CODA",
            "DA_SEGNO_AL_DOUBLE_CODA",
            "DA_SEGNO_AL_FINE",
            "DA_SEGNO_SEGNO",
            "DA_SEGNO_SEGNO_AL_CODA",
            "DA_SEGNO_SEGNO_AL_DOUBLE_CODA",
            "DA_SEGNO_SEGNO_AL_FINE",
            "DA_CODA",
            "DA_DOUBLE_CODA"
        };
        String[] actual = new String[DirectionJump.values().length];

        for (int i = 0; i < DirectionJump.values().length; i++) {
            actual[i] = DirectionJump.values()[i].name();
        }

        assertArrayEquals(expected, actual,
            "DirectionJump enum order has changed. This breaks GuitarProHeaderReader.readDirections() "
                + "because it reads the .gp5 format by relying on the exact ordinal position of each jump. "
                + "If you reordered this enum, revert it and update GuitarProHeaderReader to handle the new format, "
                + "or ensure you understand the impact on all .gp5 files already written with the old order.");
    }
}

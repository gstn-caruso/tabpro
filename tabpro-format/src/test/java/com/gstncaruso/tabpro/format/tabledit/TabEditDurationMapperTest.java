package com.gstncaruso.tabpro.format.tabledit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

/**
 * TablEdit numera sus 32 figuras posibles con un solo codigo de 0 a 31: la
 * mayoria son la combinacion de una figura base, un puntillo o un tresillo,
 * pero unos pocos codigos son puro relleno (repiten una figura simple) y
 * cuatro codigos piden un doble puntillo que el modelo de tabpro no puede
 * representar.
 */
class TabEditDurationMapperTest {

    @ParameterizedTest
    @CsvSource({
            "0, WHOLE, false",
            "3, HALF, false",
            "6, QUARTER, false",
            "9, EIGHTH, false",
            "12, SIXTEENTH, false",
            "15, THIRTY_SECOND, false",
            "18, SIXTY_FOURTH, false",
            "1, HALF, true",
            "4, QUARTER, true",
            "10, SIXTEENTH, true",
            "16, SIXTY_FOURTH, true",
            "31, WHOLE, true",
    })
    void mapeaLasFigurasSimplesYPunteadas(int code, NoteValue value, boolean dotted) {
        Duration duration = TabEditDurationMapper.toDuration(code);

        assertEquals(value, duration.value());
        assertEquals(dotted, duration.dotted());
    }

    @ParameterizedTest
    @CsvSource({"2, WHOLE", "5, HALF", "8, QUARTER", "11, EIGHTH", "14, SIXTEENTH", "17, THIRTY_SECOND"})
    void mapeaLosTresillos(int code, NoteValue value) {
        Duration duration = TabEditDurationMapper.toDuration(code);

        assertEquals(value, duration.value());
        assertEquals(Tuplet.of(3), duration.tuplet());
        assertFalse(duration.dotted());
    }

    @ParameterizedTest
    @CsvSource({"20, SIXTEENTH", "23, SIXTEENTH", "26, SIXTEENTH", "29, SIXTEENTH",
            "21, SIXTY_FOURTH", "24, SIXTY_FOURTH", "27, SIXTY_FOURTH", "30, SIXTY_FOURTH"})
    void losCodigosDeRellenoCaenEnUnaFiguraSimple(int code, NoteValue value) {
        Duration duration = TabEditDurationMapper.toDuration(code);

        assertEquals(value, duration.value());
        assertFalse(duration.dotted());
        assertTrue(duration.tuplet().isPlain());
    }

    @ParameterizedTest
    @CsvSource({"19", "22", "25", "28"})
    void elDoblePuntilloNoSePuedeRepresentarYSeDeclara(int code) {
        ScoreFileException exception =
                assertThrows(ScoreFileException.class, () -> TabEditDurationMapper.toDuration(code));

        assertTrue(exception.getMessage().contains("doble puntillo"));
    }

    @Test
    void unCodigoFueraDeRangoTambienSeDeclara() {
        assertThrows(ScoreFileException.class, () -> TabEditDurationMapper.toDuration(99));
    }
}

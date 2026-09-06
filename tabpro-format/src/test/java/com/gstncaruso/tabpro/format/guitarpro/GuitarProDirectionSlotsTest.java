package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El bloque de direcciones de un .gp5 son diecinueve casilleros seguidos, cada
 * uno apuntando a un compas. Cual es cual lo decide la posicion, asi que leerlos
 * en el orden equivocado no rompe nada de forma visible: el archivo abre igual,
 * con un Coda donde iba un Segno.
 *
 * <p>Estos tests fijan el orden que manda el formato de Guitar Pro 5, para que
 * cambiarlo cueste una falla ruidosa en vez de partituras silenciosamente mal
 * leidas.
 */
class GuitarProDirectionSlotsTest {

    @Test
    void losCincoSimbolosSeLeenEnElOrdenQueMandaElFormato() {
        assertEquals(
                List.of(
                        DirectionSymbol.CODA,
                        DirectionSymbol.DOUBLE_CODA,
                        DirectionSymbol.SEGNO,
                        DirectionSymbol.SEGNO_SEGNO,
                        DirectionSymbol.FINE),
                GuitarProHeaderReader.SYMBOL_SLOTS,
                "El formato de Guitar Pro 5 fija este orden para los simbolos de destino. "
                        + "Cambiarlo hace que los .gp5 se lean mal en silencio: el archivo abre, "
                        + "pero cada simbolo cae en el compas de otro.");
    }

    @Test
    void losCatorceSaltosSeLeenEnElOrdenQueMandaElFormato() {
        assertEquals(
                List.of(
                        DirectionJump.DA_CAPO,
                        DirectionJump.DA_CAPO_AL_CODA,
                        DirectionJump.DA_CAPO_AL_DOUBLE_CODA,
                        DirectionJump.DA_CAPO_AL_FINE,
                        DirectionJump.DA_SEGNO,
                        DirectionJump.DA_SEGNO_AL_CODA,
                        DirectionJump.DA_SEGNO_AL_DOUBLE_CODA,
                        DirectionJump.DA_SEGNO_AL_FINE,
                        DirectionJump.DA_SEGNO_SEGNO,
                        DirectionJump.DA_SEGNO_SEGNO_AL_CODA,
                        DirectionJump.DA_SEGNO_SEGNO_AL_DOUBLE_CODA,
                        DirectionJump.DA_SEGNO_SEGNO_AL_FINE,
                        DirectionJump.DA_CODA,
                        DirectionJump.DA_DOUBLE_CODA),
                GuitarProHeaderReader.JUMP_SLOTS,
                "El formato de Guitar Pro 5 fija este orden para los saltos. "
                        + "Cambiarlo hace que los .gp5 se lean mal en silencio.");
    }

    @Test
    void sonDiecinueveCasillerosEnTotal() {
        assertEquals(
                19,
                GuitarProHeaderReader.SYMBOL_SLOTS.size() + GuitarProHeaderReader.JUMP_SLOTS.size(),
                "El bloque del archivo tiene diecinueve casilleros de dos bytes. "
                        + "Leer de menos corre todo lo que viene despues.");
    }

    @Test
    void cadaDireccionDelModeloTieneSuCasillero() {
        assertEquals(
                DirectionSymbol.values().length,
                GuitarProHeaderReader.SYMBOL_SLOTS.size(),
                "Si el modelo suma un simbolo, hay que decidir en que casillero del archivo cae.");
        assertEquals(
                DirectionJump.values().length,
                GuitarProHeaderReader.JUMP_SLOTS.size(),
                "Si el modelo suma un salto, hay que decidir en que casillero del archivo cae.");
    }
}

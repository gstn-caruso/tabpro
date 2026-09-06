package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** La curva del bend y de la palanca: hasta donde llega y de que lado. */
class BendTest {

    @Test
    void unBendQueSubeLlegaHastaSuPunto() {
        Bend bend = Bend.of(BendType.BEND, 4);

        assertEquals(4, bend.peakQuarterTones());
        assertEquals(4, bend.farthestQuarterTones());
    }

    @Test
    void unaPalancaQueBajaSeAnotaConSuCaida() {
        Bend dive = new Bend(BendType.BEND_RELEASE, List.of(
                BendPoint.at(0, 0), BendPoint.at(30, -8), BendPoint.at(BendPoint.LAST_POSITION, 0)));

        assertEquals(0, dive.peakQuarterTones());
        assertEquals(-8, dive.farthestQuarterTones());
    }

    @Test
    void cuandoLaCurvaVaParaLosDosLadosGanaElPuntoMasLejano() {
        Bend vaYViene = new Bend(BendType.BEND_RELEASE, List.of(
                BendPoint.at(0, 0), BendPoint.at(20, 2), BendPoint.at(40, -6),
                BendPoint.at(BendPoint.LAST_POSITION, 0)));

        assertEquals(-6, vaYViene.farthestQuarterTones());
    }
}

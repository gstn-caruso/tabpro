package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

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

    @Test
    void unDipDeLaPalancaBajaYVuelve() {
        Bend dip = Bend.of(BendType.DIP, 4);

        assertEquals(-4, dip.farthestQuarterTones());
        assertEquals(0, dip.points().getLast().quarterTones());
    }

    @Test
    void unInvertedDipDeLaPalancaSubeYVuelve() {
        Bend invertedDip = Bend.of(BendType.INVERTED_DIP, 4);

        assertEquals(4, invertedDip.farthestQuarterTones());
        assertEquals(0, invertedDip.points().getLast().quarterTones());
    }

    @Test
    void unDiveDeLaPalancaBajaYSeQueda() {
        Bend dive = Bend.of(BendType.DIVE, 4);

        assertEquals(-4, dive.farthestQuarterTones());
        assertEquals(-4, dive.points().getLast().quarterTones());
    }

    @Test
    void unReturnDeLaPalancaSubeYSeQueda() {
        Bend returnType = Bend.of(BendType.RETURN, 4);

        assertEquals(4, returnType.farthestQuarterTones());
        assertEquals(4, returnType.points().getLast().quarterTones());
    }

    @Test
    void unReleaseUpDeLaPalancaEmpiezaAbajoYSube() {
        Bend releaseUp = Bend.of(BendType.RELEASE_UP, 4);

        assertEquals(-4, releaseUp.points().getFirst().quarterTones());
        assertEquals(0, releaseUp.points().getLast().quarterTones());
    }

    @Test
    void unReleaseDownDeLaPalancaEmpiezaArribaYBaja() {
        Bend releaseDown = Bend.of(BendType.RELEASE_DOWN, 4);

        assertEquals(4, releaseDown.points().getFirst().quarterTones());
        assertEquals(0, releaseDown.points().getLast().quarterTones());
    }

    @Test
    void losSeisTiposDeLaPalancaNoSonLosCincoDelBend() {
        assertEquals(5, BendType.bendTypes().size());
        assertEquals(6, BendType.tremoloBarTypes().size());
        assertTrue(java.util.Collections.disjoint(BendType.bendTypes(), BendType.tremoloBarTypes()));
    }
}

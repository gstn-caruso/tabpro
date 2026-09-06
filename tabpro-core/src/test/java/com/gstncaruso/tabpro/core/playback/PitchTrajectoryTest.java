package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import org.junit.jupiter.api.Test;

/**
 * Como se mueve la altura de una nota a lo largo de su duracion: bends,
 * palanca, slides y vibrato son distintas formas de la misma curva.
 */
class PitchTrajectoryTest {

    @Test
    void unaCurvaPlanaNoMueveLaAltura() {
        PitchTrajectory flat = PitchTrajectory.flat();

        assertEquals(0.0, flat.semitonesAt(0));
        assertEquals(0.0, flat.semitonesAt(500));
    }

    @Test
    void antesDelPrimerPuntoValeLoQueValeElPrimero() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(100, 2.0), new PitchTrajectory.Point(200, 0.0)));

        assertEquals(2.0, trajectory.semitonesAt(0));
    }

    @Test
    void despuesDelUltimoPuntoValeLoQueValeElUltimo() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));

        assertEquals(2.0, trajectory.semitonesAt(1000));
    }

    @Test
    void interpolaLinealmenteEntreDosPuntos() {
        PitchTrajectory trajectory = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));

        assertEquals(1.0, trajectory.semitonesAt(50));
    }

    @Test
    void seConstruyeAPartirDeUnBendEscalandoLasPosicionesALosTicksDeLaNota() {
        Bend bend = Bend.of(BendType.BEND, 4); // sube un tono (4 cuartos de tono)
        PitchTrajectory trajectory = PitchTrajectory.of(bend, 960);

        assertEquals(0.0, trajectory.semitonesAt(0));
        assertEquals(2.0, trajectory.semitonesAt(960));
    }

    @Test
    void unSaltoInstantaneoNoInterpolaEntreElAntesYElDespues() {
        PitchTrajectory trajectory = PitchTrajectory.flat()
                .withJumpAt(500, 5.0);

        assertEquals(0.0, trajectory.semitonesAt(499));
        assertEquals(5.0, trajectory.semitonesAt(500));
        assertEquals(5.0, trajectory.semitonesAt(600));
    }

    @Test
    void rampingToLlegaGradualmenteAlValorPedido() {
        PitchTrajectory trajectory = PitchTrajectory.flat().rampingTo(1000, 2.0, 100);

        assertEquals(0.0, trajectory.semitonesAt(899));
        assertEquals(1.0, trajectory.semitonesAt(950));
        assertEquals(2.0, trajectory.semitonesAt(1000));
        assertEquals(2.0, trajectory.semitonesAt(2000));
    }

    @Test
    void plusSumaDosCurvasEnCadaPuntoQueCualquieraDeLasDosDefine() {
        PitchTrajectory a = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 0.0), new PitchTrajectory.Point(100, 2.0)));
        PitchTrajectory b = new PitchTrajectory(java.util.List.of(
                new PitchTrajectory.Point(0, 1.0), new PitchTrajectory.Point(100, 1.0)));

        PitchTrajectory combined = a.plus(b);

        assertEquals(1.0, combined.semitonesAt(0));
        assertEquals(3.0, combined.semitonesAt(100));
    }

    @Test
    void unaVibradaOscilaAlrededorDeCero() {
        PitchTrajectory vibrato = PitchTrajectory.vibrato(960, 0.5, 240);

        assertTrue(vibrato.semitonesAt(0) <= 0.0001);
        boolean tieneAlgunPuntoPositivo = false;
        boolean tieneAlgunPuntoNegativo = false;
        for (long tick = 0; tick <= 960; tick += 60) {
            double value = vibrato.semitonesAt(tick);
            if (value > 0) {
                tieneAlgunPuntoPositivo = true;
            }
            if (value < 0) {
                tieneAlgunPuntoNegativo = true;
            }
        }
        assertTrue(tieneAlgunPuntoPositivo && tieneAlgunPuntoNegativo);
    }

    @Test
    void unaCurvaPlanaSiempreEntraEnCualquierLimite() {
        assertTrue(PitchTrajectory.flat().staysWithin(0.0));
    }

    @Test
    void unaCurvaJustoEnElLimiteEntra() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, 2.0);

        assertTrue(trajectory.staysWithin(2.0));
    }

    @Test
    void unaCurvaQuePasaElLimiteNoEntra() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, 2.5);

        assertTrue(!trajectory.staysWithin(2.0));
    }

    @Test
    void elLimiteMiraElValorAbsolutoDeLaVariacion() {
        PitchTrajectory trajectory = PitchTrajectory.ramp(0, 0.0, 100, -3.0);

        assertTrue(!trajectory.staysWithin(2.0));
    }
}

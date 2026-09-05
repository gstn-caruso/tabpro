package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import org.junit.jupiter.api.Test;

/**
 * La altura que suena un armonico: los naturales dependen del nodo de la
 * cuerda (el traste no es la altura), los demas se transportan desde la
 * nota pisada.
 */
class HarmonicPitchTest {

    private static final Pitch OPEN_STRING = new Pitch(40); // mi grave

    @Test
    void elArmonicoNaturalDelTraste12EsUnaOctavaSobreLaCuerdaAlAire() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(52), 12);

        assertEquals(new Pitch(52), pitch); // 40 + 12
    }

    @Test
    void elArmonicoNaturalDelTraste7EsUnaDuodecimaSobreLaCuerdaAlAire() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(47), 7);

        assertEquals(new Pitch(59), pitch); // 40 + 19
    }

    @Test
    void elArmonicoNaturalDelTraste19EsElMismoNodoQueElDelTraste7() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(59), 19);

        assertEquals(new Pitch(59), pitch);
    }

    @Test
    void elArmonicoNaturalDelTraste5EsDosOctavas() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(45), 5);

        assertEquals(new Pitch(64), pitch); // 40 + 24
    }

    @Test
    void elArmonicoNaturalDelTraste4EsDosOctavasYTercera() {
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, new Pitch(44), 4);

        assertEquals(new Pitch(68), pitch); // 40 + 28
    }

    @Test
    void unTrasteSinNodoConocidoSuenaComoLaNotaPisada() {
        Pitch fretted = new Pitch(43);
        Pitch pitch = HarmonicPitch.of(HarmonicType.NATURAL, OPEN_STRING, fretted, 3);

        assertEquals(fretted, pitch);
    }

    @Test
    void elArmonicoArtificialSuenaUnaOctavaSobreLaNotaPisada() {
        Pitch fretted = new Pitch(50);
        Pitch pitch = HarmonicPitch.of(HarmonicType.ARTIFICIAL, OPEN_STRING, fretted, 10);

        assertEquals(new Pitch(62), pitch);
    }

    @Test
    void elArmonicoPellizcadoTambienSeTransportaUnaOctava() {
        Pitch fretted = new Pitch(55);
        Pitch pitch = HarmonicPitch.of(HarmonicType.PINCH, OPEN_STRING, fretted, 15);

        assertEquals(new Pitch(67), pitch);
    }
}

package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntervalTest {

    @Test
    void laFundamentalNoMueveNada() {
        assertEquals(0, Interval.ROOT.letterSteps());
        assertEquals(0, Interval.ROOT.semitones());
    }

    @Test
    void laTerceraMayorSonCuatroSemitonosYDosLetras() {
        assertEquals(2, Interval.MAJOR_THIRD.letterSteps());
        assertEquals(4, Interval.MAJOR_THIRD.semitones());
    }

    @Test
    void laQuintaDisminuidaYLaCuartaAumentadaSuenanIgualPeroSeEscribenDistinto() {
        assertEquals(Interval.DIMINISHED_FIFTH.semitones(), Interval.AUGMENTED_FOURTH.semitones());
        assertEquals(4, Interval.DIMINISHED_FIFTH.letterSteps());
        assertEquals(3, Interval.AUGMENTED_FOURTH.letterSteps());
    }

    @Test
    void aplicadoSobreUnaFundamentalDaLaNotaEsperada() {
        PitchClass do_ = PitchClass.of("Do");
        assertEquals(PitchClass.of("Mi"), Interval.MAJOR_THIRD.from(do_));
        assertEquals(PitchClass.of("Sol"), Interval.PERFECT_FIFTH.from(do_));
        assertEquals(PitchClass.of("Sib"), Interval.MINOR_SEVENTH.from(do_));
        assertEquals(PitchClass.of("Si"), Interval.MAJOR_SEVENTH.from(do_));
    }

    @Test
    void laNovenaYlaSegundaSuenanIgualPeroTienenEtiquetaDistinta() {
        assertEquals(Interval.MAJOR_SECOND.semitones(), Interval.MAJOR_NINTH.semitones() % 12);
        assertEquals("2", Interval.MAJOR_SECOND.label());
        assertEquals("9", Interval.MAJOR_NINTH.label());
    }

    @Test
    void encuentraElIntervaloPorPasosYSemitonos() {
        assertEquals(Interval.MAJOR_THIRD, Interval.matching(2, 4).orElseThrow());
        assertEquals(Interval.MINOR_THIRD, Interval.matching(2, 3).orElseThrow());
    }
}

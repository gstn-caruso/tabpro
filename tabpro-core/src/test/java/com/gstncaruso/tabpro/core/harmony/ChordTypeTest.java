package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChordTypeTest {

    @Test
    void elMayorEsFundamentalTerceraMayorYQuintaJusta() {
        assertEquals(
                Set.of(Interval.ROOT, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH),
                intervalsOf(ChordType.MAJOR));
    }

    @Test
    void elMenorTieneTerceraMenor() {
        assertTrue(intervalsOf(ChordType.MINOR).contains(Interval.MINOR_THIRD));
        assertFalse(intervalsOf(ChordType.MINOR).contains(Interval.MAJOR_THIRD));
    }

    @Test
    void elSufijoEsElQueUsaGuitarPro() {
        assertEquals("", ChordType.MAJOR.suffix());
        assertEquals("m", ChordType.MINOR.suffix());
        assertEquals("7", ChordType.SEVENTH.suffix());
        assertEquals("m7", ChordType.MINOR_SEVENTH.suffix());
        assertEquals("maj7", ChordType.MAJOR_SEVENTH.suffix());
        assertEquals("dim7", ChordType.DIMINISHED_SEVENTH.suffix());
        assertEquals("m7b5", ChordType.MINOR_SEVENTH_FLAT_FIVE.suffix());
        assertEquals("mMaj7", ChordType.MINOR_MAJOR_SEVENTH.suffix());
    }

    @Test
    void elAcordeDePotenciaSoloTieneFundamentalYQuinta() {
        assertEquals(Set.of(Interval.ROOT, Interval.PERFECT_FIFTH), intervalsOf(ChordType.FIVE));
    }

    @Test
    void elDisminuidoSieteUsaLaSeptimaDisminuida() {
        assertTrue(intervalsOf(ChordType.DIMINISHED_SEVENTH).contains(Interval.DIMINISHED_SEVENTH));
    }

    @Test
    void laQuintaEsOpcionalEnLosAcordesDeSeptimaOMas() {
        assertFalse(essentialOf(ChordType.SEVENTH).contains(Interval.PERFECT_FIFTH));
        assertTrue(essentialOf(ChordType.SEVENTH).contains(Interval.MINOR_SEVENTH));
    }

    @Test
    void laQuintaAlteradaNuncaEsOpcionalPorqueDefineElAcorde() {
        assertTrue(essentialOf(ChordType.SEVEN_FLAT_FIVE).contains(Interval.DIMINISHED_FIFTH));
    }

    @Test
    void hayVeintiseisTiposDeAcordeComoOfreceGuitarPro() {
        assertEquals(26, ChordType.values().length);
    }

    private static Set<Interval> intervalsOf(ChordType type) {
        return type.tones().stream().map(ChordTone::interval).collect(java.util.stream.Collectors.toSet());
    }

    private static List<Interval> essentialOf(ChordType type) {
        return type.tones().stream()
                .filter(ChordTone::essential)
                .map(ChordTone::interval)
                .toList();
    }
}

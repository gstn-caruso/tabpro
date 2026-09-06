package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ParameterChangeTest {

    @Test
    void aChangeThatChangesNothingIsEmpty() {
        assertTrue(ParameterChange.nothing().isEmpty());
    }

    @Test
    void onlyTheParametersThatChangeAreListed() {
        ParameterChange change = ParameterChange.nothing().changing(SoundParameter.VOLUME, 80);

        assertTrue(change.changes(SoundParameter.VOLUME));
        assertFalse(change.changes(SoundParameter.PAN));
        assertEquals(OptionalInt.of(80), change.valueOf(SoundParameter.VOLUME));
        assertEquals(OptionalInt.empty(), change.valueOf(SoundParameter.PAN));
    }

    @Test
    void aValueOutOfRangeIsBroughtBackIn() {
        ParameterChange change = ParameterChange.nothing().changing(SoundParameter.VOLUME, 500);

        assertEquals(OptionalInt.of(127), change.valueOf(SoundParameter.VOLUME));
    }

    @Test
    void aParameterCanBeLeftAlone() {
        ParameterChange change = ParameterChange.nothing()
                .changing(SoundParameter.TEMPO, 90)
                .leaving(SoundParameter.TEMPO);

        assertTrue(change.isEmpty());
    }

    @Test
    void theTempoIsTheOnlyGlobalParameter() {
        assertTrue(SoundParameter.TEMPO.isGlobal());
        assertFalse(SoundParameter.VOLUME.isGlobal());
    }

    @Test
    void aTransitionAndItsScopeTravelWithTheChange() {
        ParameterChange change = ParameterChange.nothing()
                .changing(SoundParameter.VOLUME, 80)
                .over(4)
                .onEveryTrack(true);

        assertEquals(4, change.transitionBeats());
        assertTrue(change.everyTrack());
    }
}

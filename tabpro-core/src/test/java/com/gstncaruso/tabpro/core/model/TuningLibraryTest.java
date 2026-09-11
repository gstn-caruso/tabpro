package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TuningLibraryTest {

    @Test
    void thePercussionKitTuningIsNamedByALanguageNeutralLibraryId() {
        assertEquals(new TuningName.Library("percussion"), PercussionKit.tuning().name());
    }

    @Test
    void everyGuitarTuningIsNamedByItsOwnLibraryId() {
        List<TuningName> names = TuningLibrary.guitars().stream().map(Tuning::name).toList();

        assertTrue(names.stream().allMatch(TuningName.Library.class::isInstance), names.toString());
        assertEquals(names.size(), Set.copyOf(names).size());
    }

    @Test
    void theStandardGuitarIsNamedGuitarStandard() {
        assertEquals(new TuningName.Library("guitar.standard"), TuningLibrary.standardGuitar().name());
    }
}

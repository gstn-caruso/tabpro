package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TuningLibraryTest {

    @Test
    void thePercussionKitTuningIsNamedByALanguageNeutralLibraryId() {
        assertEquals(new TuningName.Library("percussion"), PercussionKit.tuning().name());
    }

    @Test
    void everyGuitarAndBassTuningIsNamedByItsOwnLibraryId() {
        List<TuningName> names = Stream.concat(TuningLibrary.guitars().stream(), TuningLibrary.basses().stream())
                .map(Tuning::name)
                .toList();

        assertTrue(names.stream().allMatch(TuningName.Library.class::isInstance), names.toString());
        assertEquals(names.size(), Set.copyOf(names).size());
    }

    @Test
    void theStandardGuitarIsNamedGuitarStandard() {
        assertEquals(new TuningName.Library("guitar.standard"), TuningLibrary.standardGuitar().name());
    }

    @Test
    void theStandardBassIsNamedBassStandard() {
        assertEquals(new TuningName.Library("bass.standard"), TuningLibrary.standardBass().name());
    }
}

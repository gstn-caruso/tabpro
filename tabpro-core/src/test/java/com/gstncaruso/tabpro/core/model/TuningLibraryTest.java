package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TuningLibraryTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "guitar.openD5 | 62,57,50,50,45,38",
            "guitar.openF | 65,60,53,48,45,41",
            "guitar.openG6 | 64,59,55,50,43,38",
            "guitar.twoStepsDown | 60,55,51,46,41,36",
            "guitar.dropDVariant | 64,57,55,50,45,38",
            "guitar.doubleDropD | 62,59,55,50,45,38",
            "guitar.dropE | 66,61,57,52,47,40",
            "guitar.openCmaj7 | 64,59,55,52,43,36",
            "guitar.openD6 | 62,59,54,50,45,38",
            "guitar.openEsus11 | 64,59,55,52,45,40",
            "bass.twoStepsDown | 39,34,29,24",
            "lute.standard | 64,59,54,50,45,40"
    })
    void alternateTuningsHaveTheExpectedOpenStringPitches(String id, String midiNumbers) {
        Tuning tuning = TuningLibrary.all().stream()
                .filter(candidate -> candidate.name().equals(new TuningName.Library(id)))
                .findFirst().orElseThrow();
        List<Pitch> expected = Stream.of(midiNumbers.split(","))
                .map(Integer::parseInt).map(Pitch::new).toList();

        assertEquals(expected, tuning.strings());
        assertEquals(tuning, TuningLibrary.identify(expected));
        assertTrue(TuningLibrary.withStringCount(expected.size()).contains(tuning));
    }

    @Test
    void thePercussionKitTuningIsNamedByALanguageNeutralLibraryId() {
        assertEquals(new TuningName.Library("percussion"), PercussionKit.tuning().name());
    }

    @Test
    void everyLibraryTuningIsNamedByItsOwnLibraryId() {
        List<TuningName> names = Stream.concat(TuningLibrary.all().stream(), Stream.of(PercussionKit.tuning()))
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

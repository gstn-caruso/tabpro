package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TuningLibraryTest {

    @Test
    void thePercussionKitTuningIsNamedByALanguageNeutralLibraryId() {
        assertEquals(new TuningName.Library("percussion"), PercussionKit.tuning().name());
    }
}

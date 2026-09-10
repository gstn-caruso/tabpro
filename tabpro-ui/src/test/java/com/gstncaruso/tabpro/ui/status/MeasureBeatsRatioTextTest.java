package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeasureBeatsRatioTextTest {

    @Test
    void fourQuartersMatchFourFourExactly() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));

        assertEquals("4.000 : 4.000", MeasureBeatsRatioText.of(measure));
    }

    @Test
    void aLoneQuarterRestIsOneOverFour() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());

        assertEquals("1.000 : 4.000", MeasureBeatsRatioText.of(measure));
    }

    @Test
    void aFractionOfABeatShowsUpWithThreeDecimals() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.EIGHTH))));

        assertEquals("1.500 : 4.000", MeasureBeatsRatioText.of(measure));
    }
}

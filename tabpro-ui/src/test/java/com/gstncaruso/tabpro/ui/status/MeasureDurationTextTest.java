package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeasureDurationTextTest {

    @Test
    void aLoneQuarterRestIsOneOfFour() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());

        assertEquals("1/4", MeasureDurationText.of(measure));
    }

    @Test
    void fourQuartersMatchFourFourExactly() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));

        assertEquals("4/4", MeasureDurationText.of(measure));
    }

    @Test
    void fiveQuartersOverflowFourFour() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));

        assertEquals("5/4", MeasureDurationText.of(measure));
    }

    @Test
    void aFractionOfABeatShowsUpAsADecimal() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.EIGHTH))));

        assertEquals("1.5/4", MeasureDurationText.of(measure));
    }

    @Test
    void aThreeEightMeasureIsMeasuredInEighths() {
        Measure measure = new Measure(new TimeSignature(3, 8), List.of(
                Beat.rest(Duration.of(NoteValue.EIGHTH)),
                Beat.rest(Duration.of(NoteValue.EIGHTH))));

        assertEquals("2/3", MeasureDurationText.of(measure));
    }
}

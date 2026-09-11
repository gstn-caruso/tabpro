package com.gstncaruso.tabpro.ui.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MeasureCompletenessTest {

    @Test
    void everyCaseHasAReadableLabelInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("complete", english.text("views.MeasureCompleteness.COMPLETE"));
        assertEquals("short", english.text("views.MeasureCompleteness.TOO_SHORT"));
        assertEquals("long", english.text("views.MeasureCompleteness.TOO_LONG"));
    }

    @Test
    void aFreshMeasureWithOnlyAQuarterRestIsTooShortForFourFour() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());

        assertEquals(MeasureCompleteness.TOO_SHORT, MeasureCompleteness.of(measure));
    }

    @Test
    void fourQuartersFillFourFourExactly() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));

        assertEquals(MeasureCompleteness.COMPLETE, MeasureCompleteness.of(measure));
    }

    @Test
    void fiveQuartersOverflowFourFour() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));

        assertEquals(MeasureCompleteness.TOO_LONG, MeasureCompleteness.of(measure));
    }

    @Test
    void everyCaseHasAReadableLabel() {
        assertEquals("completo", MeasureCompleteness.COMPLETE.label());
        assertEquals("corto", MeasureCompleteness.TOO_SHORT.label());
        assertEquals("largo", MeasureCompleteness.TOO_LONG.label());
    }
}

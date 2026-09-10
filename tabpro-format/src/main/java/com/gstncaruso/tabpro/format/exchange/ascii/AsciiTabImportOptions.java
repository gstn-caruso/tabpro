package com.gstncaruso.tabpro.format.exchange.ascii;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;

public record AsciiTabImportOptions(TimeSignature defaultTimeSignature, RhythmStrategy rhythm) {

    public static AsciiTabImportOptions standard() {
        return new AsciiTabImportOptions(TimeSignature.fourFour(), RhythmStrategy.fixed(Duration.of(NoteValue.EIGHTH)));
    }

    public AsciiTabImportOptions withRhythm(RhythmStrategy rhythm) {
        return new AsciiTabImportOptions(defaultTimeSignature, rhythm);
    }

    public AsciiTabImportOptions withDefaultTimeSignature(TimeSignature defaultTimeSignature) {
        return new AsciiTabImportOptions(defaultTimeSignature, rhythm);
    }
}

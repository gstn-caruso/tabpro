package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.model.TimeSignature;

public record TimeSignatureDto(int beats, int beatUnit) {

    public static TimeSignatureDto from(TimeSignature timeSignature) {
        return new TimeSignatureDto(timeSignature.beats(), timeSignature.beatUnit());
    }

    public TimeSignature toTimeSignature() {
        return new TimeSignature(beats, beatUnit);
    }
}

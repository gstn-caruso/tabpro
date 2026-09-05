package com.gstncaruso.tabpro.format;

import java.util.List;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;

public record MeasureDto(TimeSignatureDto timeSignature, List<BeatDto> beats) {

    public static MeasureDto from(Measure measure) {
        List<BeatDto> beats = measure.beats().stream().map(BeatDto::from).toList();
        return new MeasureDto(TimeSignatureDto.from(measure.timeSignature()), beats);
    }

    public Measure toMeasure() {
        List<Beat> domainBeats = beats.stream().map(BeatDto::toBeat).toList();
        return new Measure(timeSignature.toTimeSignature(), domainBeats);
    }
}

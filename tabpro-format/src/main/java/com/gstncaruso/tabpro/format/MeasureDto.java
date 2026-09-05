package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.ScoreColor;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.List;
import java.util.Optional;

public record MeasureDto(
        TimeSignatureDto timeSignature,
        List<BeatDto> beats,
        List<BeatDto> bassVoice,
        AttributesDto attributes) {

    public static MeasureDto from(Measure measure) {
        List<BeatDto> lead = measure.lead().beats().stream().map(BeatDto::from).toList();
        Voice bass = measure.voice(com.gstncaruso.tabpro.core.model.VoicePart.BASS);
        return new MeasureDto(
                TimeSignatureDto.from(measure.timeSignature()),
                lead,
                bass.isUnused() ? null : bass.beats().stream().map(BeatDto::from).toList(),
                measure.attributes().isPlain() ? null : AttributesDto.from(measure.attributes()));
    }

    public Measure toMeasure() {
        if (beats == null) {
            throw new ScoreFileException("falta el campo beats");
        }
        List<Beat> lead = beats.stream().map(BeatDto::toBeat).toList();
        Voice bass = bassVoice == null
                ? Voice.unused()
                : new Voice(bassVoice.stream().map(BeatDto::toBeat).toList());
        MeasureAttributes read = attributes == null ? MeasureAttributes.plain() : attributes.toAttributes();
        return new Measure(timeSignature.toTimeSignature(), read, List.of(new Voice(lead), bass));
    }

    public record AttributesDto(
            Integer keyAccidentals,
            String keyMode,
            String tripletFeel,
            Boolean doubleBar,
            Boolean repeatOpen,
            Integer repeatCount,
            List<Integer> alternateEndings,
            String symbol,
            String jump,
            String markerName,
            Integer markerColor,
            String lineBreak) {

        public static AttributesDto from(MeasureAttributes attributes) {
            KeySignature key = attributes.keySignature();
            return new AttributesDto(
                    key.accidentals() == 0 && key.mode() == Mode.MAJOR ? null : key.accidentals(),
                    key.mode() == Mode.MAJOR ? null : key.mode().name(),
                    attributes.tripletFeel() == TripletFeel.NONE ? null : attributes.tripletFeel().name(),
                    attributes.doubleBar() ? Boolean.TRUE : null,
                    attributes.repeatOpen() ? Boolean.TRUE : null,
                    attributes.repeatCount() == 0 ? null : attributes.repeatCount(),
                    attributes.alternateEndings().isEmpty() ? null : attributes.alternateEndings(),
                    attributes.symbol().map(Enum::name).orElse(null),
                    attributes.jump().map(Enum::name).orElse(null),
                    attributes.marker().map(Marker::name).orElse(null),
                    attributes.marker().map(marker -> marker.color().packed()).orElse(null),
                    attributes.lineBreak() == LineBreak.AUTOMATIC ? null : attributes.lineBreak().name());
        }

        public MeasureAttributes toAttributes() {
            return new MeasureAttributes(
                    new KeySignature(
                            keyAccidentals == null ? 0 : keyAccidentals,
                            Enums.read(Mode.class, keyMode, Mode.MAJOR)),
                    Enums.read(TripletFeel.class, tripletFeel, TripletFeel.NONE),
                    isSet(doubleBar),
                    isSet(repeatOpen),
                    repeatCount == null ? 0 : repeatCount,
                    alternateEndings == null ? List.of() : alternateEndings,
                    Optional.ofNullable(Enums.read(DirectionSymbol.class, symbol, null)),
                    Optional.ofNullable(Enums.read(DirectionJump.class, jump, null)),
                    toMarker(),
                    Enums.read(LineBreak.class, lineBreak, LineBreak.AUTOMATIC));
        }

        private Optional<Marker> toMarker() {
            if (markerName == null || markerName.isBlank()) {
                return Optional.empty();
            }
            ScoreColor color = markerColor == null ? Marker.DEFAULT_COLOR : ScoreColor.rgb(markerColor);
            return Optional.of(new Marker(markerName, color));
        }

        private static boolean isSet(Boolean value) {
            return value != null && value;
        }
    }
}

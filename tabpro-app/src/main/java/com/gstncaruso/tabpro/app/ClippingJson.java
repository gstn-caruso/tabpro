package com.gstncaruso.tabpro.app;

import com.google.gson.Gson;
import com.gstncaruso.tabpro.core.editing.Clipboard.Clipping;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.format.BeatDto;
import com.gstncaruso.tabpro.format.MeasureDto;
import java.util.List;

final class ClippingJson {

    private static final String KIND = "tabpro-clipping";
    private static final int CURRENT_FORMAT = 1;

    private final Gson gson = new Gson();

    String encode(Clipping clipping) {
        return gson.toJson(Envelope.from(clipping));
    }

    Clipping decode(String text) {
        try {
            Envelope envelope = gson.fromJson(text, Envelope.class);
            if (envelope == null || !KIND.equals(envelope.kind) || envelope.format != CURRENT_FORMAT) {
                return Clipping.EMPTY;
            }
            return envelope.toClipping();
        } catch (RuntimeException notAValidClipping) {
            return Clipping.EMPTY;
        }
    }

    private record Envelope(
            String kind, int format, List<List<MeasureDto>> measuresByTrack, List<BeatDto> beats, int stringCount) {

        static Envelope from(Clipping clipping) {
            return new Envelope(
                    KIND,
                    CURRENT_FORMAT,
                    clipping.measuresByTrack().stream()
                            .map(measures -> measures.stream().map(MeasureDto::from).toList())
                            .toList(),
                    clipping.beats().stream().map(BeatDto::from).toList(),
                    clipping.stringCount());
        }

        Clipping toClipping() {
            List<Beat> readBeats = beats == null ? List.of() : beats.stream().map(BeatDto::toBeat).toList();
            if (!readBeats.isEmpty()) {
                return Clipping.ofBeats(readBeats, stringCount);
            }
            List<List<Measure>> readMeasures = measuresByTrack == null
                    ? List.of()
                    : measuresByTrack.stream()
                            .map(measures -> measures.stream().map(MeasureDto::toMeasure).toList())
                            .toList();
            return Clipping.ofMeasures(readMeasures, stringCount);
        }
    }
}

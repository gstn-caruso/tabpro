package com.gstncaruso.tabpro.app;

import com.google.gson.Gson;
import com.gstncaruso.tabpro.core.editing.Clipboard.Clipping;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.format.BeatDto;
import com.gstncaruso.tabpro.format.MeasureDto;
import java.util.List;

/**
 * El Clipping en JSON, para que pueda cruzar el portapapeles del sistema operativo -que
 * solo entiende texto-. Reusa MeasureDto y BeatDto, los mismos con los que tabpro-format
 * ya lee y escribe compases y beats en los archivos .json de tabpro; no hace falta un
 * formato nuevo.
 *
 * <p>Lo que decodifica no es necesariamente un clipping de tabpro: puede ser texto suelto
 * copiado de cualquier otro lado, JSON de otra cosa, o de una version de este formato que
 * esta clase todavia no conoce. En todos esos casos {@link #decode} devuelve
 * {@link Clipping#EMPTY} en vez de romper -pegar un portapapeles vacio ya es, en
 * {@code Editor.paste}, una operacion que no hace nada.
 */
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
        } catch (RuntimeException e) {
            // El texto vino de afuera del proceso (portapapeles del sistema operativo): una
            // violacion de invariantes de dominio en un compas corrupto es tan "no es un
            // clipping valido" como un JSON con otra forma. En ambos casos, portapapeles vacio.
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

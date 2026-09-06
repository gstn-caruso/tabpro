package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Clipboard;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * El portapapeles del sistema operativo solo entiende texto, asi que lo que se copia
 * tiene que cruzarlo como JSON -reusando MeasureDto/BeatDto, que tabpro-format ya usa
 * para leer y escribir compases y beats en los archivos .json de tabpro-. Estos tests
 * no tocan el portapapeles real (eso rompe en headless); prueban el codec solo.
 */
class ClippingJsonTest {

    private final ClippingJson json = new ClippingJson();

    @Test
    void aClippingSurvivesTheRoundTripToJsonAndBack() {
        Editor source = new Editor(Score.blank());
        source.setFret(5);
        source.copy(false);

        Clipboard.Clipping roundTripped = json.decode(json.encode(source.clipboard().content()));

        Editor target = new Editor(Score.blank());
        target.clipboard().hold(roundTripped);
        target.paste(PasteOptions.replacingOnce());

        assertEquals(
                Optional.of(5), target.score().track(0).measure(0).beat(0).noteOn(1).map(note -> note.fret()));
    }

    @Test
    void plainTextIsNotAClipping() {
        assertTrue(json.decode("un texto cualquiera copiado de otro lado, no es json").isEmpty());
    }

    @Test
    void jsonThatIsNotShapedLikeAClippingIsNotAClipping() {
        assertTrue(json.decode("{\"cualquierCosa\":1}").isEmpty());
    }

    @Test
    void aDifferentFormatVersionIsNotAClipping() {
        String otroFormato =
                "{\"kind\":\"tabpro-clipping\",\"format\":999,\"measuresByTrack\":[],\"beats\":[],\"stringCount\":6}";

        assertTrue(json.decode(otroFormato).isEmpty());
    }
}

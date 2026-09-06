package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChordSoundTest {

    private final RecordingPlayer player = new RecordingPlayer();

    @Test
    void tocaCadaCuerdaQueSuenaEnSuAltura() {
        ChordDiagram amAbierto = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        ChordSound.play(amAbierto, Tuning.standard(), player, 25);

        assertEquals(
                List.of(
                        new Pitch(64), // cuerda 1 al aire, Mi4
                        new Pitch(60), // cuerda 2 traste 1
                        new Pitch(57), // cuerda 3 traste 2
                        new Pitch(52), // cuerda 4 traste 2
                        new Pitch(45)), // cuerda 5 al aire
                player.sounded().stream().map(RecordingPlayer.Sounded::pitch).toList());
    }

    @Test
    void noTocaLasCuerdasMudas() {
        ChordDiagram amAbierto = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        ChordSound.play(amAbierto, Tuning.standard(), player, 25);

        assertEquals(5, player.sounded().size());
    }

    @Test
    void usaElInstrumentoIndicado() {
        ChordDiagram solo = ChordDiagram.named("E", List.of(0, -1, -1, -1, -1, -1));

        ChordSound.play(solo, Tuning.standard(), player, 33);

        assertEquals(33, player.sounded().get(0).program());
    }
}

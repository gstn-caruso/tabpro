package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreExchange;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CombinedExchangeTest {

    private final List<String> notationCalls = new ArrayList<>();
    private final List<String> soundCalls = new ArrayList<>();
    private final ScoreExchange exchange = new CombinedExchange(notation(), sound());

    @Test
    void sendsTheSoundFormatsToTheSoundSide() {
        exchange.exportMidi(Score.blank(), Path.of("prueba.mid"));
        exchange.exportWave(Score.blank(), Path.of("prueba.wav"), AudioQuality.standard());

        assertEquals(List.of("exportMidi", "exportWave"), soundCalls);
        assertEquals(List.of(), notationCalls);
    }

    @Test
    void sendsTheNotationFormatsToTheNotationSide() {
        exchange.importMidi(Path.of("ajeno.mid"));
        exchange.importGuitarPro(Path.of("ajeno.gp5"));
        exchange.exportMusicXml(Score.blank(), Path.of("prueba.xml"));

        assertEquals(List.of("importMidi", "importGuitarPro", "exportMusicXml"), notationCalls);
        assertEquals(List.of(), soundCalls);
    }

    private ScoreExchange notation() {
        return new ScoreExchange() {
            @Override
            public Score importMidi(Path path) {
                notationCalls.add("importMidi");
                return Score.blank();
            }

            @Override
            public Score importGuitarPro(Path path) {
                notationCalls.add("importGuitarPro");
                return Score.blank();
            }

            @Override
            public void exportMusicXml(Score score, Path path) {
                notationCalls.add("exportMusicXml");
            }
        };
    }

    private ScoreExchange sound() {
        return new ScoreExchange() {
            @Override
            public void exportMidi(Score score, Path path) {
                soundCalls.add("exportMidi");
            }

            @Override
            public void exportWave(Score score, Path path, AudioQuality quality) {
                soundCalls.add("exportWave");
            }
        };
    }
}

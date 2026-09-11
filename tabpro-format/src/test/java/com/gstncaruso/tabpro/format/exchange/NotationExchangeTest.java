package com.gstncaruso.tabpro.format.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.files.ScoreOperation;
import com.gstncaruso.tabpro.format.TestDefaultNames;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotationExchangeTest {

    private final NotationExchange exchange = new NotationExchange(new TestDefaultNames());

    @Test
    void midiExportBelongsToTheSoundExchange() {
        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> exchange.exportMidi(null, null));

        assertEquals(ScoreFileProblem.NOT_SUPPORTED, failure.problem());
        assertEquals(List.of(ScoreOperation.EXPORT_MIDI), failure.arguments());
    }

    @Test
    void waveExportBelongsToTheSoundExchange() {
        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> exchange.exportWave(null, null, AudioQuality.standard()));

        assertEquals(ScoreFileProblem.NOT_SUPPORTED, failure.problem());
        assertEquals(List.of(ScoreOperation.EXPORT_WAVE), failure.arguments());
    }
}

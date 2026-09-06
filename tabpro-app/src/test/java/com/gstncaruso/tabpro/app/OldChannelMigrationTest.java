package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.format.JsonScoreFiles;
import com.gstncaruso.tabpro.midi.MidiScoreExporter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;

/**
 * El criterio de aceptacion real del arreglo de la mesa de mezcla: una partitura de varias
 * pistas guardada antes de que el canal configurado llegara a sonar tiene que seguir sonando
 * como esas mismas pistas, cada una con su instrumento -y en los mismos canales de siempre, no
 * en unos nuevos elegidos al azar- en vez de que las cuatro terminen pisandose en una sola.
 */
class OldChannelMigrationTest {

    private final JsonScoreFiles scoreFiles = new JsonScoreFiles();
    private final MidiScoreExporter exporter = new MidiScoreExporter();

    @Test
    void aScoreSavedBeforeChannelsWereHonoredSoundsExactlyAsItUsedTo() throws URISyntaxException {
        Path path = Path.of(getClass().getResource("/v4-three-tracks-all-on-channel-one.tabpro").toURI());
        Score score = scoreFiles.load(path);

        Sequence sequence = exporter.toSequence(score);

        ShortMessage first = firstProgramChangeOf(sequence, 1);
        ShortMessage second = firstProgramChangeOf(sequence, 2);
        ShortMessage third = firstProgramChangeOf(sequence, 3);

        // el reparto que hacia MidiSequences antes de este cambio daba exactamente estos canales
        assertEquals(0, first.getChannel(), "canal 1 del modelo, indice 0 en MIDI");
        assertEquals(2, second.getChannel(), "canal 3 del modelo, indice 2 en MIDI");
        assertEquals(4, third.getChannel(), "canal 5 del modelo, indice 4 en MIDI");
        assertEquals(25, first.getData1(), "la primera guitarra");
        assertEquals(33, second.getData1(), "el bajo");
        assertEquals(30, third.getData1(), "la segunda guitarra");
    }

    private ShortMessage firstProgramChangeOf(Sequence sequence, int trackIndex) {
        return (ShortMessage) sequence.getTracks()[trackIndex].get(0).getMessage();
    }
}

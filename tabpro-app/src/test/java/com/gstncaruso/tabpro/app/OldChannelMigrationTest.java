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

        assertEquals(0, first.getChannel(), "channel 1 in the model, index 0 in MIDI");
        assertEquals(2, second.getChannel(), "channel 3 in the model, index 2 in MIDI");
        assertEquals(4, third.getChannel(), "channel 5 in the model, index 4 in MIDI");
        assertEquals(25, first.getData1(), "the first guitar");
        assertEquals(33, second.getData1(), "the bass");
        assertEquals(30, third.getData1(), "the second guitar");
    }

    private ShortMessage firstProgramChangeOf(Sequence sequence, int trackIndex) {
        return (ShortMessage) sequence.getTracks()[trackIndex].get(0).getMessage();
    }
}

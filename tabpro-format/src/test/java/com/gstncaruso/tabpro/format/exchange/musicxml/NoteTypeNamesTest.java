package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.files.ScoreFeature;
import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class NoteTypeNamesTest {

    @Test
    void namesEveryFigureAsMusicXmlExpectsIt() {
        assertEquals("whole", NoteTypeNames.toXml(NoteValue.WHOLE));
        assertEquals("half", NoteTypeNames.toXml(NoteValue.HALF));
        assertEquals("quarter", NoteTypeNames.toXml(NoteValue.QUARTER));
        assertEquals("eighth", NoteTypeNames.toXml(NoteValue.EIGHTH));
        assertEquals("16th", NoteTypeNames.toXml(NoteValue.SIXTEENTH));
        assertEquals("32nd", NoteTypeNames.toXml(NoteValue.THIRTY_SECOND));
        assertEquals("64th", NoteTypeNames.toXml(NoteValue.SIXTY_FOURTH));
    }

    @Test
    void aNoteValueTabproCannotRepresentIsContentThatIsNotSupported() {
        ScoreFileException failure = assertThrows(ScoreFileException.class, () -> NoteTypeNames.fromXml("breve"));

        assertEquals(ScoreFileProblem.UNSUPPORTED_CONTENT, failure.problem());
        assertEquals(List.of(ScoreFeature.EXTREME_NOTE_VALUES), failure.arguments());
    }

    @Test
    void readsEveryNameBack() {
        for (NoteValue value : NoteValue.values()) {
            assertEquals(value, NoteTypeNames.fromXml(NoteTypeNames.toXml(value)));
        }
    }
}

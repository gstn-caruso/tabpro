package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * El fixture es {@code guitars.ptb}, del propio repositorio de powertabeditor:
 * dos guitarras en la score de guitarra (7 y 6 cuerdas) y una en la score de
 * bajo (4 cuerdas).
 */
class PowerTabScoreReaderTest {

    private final PowerTabHeaderReader headerReader = new PowerTabHeaderReader();
    private final PowerTabScoreReader scoreReader = new PowerTabScoreReader();

    @Test
    void readsTheGuitarsFromBothScores() {
        PowerTabByteReader reader = new PowerTabByteReader(PowerTabHeaderReaderTest.bytesOf("guitars"));
        headerReader.read(reader);

        PowerTabScore guitarScore = scoreReader.read(reader);
        PowerTabScore bassScore = scoreReader.read(reader);

        assertEquals(2, guitarScore.guitars().size());
        assertEquals("First Player", guitarScore.guitars().get(0).description());
        assertEquals(7, guitarScore.guitars().get(1).tuningMidiNotes().size());

        assertEquals(1, bassScore.guitars().size());
        assertEquals(4, bassScore.guitars().get(0).tuningMidiNotes().size());
    }
}

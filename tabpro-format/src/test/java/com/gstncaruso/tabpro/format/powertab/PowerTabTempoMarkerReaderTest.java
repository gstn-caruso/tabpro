package com.gstncaruso.tabpro.format.powertab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El fixture es {@code tempo_markers.ptb}, del propio repositorio de
 * powertabeditor: un marcador estandar en la posicion 3 a negra = 99.
 */
class PowerTabTempoMarkerReaderTest {

    @Test
    void readsTheStandardMarkerFromTheRealFixture() {
        PowerTabByteReader reader = new PowerTabByteReader(PowerTabHeaderReaderTest.bytesOf("tempo_markers"));
        new PowerTabHeaderReader().read(reader);

        PowerTabScore score = new PowerTabScoreReader().read(reader);

        List<PowerTabTempoMarker> tempoMarkers = score.tempoMarkers();
        assertEquals(1, tempoMarkers.size());
        PowerTabTempoMarker tempo = tempoMarkers.get(0);
        assertEquals(0, tempo.system());
        assertEquals(3, tempo.position());
        assertTrue(tempo.isStandardMarker());
        assertEquals(99, tempo.beatsPerMinute());
    }
}

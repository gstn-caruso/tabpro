package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Reads a "score" (guitar or bass): its guitars, and then the chord diagrams and the
 * floating text, which are discarded; the guitar-to-staff assignments, which are kept
 * for later validation; the tempo markers (of which only the first standard one
 * matters: tabpro does not support tempo changes partway through a score); the
 * dynamics, which are discarded; the alternate endings, and lastly the systems.
 */
final class PowerTabScoreReader {

    private final PowerTabGuitarReader guitarReader = new PowerTabGuitarReader();
    private final PowerTabGuitarInReader guitarInReader = new PowerTabGuitarInReader();
    private final PowerTabTempoMarkerReader tempoMarkerReader = new PowerTabTempoMarkerReader();
    private final PowerTabAlternateEndingReader endingReader = new PowerTabAlternateEndingReader();
    private final PowerTabSystemReader systemReader = new PowerTabSystemReader();

    PowerTabScore read(PowerTabByteReader reader) {
        List<PowerTabGuitar> guitars = reader.readVector(guitarReader::read);
        reader.skipVector(PowerTabAuxiliaryReader::skipChordDiagram);
        reader.skipVector(PowerTabAuxiliaryReader::skipFloatingText);
        List<PowerTabGuitarIn> guitarIns = reader.readVector(guitarInReader::read);
        List<PowerTabTempoMarker> tempoMarkers = reader.readVector(tempoMarkerReader::read);
        reader.skipVector(PowerTabAuxiliaryReader::skipDynamic);
        List<PowerTabAlternateEnding> alternateEndings = reader.readVector(endingReader::read);
        List<PowerTabSystem> systems = reader.readVector(systemReader::read);

        return new PowerTabScore(guitars, guitarIns, tempoMarkers, alternateEndings, systems);
    }
}

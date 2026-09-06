package com.gstncaruso.tabpro.format.powertab;

import java.util.List;

/**
 * Lee una "score" (la de guitarra o la de bajo): sus guitarras, y despues los
 * diagramas de acorde y el texto flotante, que se descartan; las asignaciones
 * de guitarra a pentagrama, que se guardan para validar mas adelante; los
 * marcadores de tempo y las dinamicas, que se descartan; los finales
 * alternativos, y por ultimo los sistemas.
 */
final class PowerTabScoreReader {

    private final PowerTabGuitarReader guitarReader = new PowerTabGuitarReader();
    private final PowerTabGuitarInReader guitarInReader = new PowerTabGuitarInReader();
    private final PowerTabAlternateEndingReader endingReader = new PowerTabAlternateEndingReader();
    private final PowerTabSystemReader systemReader = new PowerTabSystemReader();

    PowerTabScore read(PowerTabByteReader reader) {
        List<PowerTabGuitar> guitars = reader.readVector(guitarReader::read);
        reader.skipVector(PowerTabAuxiliaryReader::skipChordDiagram);
        reader.skipVector(PowerTabAuxiliaryReader::skipFloatingText);
        List<PowerTabGuitarIn> guitarIns = reader.readVector(guitarInReader::read);
        reader.skipVector(PowerTabAuxiliaryReader::skipTempoMarker);
        reader.skipVector(PowerTabAuxiliaryReader::skipDynamic);
        List<PowerTabAlternateEnding> alternateEndings = reader.readVector(endingReader::read);
        List<PowerTabSystem> systems = reader.readVector(systemReader::read);

        return new PowerTabScore(guitars, guitarIns, alternateEndings, systems);
    }
}

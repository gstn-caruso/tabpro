package com.gstncaruso.tabpro.format.exchange.midi;

import java.util.List;
import java.util.Optional;

/** El contenido de un archivo MIDI ya interpretado: su tempo, sus compases y sus pistas. */
record ParsedMidiFile(int tempoBpm, Optional<String> title, MeasureGrid grid, List<RawMidiTrack> tracks) {
}

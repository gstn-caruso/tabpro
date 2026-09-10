package com.gstncaruso.tabpro.format.exchange.midi;

import java.util.List;
import java.util.Optional;

record ParsedMidiFile(int tempoBpm, Optional<String> title, MeasureGrid grid, List<RawMidiTrack> tracks) {
}

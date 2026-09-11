package com.gstncaruso.tabpro.core.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ScoreExchangeTest {

    private static final ScoreExchange NONE = ScoreExchange.NONE;

    static Stream<Arguments> everyOperationOfTheExchangeWithoutFormats() {
        return Stream.of(
                operation(ScoreOperation.IMPORT_MIDI, () -> NONE.importMidi(null)),
                operation(ScoreOperation.EXPORT_MIDI, () -> NONE.exportMidi(null, null)),
                operation(ScoreOperation.IMPORT_MIDI, () -> NONE.midiTracksIn(null)),
                operation(ScoreOperation.IMPORT_MIDI,
                        () -> NONE.importMidiQuick(null, List.of(), false, Optional.empty(), Optional.empty(), false)),
                operation(ScoreOperation.IMPORT_MIDI,
                        () -> NONE.importMidiInto(null, null, List.of(), false, Optional.empty(), Optional.empty())),
                operation(ScoreOperation.IMPORT_MIDI, () -> NONE.importMidiTitleAndTimeSignatures(null, null)),
                operation(ScoreOperation.EXPORT_WAVE, () -> NONE.exportWave(null, null, AudioQuality.standard())),
                operation(ScoreOperation.IMPORT_MIDI, () -> NONE.midiTrackTimeline(null, List.of())),
                operation(ScoreOperation.IMPORT_ASCII, () -> NONE.importAscii(null)),
                operation(ScoreOperation.EXPORT_ASCII, () -> NONE.exportAscii(null, null)),
                operation(ScoreOperation.IMPORT_ASCII, () -> NONE.importAsciiInto(null, "", Optional.empty(), 4)),
                operation(ScoreOperation.EXPORT_ASCII, () -> NONE.previewAscii(null, 80)),
                operation(ScoreOperation.EXPORT_ASCII, () -> NONE.exportAscii(null, null, 80)),
                operation(ScoreOperation.IMPORT_MUSIC_XML, () -> NONE.importMusicXml(null)),
                operation(ScoreOperation.EXPORT_MUSIC_XML, () -> NONE.exportMusicXml(null, null)),
                operation(ScoreOperation.OPEN_GUITAR_PRO, () -> NONE.importGuitarPro(null)),
                operation(ScoreOperation.OPEN_TAB_EDIT, () -> NONE.importTabEdit(null)),
                operation(ScoreOperation.EXPORT_GUITAR_PRO, () -> NONE.exportGuitarPro(null, null)),
                operation(ScoreOperation.EXPORT_GUITAR_PRO, () -> NONE.guitarProExportWarnings(null)),
                operation(ScoreOperation.IMPORT_POWER_TAB, () -> NONE.importPowerTab(null)));
    }

    @ParameterizedTest
    @MethodSource
    void everyOperationOfTheExchangeWithoutFormats(ScoreOperation operation, Executable call) {
        ScoreFileException failure = assertThrows(ScoreFileException.class, call);

        assertEquals(ScoreFileProblem.NOT_SUPPORTED, failure.problem());
        assertEquals(List.of(operation), failure.arguments());
    }

    private static Arguments operation(ScoreOperation operation, Executable call) {
        return Arguments.of(operation, call);
    }
}

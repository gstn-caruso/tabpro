package com.gstncaruso.tabpro.format;

import static java.util.Map.entry;

import com.gstncaruso.tabpro.core.model.TuningName;
import java.util.Map;
import java.util.stream.Collectors;

final class StoredTuningName {

    private static final String CUSTOM = "Personalizada";

    private static final Map<String, String> STORED_NAMES_BY_LIBRARY_ID = Map.ofEntries(
            entry("guitar.standard", "Guitarra estándar"),
            entry("guitar.dropD", "Drop D"),
            entry("guitar.halfStepDown", "Medio tono abajo"),
            entry("guitar.wholeStepDown", "Un tono abajo"),
            entry("guitar.dropC", "Drop C"),
            entry("guitar.openD", "Open D"),
            entry("guitar.openG", "Open G"),
            entry("guitar.openC", "Open C"),
            entry("guitar.openE", "Open E"),
            entry("guitar.openA", "Open A"),
            entry("guitar.dadgad", "DADGAD"),
            entry("guitar.newStandard", "Nuevo estándar"),
            entry("guitar.openCm", "Open Cm"),
            entry("guitar.openC6", "Open C6"),
            entry("guitar.openDm", "Open Dm"),
            entry("guitar.openD5", "Open D5"),
            entry("guitar.openDsus4", "Open Dsus4"),
            entry("guitar.openEm", "Open Em"),
            entry("guitar.openGm", "Open Gm"),
            entry("guitar.openG6", "Open G6"),
            entry("guitar.openGsus4", "Open Gsus4"),
            entry("guitar.openAm", "Open Am"),
            entry("guitar.openF", "Open F"),
            entry("guitar.nashville", "Nashville"),
            entry("guitar.sevenString", "Guitarra de 7 cuerdas"),
            entry("guitar.sevenStringDropA", "Guitarra de 7 cuerdas Drop A"),
            entry("bass.standard", "Bajo estándar"),
            entry("bass.dropD", "Bajo Drop D"),
            entry("bass.halfStepDown", "Bajo medio tono abajo"),
            entry("bass.wholeStepDown", "Bajo un tono abajo"),
            entry("bass.fiveString", "Bajo de 5 cuerdas"),
            entry("bass.sixString", "Bajo de 6 cuerdas"),
            entry("percussion", "Percusión"));

    private static final Map<String, String> LIBRARY_IDS_BY_STORED_NAME = STORED_NAMES_BY_LIBRARY_ID.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

    private StoredTuningName() {
    }

    static String of(TuningName name) {
        return switch (name) {
            case TuningName.Library(String id) -> STORED_NAMES_BY_LIBRARY_ID.get(id);
            case TuningName.UserNamed(String typed) -> typed;
            case TuningName.Custom() -> CUSTOM;
        };
    }

    static TuningName read(String stored) {
        if (stored.equals(CUSTOM)) {
            return new TuningName.Custom();
        }
        String libraryId = LIBRARY_IDS_BY_STORED_NAME.get(stored);
        return libraryId == null ? new TuningName.UserNamed(stored) : new TuningName.Library(libraryId);
    }
}

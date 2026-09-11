package com.gstncaruso.tabpro.format;

import static java.util.Map.entry;

import com.gstncaruso.tabpro.core.model.TuningName;
import java.util.Map;
import java.util.stream.Collectors;

final class StoredTuningName {

    private static final String CUSTOM = "Personalizada";

    private static final Map<String, String> STORED_NAMES_BY_LIBRARY_ID = Map.ofEntries(
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

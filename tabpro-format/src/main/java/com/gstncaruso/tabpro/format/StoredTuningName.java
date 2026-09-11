package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.model.TuningName;

final class StoredTuningName {

    private static final String CUSTOM = "Personalizada";

    private StoredTuningName() {
    }

    static String of(TuningName name) {
        return switch (name) {
            case TuningName.UserNamed(String typed) -> typed;
            case TuningName.Custom() -> CUSTOM;
        };
    }

    static TuningName read(String stored) {
        return stored.equals(CUSTOM) ? new TuningName.Custom() : new TuningName.UserNamed(stored);
    }
}

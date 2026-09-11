package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.actions.Ports.SoundBankStatus;
import com.gstncaruso.tabpro.ui.i18n.Texts;

public final class SoundBankStatusText {

    private SoundBankStatusText() {
    }

    public static String of(SoundBankStatus status) {
        return switch (status.kind()) {
            case NONE -> Texts.get("views.soundBank.none");
            case UNAVAILABLE -> Texts.get("views.soundBank.unavailable");
            case CHOSEN -> Texts.get("views.soundBank.chosen", status.fileName().orElseThrow());
            case DISABLED -> Texts.get("views.soundBank.disabled", status.fileName().orElseThrow());
            case PLAYING -> Texts.get("views.soundBank.playing", status.fileName().orElseThrow());
            case FAILED -> Texts.get("views.soundBank.failed", status.fileName().orElseThrow());
        };
    }
}

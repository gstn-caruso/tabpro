package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.VoicePart;

public final class StemDirection {

    private StemDirection() {
    }

    public static boolean pointsUp(VoicePart part, boolean usesTwoVoices, double averageStep, double middleLineStep) {
        if (usesTwoVoices) {
            return part == VoicePart.LEAD;
        }
        return averageStep < middleLineStep;
    }
}

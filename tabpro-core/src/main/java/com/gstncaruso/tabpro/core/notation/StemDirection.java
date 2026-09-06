package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.VoicePart;

/**
 * Hacia donde apunta la plica de un beat. Con una sola voz se mira el registro de sus notas;
 * con dos voces conviviendo en el mismo pentagrama la convencion es fija y no depende del
 * registro: la principal siempre para arriba, la de bajos siempre para abajo, para que no se
 * confundan entre si.
 */
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

package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import org.junit.jupiter.api.Test;

class KeySignaturePanelTest {

    @Test
    void startsWithTheGivenKey() {
        KeySignaturePanel panel = new KeySignaturePanel(new KeySignature(3, Mode.MAJOR));

        assertEquals(new KeySignature(3, Mode.MAJOR), panel.toKeySignature());
    }

    @Test
    void tellsMajorAndMinorApart() {
        KeySignaturePanel panel = new KeySignaturePanel(KeySignature.cMajor());

        panel.apply(new KeySignature(0, Mode.MINOR));

        assertEquals(new KeySignature(0, Mode.MINOR), panel.toKeySignature());
    }

    @Test
    void offersFlatsAndSharps() {
        KeySignaturePanel panel = new KeySignaturePanel(KeySignature.cMajor());

        panel.apply(new KeySignature(-7, Mode.MAJOR));
        assertEquals(-7, panel.toKeySignature().accidentals());

        panel.apply(new KeySignature(7, Mode.MAJOR));
        assertEquals(7, panel.toKeySignature().accidentals());
    }
}

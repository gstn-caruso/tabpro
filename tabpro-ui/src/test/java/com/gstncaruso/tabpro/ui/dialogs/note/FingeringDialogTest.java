package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FingeringDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(Optional.empty(), Optional.empty());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }

    @Test
    void porDefectoElFocoInicialQuedaEnElCampoDeManoIzquierda() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(Optional.empty(), Optional.empty());

        assertSame(fields.leftHand(), fields.initialFocus());
    }

    @Test
    void siSePideElFocoEnManoDerechaElCampoInicialEsElDeManoDerecha() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(
                Optional.empty(), Optional.empty(), FingeringDialog.Hand.RIGHT);

        assertSame(fields.rightHand(), fields.initialFocus());
    }
}

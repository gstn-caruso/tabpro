package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import org.junit.jupiter.api.Test;

class DynamicsDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        DynamicsDialog.Fields fields = DynamicsDialog.buildFields(Dynamic.defaultDynamic());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }

    @Test
    void elComboDeDinamicaMuestraElSimboloMusicalEnVezDelEnumCrudo() {
        DynamicsDialog.Fields fields = DynamicsDialog.buildFields(Dynamic.defaultDynamic());

        String texto = Combos.renderedTextOf(fields.form(), Dynamic.class, Dynamic.MEZZO_FORTE);

        assertEquals("mf", texto);
    }
}

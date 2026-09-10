package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class TrackScopePanelTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new TrackScopePanel());
    }
}

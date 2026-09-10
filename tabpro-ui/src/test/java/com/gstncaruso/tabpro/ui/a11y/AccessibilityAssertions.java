package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.util.List;

/** Assertion compartida por los tests de "raíz de la UI sin violaciones de accesibilidad". */
public final class AccessibilityAssertions {

    private AccessibilityAssertions() {
    }

    public static void assertNoViolations(Container root) {
        List<Violation> violations = new AccessibilityWalker().walk(root);
        assertTrue(violations.isEmpty(), violations.toString());
    }
}

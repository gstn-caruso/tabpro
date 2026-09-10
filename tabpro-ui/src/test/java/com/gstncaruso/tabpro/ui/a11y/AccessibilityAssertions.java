package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

public final class AccessibilityAssertions {

    private AccessibilityAssertions() {
    }

    public static void assertNoViolations(Container root) {
        List<Violation> violations = new ArrayList<>(new AccessibilityWalker().walk(root));
        violations.addAll(mnemonicClashesOf(root));
        assertTrue(violations.isEmpty(), violations.toString());
    }

    private static List<Violation> mnemonicClashesOf(Container root) {
        return new MnemonicWalker().walkForm(root).stream()
                .filter(violation -> violation.reason().equals("duplicate mnemonic"))
                .toList();
    }
}

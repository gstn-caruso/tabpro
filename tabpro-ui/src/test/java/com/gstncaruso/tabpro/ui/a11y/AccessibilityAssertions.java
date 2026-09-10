package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

/** Assertion compartida por los tests de "raíz de la UI sin violaciones de accesibilidad". */
public final class AccessibilityAssertions {

    private AccessibilityAssertions() {
    }

    public static void assertNoViolations(Container root) {
        List<Violation> violations = new ArrayList<>(new AccessibilityWalker().walk(root));
        violations.addAll(mnemonicClashesOf(root));
        assertTrue(violations.isEmpty(), violations.toString());
    }

    /**
     * Solo los choques de mnemonico: un campo sin mnemonico todavia no esta cableado al
     * asignador (FormPanel lo hace; otros formularios manuales, por ahora, no) y no es lo que
     * este assert compartido esta verificando.
     */
    private static List<Violation> mnemonicClashesOf(Container root) {
        return new MnemonicWalker().walkForm(root).stream()
                .filter(violation -> violation.reason().equals("mnemónico repetido"))
                .toList();
    }
}

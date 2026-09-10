package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class AccessibilityAssertionsTest {

    @Test
    void dosEtiquetasDeFormularioConElMismoMnemonicoHacenFallarElAssert() {
        JPanel form = new JPanel();
        JTextField uno = new JTextField();
        JTextField otro = new JTextField();
        JLabel primera = new JLabel("Nombre");
        primera.setLabelFor(uno);
        primera.setDisplayedMnemonic('N');
        JLabel segunda = new JLabel("Número");
        segunda.setLabelFor(otro);
        segunda.setDisplayedMnemonic('N');
        form.add(primera);
        form.add(uno);
        form.add(segunda);
        form.add(otro);

        assertThrows(AssertionFailedError.class, () -> AccessibilityAssertions.assertNoViolations(form));
    }
}

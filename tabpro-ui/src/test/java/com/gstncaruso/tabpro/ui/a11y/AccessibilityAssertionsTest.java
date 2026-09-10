package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class AccessibilityAssertionsTest {

    @Test
    void twoFormLabelsWithTheSameMnemonicMakeTheAssertionFail() {
        JPanel form = new JPanel();
        JTextField firstField = new JTextField();
        JTextField secondField = new JTextField();
        JLabel firstLabel = new JLabel("Nombre");
        firstLabel.setLabelFor(firstField);
        firstLabel.setDisplayedMnemonic('N');
        JLabel secondLabel = new JLabel("Número");
        secondLabel.setLabelFor(secondField);
        secondLabel.setDisplayedMnemonic('N');
        form.add(firstLabel);
        form.add(firstField);
        form.add(secondLabel);
        form.add(secondField);

        assertThrows(AssertionFailedError.class, () -> AccessibilityAssertions.assertNoViolations(form));
    }
}

package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.awt.Container;
import java.util.Locale;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.junit.jupiter.api.Test;

class KeySignaturePanelTest {

    @Test
    void theKeySignatureFieldIsAvailableInEnglish() {
        assertEquals("Key Signature",
                Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.MeasurePropertiesDialog.keySignature"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new KeySignaturePanel(new KeySignature(0, Mode.MAJOR)));
    }

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

    @Test
    void showsEachKeyByItsTonicAndMode() {
        KeySignaturePanel panel = new KeySignaturePanel(new KeySignature(-7, Mode.MINOR));
        JComboBox<?> combo = comboIn(panel);

        Component rendered = rendererOf(combo)
                .getListCellRendererComponent(new JList<>(), combo.getSelectedItem(), 0, false, false);

        assertEquals("La b (Menor)", ((JLabel) rendered).getText());
    }

    private static JComboBox<?> comboIn(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JComboBox<?> combo) {
                return combo;
            }
        }
        throw new AssertionError("the panel has no combo");
    }

    @SuppressWarnings("unchecked")
    private static ListCellRenderer<Object> rendererOf(JComboBox<?> combo) {
        return (ListCellRenderer<Object>) combo.getRenderer();
    }
}

package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class MidiSetupDialogTest {

    private static final int LAPTOP_SCREEN_HEIGHT = 800;

    @Test
    void theFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("MIDI Setup", english.text("views.MidiSetupDialog.title"));
        assertEquals("Device", english.text("views.MidiSetupDialog.device"));
        assertEquals("Port 1", english.text("views.MidiSetupDialog.port", 1));
        assertEquals("JDK Internal Synthesizer", english.text("views.MidiSetupDialog.internalSynthesizer"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(panelWithFourEmptyPorts());
    }

    @Test
    void theFormsHeightFitsInATypicalLaptopWithoutScrolling() {
        JPanel panel = panelWithFourEmptyPorts();

        assertTrue(panel.getPreferredSize().height <= LAPTOP_SCREEN_HEIGHT,
                "the form (" + panel.getPreferredSize().height + "px) has to fit within "
                        + LAPTOP_SCREEN_HEIGHT + "px without scrolling");
    }

    private static JPanel panelWithFourEmptyPorts() {
        MidiSetupDialog.Setup current = new MidiSetupDialog.Setup(
                "", false,
                List.of(
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false)),
                "", 20, StringAssignment.NO_CHANNEL_DETECTION);

        return MidiSetupDialog.buildPanel(Ports.Devices.NONE, current).panel();
    }
}

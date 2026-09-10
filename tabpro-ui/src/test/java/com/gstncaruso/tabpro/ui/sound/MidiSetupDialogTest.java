package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class MidiSetupDialogTest {

    private static final int LAPTOP_SCREEN_HEIGHT = 800;

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(panelWithFourEmptyPorts());
    }

    @Test
    void theFormsHeightFitsInATypicalLaptopWithoutScrolling() {
        JPanel panel = panelWithFourEmptyPorts();

        assertTrue(panel.getPreferredSize().height <= LAPTOP_SCREEN_HEIGHT,
                "el formulario (" + panel.getPreferredSize().height + "px) tiene que entrar en "
                        + LAPTOP_SCREEN_HEIGHT + "px sin scrollear");
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

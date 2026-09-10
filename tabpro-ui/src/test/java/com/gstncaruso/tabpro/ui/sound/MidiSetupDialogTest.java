package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.util.List;
import org.junit.jupiter.api.Test;

class MidiSetupDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        MidiSetupDialog.Setup current = new MidiSetupDialog.Setup(
                "", false,
                List.of(
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false),
                        new MidiSetupDialog.PortSetup("", "", false)),
                "", 20, StringAssignment.NO_CHANNEL_DETECTION);

        AccessibilityAssertions.assertNoViolations(MidiSetupDialog.buildPanel(Ports.Devices.NONE, current).panel());
    }
}

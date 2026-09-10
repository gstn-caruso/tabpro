package com.gstncaruso.tabpro.ui.dialogs.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.util.List;
import org.junit.jupiter.api.Test;

class BendPanelTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new BendPanel(Bend.of(BendType.BEND, 4)));
    }

    @Test
    void startsWithTheGivenBend() {
        Bend bend = Bend.of(BendType.BEND_RELEASE, 6);

        BendPanel panel = new BendPanel(bend);

        assertEquals(bend, panel.toBend());
    }

    @Test
    void clickingTheGridAddsAPoint() {
        BendPanel panel = new BendPanel(Bend.of(BendType.BEND, 4));

        panel.clickAt(20, 6);

        assertEquals(List.of(BendPoint.at(20, 6)), panel.toBend().points().stream()
                .filter(point -> point.position() == 20).toList());
    }

    @Test
    void rightClickingAddsVibratoToTheNearestPoint() {
        BendPanel panel = new BendPanel(Bend.of(BendType.BEND, 4));

        panel.rightClickAt(0);

        assertEquals(1, panel.toBend().points().getFirst().vibrato());
    }

    @Test
    void laAlturaMostradaEsLaMagnitudDeLaCurvaAunqueSoloBaje() {
        BendPanel panel = new BendPanel(Bend.of(BendType.DIVE, 8));

        assertEquals(8, panel.selectedHeight());
    }

    @Test
    void elTipoDisponibleSeRestringeALosQueSeLePasan() {
        BendPanel panel = new BendPanel(Bend.of(BendType.DIP, 4), BendType.tremoloBarTypes());

        assertEquals(BendType.tremoloBarTypes(), panel.availableTypes());
    }
}

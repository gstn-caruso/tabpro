package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class CircleOfFifthsTest {

    @Test
    void givenLargeFonts_whenLaidOutAtPreferredSize_thenButtonsAreContainedAndDoNotOverlap() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CircleOfFifths circle = new CircleOfFifths((tonic, scale) -> {});
            List<AbstractButton> buttons = new ArrayList<>();
            enlargeFonts(circle, buttons);

            circle.setSize(circle.getPreferredSize());
            layout(circle);

            assertEquals(24, buttons.size());
            Rectangle available = new Rectangle(circle.getSize());
            List<Executable> checks = new ArrayList<>();
            for (int index = 0; index < buttons.size(); index++) {
                AbstractButton button = buttons.get(index);
                Rectangle bounds = SwingUtilities.convertRectangle(button.getParent(), button.getBounds(), circle);
                String name = button.getAccessibleContext().getAccessibleName();
                checks.add(() -> assertTrue(available.contains(bounds),
                        name + " fuera del círculo: " + bounds + ", disponible: " + available));
                for (int otherIndex = index + 1; otherIndex < buttons.size(); otherIndex++) {
                    AbstractButton other = buttons.get(otherIndex);
                    Rectangle otherBounds = SwingUtilities.convertRectangle(
                            other.getParent(), other.getBounds(), circle);
                    checks.add(() -> assertFalse(bounds.intersects(otherBounds),
                            name + " " + bounds + " se solapa con "
                                    + other.getAccessibleContext().getAccessibleName() + " " + otherBounds));
                }
            }
            assertAll(checks);
        });
    }

    private static void enlargeFonts(Component component, List<AbstractButton> buttons) {
        component.setFont(component.getFont().deriveFont(24f));
        if (component instanceof AbstractButton button) {
            buttons.add(button);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                enlargeFonts(child, buttons);
            }
        }
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) {
                layout(nested);
            }
        }
    }
}

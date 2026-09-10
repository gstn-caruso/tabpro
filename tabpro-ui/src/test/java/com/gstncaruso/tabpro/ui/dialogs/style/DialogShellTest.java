package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.junit.jupiter.api.Test;

class DialogShellTest {

    @Test
    void contentThatAlreadyFitsTheAvailableHeightIsNotWrappedInAnyScroll() {
        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(300, 200));

        JComponent fitted = DialogShell.fitToAvailableHeight(content, 400);

        assertSame(content, fitted);
    }

    @Test
    void contentTallerThanAvailableIsWrappedInAScrollWithThatCap() {
        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(300, 5000));

        JComponent fitted = DialogShell.fitToAvailableHeight(content, 400);

        JScrollPane scroll = assertInstanceOf(JScrollPane.class, fitted);
        assertSame(content, scroll.getViewport().getView());
        assertTrue(scroll.getPreferredSize().height <= 400,
                "el alto preferido del scroll no puede superar el disponible");
    }

    @Test
    void theAvailableContentHeightSubtractsTheButtonBarAndTheWindowMargin() {
        int availableHeight = DialogShell.availableContentHeight(1024, 60);

        assertEquals(1024 - 60 - DialogShell.WINDOW_CHROME_HEIGHT, availableHeight);
    }
}

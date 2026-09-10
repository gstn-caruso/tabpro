package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispose;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButtonByActionName;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * MainFrame extends JFrame, so building a real one requires a non-headless toolkit (see
 * AuditSupport); that is why this is an audit test instead of an ordinary one.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ToolBarsIconContrastAuditTest {

    @Test
    void elIconoDeLaFilaDeDocumentoLeeContraSuFondoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JButton newButton = findButtonByActionName(frame.getContentPane(), "Nuevo");
            assertReadsAgainstItsRealBackground(newButton, "Nuevo", frame.getContentPane());
        } finally {
            dispose(frame);
        }
    }

    @Test
    void elIconoDeLaFilaDeEfectosLeeContraSuFondoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JButton deadNoteButton = findButtonByActionName(frame.getContentPane(), "Nota muerta");
            assertReadsAgainstItsRealBackground(deadNoteButton, "Nota muerta", frame.getContentPane());
        } finally {
            dispose(frame);
        }
    }

    @Test
    void laEtiquetaDeTempoLeeContraSuFondoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JLabel tempoLabel = findLabel(frame.getContentPane(), "Tempo ");
            assertReadsAgainstItsRealBackground(tempoLabel, "Tempo", frame.getContentPane());
        } finally {
            dispose(frame);
        }
    }

    private JLabel findLabel(Container root, String text) {
        for (Component child : root.getComponents()) {
            if (child instanceof JLabel label && text.equals(label.getText())) {
                return label;
            }
            if (child instanceof Container container) {
                JLabel found = findLabel(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void assertReadsAgainstItsRealBackground(JComponent component, String description, Container contentPane) {
        BufferedImage rendering = renderingOf(contentPane);
        Rectangle bounds = boundsWithin(component, contentPane);
        BufferedImage componentArea = rendering.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);

        Color background = new Color(componentArea.getRGB(0, 0));
        Color foreground = mostDifferentFrom(background, componentArea);
        double ratio = Contrast.ratio(foreground, background);

        assertTrue(ratio >= Contrast.GRAPHICAL_MINIMUM_RATIO,
                "\"" + description + "\" da " + String.format("%.2f", ratio)
                        + ":1 contra su fondo real, necesita >= " + Contrast.GRAPHICAL_MINIMUM_RATIO + ":1");
    }

    private Rectangle boundsWithin(JComponent component, Container ancestor) {
        Point origin = SwingUtilities.convertPoint(component, new Point(0, 0), ancestor);
        return new Rectangle(origin.x, origin.y, component.getWidth(), component.getHeight());
    }

    private BufferedImage renderingOf(Container root) {
        BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D canvas = image.createGraphics();
        root.printAll(canvas);
        canvas.dispose();
        return image;
    }

    private Color mostDifferentFrom(Color background, BufferedImage image) {
        Color best = background;
        int bestDistance = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color candidate = new Color(image.getRGB(x, y));
                int distance = distance(candidate, background);
                if (distance > bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private int distance(Color one, Color other) {
        return Math.abs(one.getRed() - other.getRed())
                + Math.abs(one.getGreen() - other.getGreen())
                + Math.abs(one.getBlue() - other.getBlue());
    }
}

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
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Guitar Pro 5, manual pagina 14: el icono de cada boton de una fila de herramientas tiene que
 * leerse contra el fondo real que tiene detras, no solo pintarse. MainFrame extiende JFrame, asi
 * que armarlo de verdad exige un toolkit no headless (ver AuditSupport); por eso esta auditoria,
 * en vez de un test comun.
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
            assertReadsAgainstItsRealBackground(newButton, frame.getContentPane());
        } finally {
            dispose(frame);
        }
    }

    private void assertReadsAgainstItsRealBackground(JButton button, Container contentPane) {
        BufferedImage rendering = renderingOf(contentPane);
        Rectangle bounds = boundsWithin(button, contentPane);
        BufferedImage buttonArea = rendering.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);

        Color background = new Color(buttonArea.getRGB(0, 0));
        Color icon = mostDifferentFrom(background, buttonArea);
        double ratio = Contrast.ratio(icon, background);

        assertTrue(ratio >= Contrast.GRAPHICAL_MINIMUM_RATIO,
                "el icono de \"" + button.getToolTipText() + "\" da " + String.format("%.2f", ratio)
                        + ":1 contra su fondo real, necesita >= " + Contrast.GRAPHICAL_MINIMUM_RATIO + ":1");
    }

    private Rectangle boundsWithin(JButton button, Container ancestor) {
        Point origin = SwingUtilities.convertPoint(button, new Point(0, 0), ancestor);
        return new Rectangle(origin.x, origin.y, button.getWidth(), button.getHeight());
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

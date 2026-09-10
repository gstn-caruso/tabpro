package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 numera cada compas cuando el numero entra en el ancho de la celda, y cada 5 cuando
 * no -como en la regla de la vista general de la mesa de mezcla-.
 */
class MeasureGridNumberRulerTest {

    private static final FontMetrics METRICS = metrics();

    @Test
    void numbersEveryMeasureWhenTheWidestOneFitsTheCell() {
        assertEquals(1, MeasureGrid.numberStep(9, METRICS));
    }

    @Test
    void numbersEveryFiveMeasuresWhenTheWidestOneDoesNotFit() {
        assertEquals(MeasureGrid.NUMBER_EVERY, MeasureGrid.numberStep(999, METRICS));
    }

    private static FontMetrics metrics() {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return probe.createGraphics().getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
    }
}

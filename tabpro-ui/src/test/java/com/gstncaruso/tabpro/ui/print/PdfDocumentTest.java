package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class PdfDocumentTest {

    @Test
    void aDocumentWithoutPagesIsNotWritten() {
        assertThrows(IOException.class, () -> new PdfDocument().writeTo(new ByteArrayOutputStream()));
    }

    @Test
    void startsWithTheHeaderAndEndsWithTheTrailer() throws IOException {
        String pdf = write(page(40, 60));

        assertTrue(pdf.startsWith("%PDF-1.4"), pdf.substring(0, 20));
        assertTrue(pdf.contains("startxref"));
        assertTrue(pdf.trim().endsWith("%%EOF"));
    }

    @Test
    void aPageIsOneImageOnAnA4Sheet() throws IOException {
        String pdf = write(page(40, 60));

        assertTrue(pdf.contains("/Type /Catalog"));
        assertTrue(pdf.contains("/Type /Pages /Count 1"));
        assertTrue(pdf.contains("/MediaBox [ 0 0 595.28 841.89 ]"));
        assertTrue(pdf.contains("/Subtype /Image"));
        assertTrue(pdf.contains("/Im0 Do"));
    }

    @Test
    void everyPageGetsItsOwnSheet() throws IOException {
        String pdf = write(page(40, 60), page(40, 60), page(40, 60));

        assertTrue(pdf.contains("/Type /Pages /Count 3"));
        assertEquals(3, countOf(pdf, "/Type /Page /Parent"));
    }

    @Test
    void theImageKeepsItsSizeAndItsColourSpace() throws IOException {
        String pdf = write(page(40, 60));

        assertTrue(pdf.contains("/Width 40 /Height 60"));
        assertTrue(pdf.contains("/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode"));
    }

    private static BufferedImage page(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, 0, width, height);
        graphics.dispose();
        return image;
    }

    private static String write(BufferedImage... pages) throws IOException {
        PdfDocument pdf = new PdfDocument();
        for (BufferedImage page : pages) {
            pdf.addPage(page);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pdf.writeTo(out);
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    private static int countOf(String text, String fragment) {
        int found = 0;
        int at = text.indexOf(fragment);
        while (at >= 0) {
            found++;
            at = text.indexOf(fragment, at + fragment.length());
        }
        return found;
    }
}

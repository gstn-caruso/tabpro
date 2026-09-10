package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
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

    @Test
    void writingAPageReadsItsPixelsByRowNotOneByOne() throws IOException {
        PixelAccessCountingImage image = new PixelAccessCountingImage(40, 60);

        write(image);

        assertEquals(0, image.singlePixelCalls(), "no puede llamar a getRGB(x, y) por cada pixel");
        assertEquals(image.getHeight(), image.bulkRowCalls(), "tiene que pedir los pixeles fila por fila");
    }

    @Test
    void theImageStreamDecodesToTheExactPixelsOfTheOriginalImage() throws IOException, DataFormatException {
        BufferedImage image = page(11, 7);
        PdfDocument pdf = new PdfDocument();
        pdf.addPage(image);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pdf.writeTo(out);

        byte[] decoded = inflate(imageStreamOf(out.toByteArray()));
        assertArrayEquals(rgbBytesOf(image), decoded, "el pdf tiene que decodificar a los mismos pixeles que la imagen");
    }

    @Test
    void compressesTheImageAtDeflaterBestSpeedToPrioritizeExportSpeedOverFileSize() throws IOException {
        BufferedImage image = page(64, 64);
        PdfDocument pdf = new PdfDocument();
        pdf.addPage(image);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pdf.writeTo(out);

        byte[] imageStream = imageStreamOf(out.toByteArray());
        assertArrayEquals(deflateAt(rgbBytesOf(image), Deflater.BEST_SPEED), imageStream,
                "el pdf tiene que comprimir con Deflater.BEST_SPEED, no con otro nivel");
    }

    @Test
    void theSheetIsAsBigAsThePaperItWasAskedFor() throws IOException {
        PdfDocument pdf = new PdfDocument(612, 792);
        pdf.addPage(page(40, 60));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pdf.writeTo(out);

        assertTrue(out.toString(StandardCharsets.ISO_8859_1).contains("/MediaBox [ 0 0 612.00 792.00 ]"));
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

    private static byte[] rgbBytesOf(BufferedImage image) {
        byte[] rgb = new byte[image.getWidth() * image.getHeight() * 3];
        int at = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                rgb[at++] = (byte) ((pixel >> 16) & 0xFF);
                rgb[at++] = (byte) ((pixel >> 8) & 0xFF);
                rgb[at++] = (byte) (pixel & 0xFF);
            }
        }
        return rgb;
    }

    private static byte[] deflateAt(byte[] data, int level) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed, new Deflater(level))) {
            deflater.write(data);
        }
        return compressed.toByteArray();
    }

    private static byte[] imageStreamOf(byte[] pdfBytes) {
        String pdf = new String(pdfBytes, StandardCharsets.ISO_8859_1);
        int imageObjectAt = pdf.indexOf("/Subtype /Image");
        int streamAt = pdf.indexOf("stream\n", imageObjectAt) + "stream\n".length();
        int endStreamAt = pdf.indexOf("\nendstream", streamAt);
        byte[] stream = new byte[endStreamAt - streamAt];
        System.arraycopy(pdfBytes, streamAt, stream, 0, stream.length);
        return stream;
    }

    private static byte[] inflate(byte[] deflated) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(deflated);
        ByteArrayOutputStream out = new ByteArrayOutputStream(deflated.length * 3);
        byte[] buffer = new byte[4096];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            if (count == 0 && inflater.needsInput()) {
                break;
            }
            out.write(buffer, 0, count);
        }
        inflater.end();
        return out.toByteArray();
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

package com.gstncaruso.tabpro.ui.print;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

final class PdfDocument {

    /** A4 sheet in points, the PDF unit (72 per inch). */
    private static final double A4_WIDTH = 595.28;
    private static final double A4_HEIGHT = 841.89;

    private final List<BufferedImage> pages = new ArrayList<>();
    private final double sheetWidth;
    private final double sheetHeight;

    PdfDocument() {
        this(A4_WIDTH, A4_HEIGHT);
    }

    PdfDocument(double sheetWidth, double sheetHeight) {
        this.sheetWidth = sheetWidth;
        this.sheetHeight = sheetHeight;
    }

    void addPage(BufferedImage page) {
        pages.add(page);
    }

    void writeTo(OutputStream out) throws IOException {
        if (pages.isEmpty()) {
            throw new IOException("there is no page to write");
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        append(body, "%PDF-1.4\n");
        int objectCount = 2 + pages.size() * 3;
        int pagesObject = 2;

        // Object 1: catalog, object 2: page tree, then per sheet: page, content and image.
        offsets.add(body.size());
        append(body, "1 0 obj\n<< /Type /Catalog /Pages " + pagesObject + " 0 R >>\nendobj\n");

        offsets.add(body.size());
        StringBuilder kids = new StringBuilder();
        for (int page = 0; page < pages.size(); page++) {
            kids.append(3 + page * 3).append(" 0 R ");
        }
        append(body, pagesObject + " 0 obj\n<< /Type /Pages /Count " + pages.size()
                + " /Kids [ " + kids.toString().strip() + " ] >>\nendobj\n");

        for (int index = 0; index < pages.size(); index++) {
            int pageObject = 3 + index * 3;
            int contentObject = pageObject + 1;
            int imageObject = pageObject + 2;
            BufferedImage image = pages.get(index);

            offsets.add(body.size());
            append(body, pageObject + " 0 obj\n<< /Type /Page /Parent " + pagesObject + " 0 R"
                    + " /MediaBox [ 0 0 " + round(sheetWidth) + " " + round(sheetHeight) + " ]"
                    + " /Resources << /XObject << /Im0 " + imageObject + " 0 R >> >>"
                    + " /Contents " + contentObject + " 0 R >>\nendobj\n");

            byte[] content = contentFor(image).getBytes(StandardCharsets.US_ASCII);
            offsets.add(body.size());
            append(body, contentObject + " 0 obj\n<< /Length " + content.length + " >>\nstream\n");
            body.write(content);
            append(body, "\nendstream\nendobj\n");

            byte[] pixels = deflate(rgbBytesOf(image));
            offsets.add(body.size());
            append(body, imageObject + " 0 obj\n<< /Type /XObject /Subtype /Image"
                    + " /Width " + image.getWidth() + " /Height " + image.getHeight()
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode"
                    + " /Length " + pixels.length + " >>\nstream\n");
            body.write(pixels);
            append(body, "\nendstream\nendobj\n");
        }

        int xrefAt = body.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(objectCount + 1).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int index = 1; index <= objectCount; index++) {
            xref.append(String.format("%010d 00000 n \n", offsets.get(index)));
        }
        xref.append("trailer\n<< /Size ").append(objectCount + 1).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(xrefAt).append("\n%%EOF\n");
        append(body, xref.toString());
        body.writeTo(out);
    }

    private String contentFor(BufferedImage image) {
        double scale = Math.min(sheetWidth / image.getWidth(), sheetHeight / image.getHeight());
        double width = image.getWidth() * scale;
        double height = image.getHeight() * scale;
        double x = (sheetWidth - width) / 2;
        double y = (sheetHeight - height) / 2;
        return "q " + round(width) + " 0 0 " + round(height) + " " + round(x) + " " + round(y) + " cm /Im0 Do Q";
    }

    private static byte[] rgbBytesOf(BufferedImage image) {
        int width = image.getWidth();
        byte[] rgb = new byte[width * image.getHeight() * 3];
        int[] row = new int[width];
        int at = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            image.getRGB(0, y, width, 1, row, 0, width);
            for (int x = 0; x < width; x++) {
                int pixel = row[x];
                rgb[at++] = (byte) ((pixel >> 16) & 0xFF);
                rgb[at++] = (byte) ((pixel >> 8) & 0xFF);
                rgb[at++] = (byte) (pixel & 0xFF);
            }
        }
        return rgb;
    }

    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater =
                new DeflaterOutputStream(compressed, new Deflater(Deflater.BEST_SPEED))) {
            deflater.write(data);
        }
        return compressed.toByteArray();
    }

    private static void append(ByteArrayOutputStream body, String text) throws IOException {
        body.write(text.getBytes(StandardCharsets.US_ASCII));
    }

    private static String round(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}

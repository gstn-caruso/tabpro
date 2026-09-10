package com.gstncaruso.tabpro.ui.print;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The classic Windows BMP layout: BITMAPFILEHEADER + BITMAPINFOHEADER, 24 bits per pixel,
 * uncompressed.
 */
final class BmpDocument {

    private BmpDocument() {
    }

    static boolean canEncode(BufferedImage image) {
        return !image.getColorModel().hasAlpha();
    }

    static void writeTo(BufferedImage image, OutputStream out) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        int rowBytes = width * 3;
        int padding = (4 - (rowBytes % 4)) % 4;
        int imageSize = (rowBytes + padding) * height;

        out.write(fileHeaderAnd(HEADER_SIZE + imageSize, width, height, imageSize));

        int[] row = new int[width];
        byte[] encodedRow = new byte[rowBytes + padding];
        for (int y = height - 1; y >= 0; y--) {
            image.getRGB(0, y, width, 1, row, 0, width);
            int at = 0;
            for (int x = 0; x < width; x++) {
                int pixel = row[x];
                encodedRow[at++] = (byte) pixel;
                encodedRow[at++] = (byte) (pixel >> 8);
                encodedRow[at++] = (byte) (pixel >> 16);
            }
            out.write(encodedRow);
        }
    }

    private static final int HEADER_SIZE = 54;

    private static byte[] fileHeaderAnd(int fileSize, int width, int height, int imageSize) {
        byte[] header = new byte[HEADER_SIZE];
        header[0] = 'B';
        header[1] = 'M';
        putIntLE(header, 2, fileSize);
        putIntLE(header, 10, HEADER_SIZE);
        putIntLE(header, 14, 40);
        putIntLE(header, 18, width);
        putIntLE(header, 22, height);
        header[26] = 1;
        header[28] = 24;
        putIntLE(header, 34, imageSize);
        return header;
    }

    private static void putIntLE(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) value;
        buffer[offset + 1] = (byte) (value >> 8);
        buffer[offset + 2] = (byte) (value >> 16);
        buffer[offset + 3] = (byte) (value >> 24);
    }
}

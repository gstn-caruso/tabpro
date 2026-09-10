package com.gstncaruso.tabpro.ui.print;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class PixelAccessCountingImage extends BufferedImage {

    private int singlePixelCalls;
    private int bulkRowCalls;

    PixelAccessCountingImage(int width, int height) {
        super(width, height, TYPE_INT_RGB);
        Graphics2D graphics = createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, 0, width, height);
        graphics.dispose();
    }

    int singlePixelCalls() {
        return singlePixelCalls;
    }

    int bulkRowCalls() {
        return bulkRowCalls;
    }

    @Override
    public int getRGB(int x, int y) {
        singlePixelCalls++;
        return super.getRGB(x, y);
    }

    @Override
    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        bulkRowCalls++;
        return super.getRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }
}

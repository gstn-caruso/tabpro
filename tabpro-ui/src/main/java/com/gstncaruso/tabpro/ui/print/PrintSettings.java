package com.gstncaruso.tabpro.ui.print;

public record PrintSettings(int fromSheet, int toSheet, int scalePercent, boolean fitToPage, boolean centeredDocument) {

    public static final int MIN_SCALE_PERCENT = 10;
    public static final int MAX_SCALE_PERCENT = 400;

    public static PrintSettings everything(int sheetCount) {
        return of(1, sheetCount, sheetCount, 100, false);
    }

    public static PrintSettings of(int fromSheet, int toSheet, int sheetCount, int scalePercent, boolean fitToPage) {
        return of(fromSheet, toSheet, sheetCount, scalePercent, fitToPage, false);
    }

    public static PrintSettings of(
            int fromSheet, int toSheet, int sheetCount, int scalePercent, boolean fitToPage,
            boolean centeredDocument) {
        int last = Math.max(1, sheetCount);
        int from = Math.clamp(fromSheet, 1, last);
        return new PrintSettings(
                from,
                Math.clamp(toSheet, from, last),
                Math.clamp(scalePercent, MIN_SCALE_PERCENT, MAX_SCALE_PERCENT),
                fitToPage,
                centeredDocument);
    }

    public int sheetsToPrint() {
        return toSheet - fromSheet + 1;
    }

    public int sheetAt(int index) {
        return fromSheet + index;
    }

    public double scaleFor(double sheetWidth, double sheetHeight, double paperWidth, double paperHeight) {
        if (!fitToPage) {
            return scalePercent / 100.0;
        }
        return Math.min(paperWidth / sheetWidth, paperHeight / sheetHeight);
    }
}

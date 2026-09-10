package com.gstncaruso.tabpro.ui.page;

public record PageMetrics(
        int pageWidth,
        int pageHeight,
        int marginTop,
        int marginBottom,
        int marginLeft,
        int marginRight,
        double scoreScale) {

    public static final int HEADER_HEIGHT = 96;
    public static final int FOOTER_HEIGHT = 28;

    public static final int PAGE_GAP = 36;

    private static final double PIXELS_PER_INCH = 100;
    private static final double MILLIMETRES_PER_INCH = 25.4;

    /** The PDF unit is points, 72 per inch. */
    private static final double POINTS_PER_INCH = 72;

    private static final int MINIMUM_CONTENT = 1;

    public static PageMetrics of(PageSetup setup) {
        Orientation orientation = setup.orientation();
        return new PageMetrics(
                pixelsOf(orientation.widthOf(setup.paperFormat())),
                pixelsOf(orientation.heightOf(setup.paperFormat())),
                pixelsOf(setup.marginTop()),
                pixelsOf(setup.marginBottom()),
                pixelsOf(setup.marginLeft()),
                pixelsOf(setup.marginRight()),
                setup.scorePercent() / 100.0);
    }

    public static int pixelsOf(double millimetres) {
        return (int) Math.round(millimetres / MILLIMETRES_PER_INCH * PIXELS_PER_INCH);
    }

    public double pageWidthPoints() {
        return pageWidth * POINTS_PER_INCH / PIXELS_PER_INCH;
    }

    public double pageHeightPoints() {
        return pageHeight * POINTS_PER_INCH / PIXELS_PER_INCH;
    }

    public int contentLeft() {
        return marginLeft;
    }

    public int contentTop() {
        return marginTop + HEADER_HEIGHT;
    }

    public int contentWidth() {
        return Math.max(MINIMUM_CONTENT, pageWidth - marginLeft - marginRight);
    }

    public int contentHeight() {
        return Math.max(MINIMUM_CONTENT, pageHeight - marginTop - marginBottom - HEADER_HEIGHT - FOOTER_HEIGHT);
    }

    public int layoutWidth() {
        return (int) Math.round(contentWidth() / scoreScale);
    }

    public int layoutHeight() {
        return (int) Math.round(contentHeight() / scoreScale);
    }
}

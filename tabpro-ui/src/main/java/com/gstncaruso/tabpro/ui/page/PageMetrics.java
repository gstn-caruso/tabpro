package com.gstncaruso.tabpro.ui.page;

/**
 * La hoja de la {@link PageSetup} medida en pixeles: cuanto ocupa el papel, donde empieza y
 * cuanto mide el area de contenido una vez descontados los margenes, el encabezado y el pie.
 *
 * <p>La partitura se dibuja adentro de esa area con la escala que pide el tamano en porcentaje,
 * asi que el area tiene dos medidas: la de la hoja (pixeles de papel) y la del dibujo
 * ({@link #layoutWidth()} y {@link #layoutHeight()}, en unidades de la partitura). Achicar la
 * partitura al 50% no achica el papel: hace entrar el doble de musica en la misma hoja.
 */
public record PageMetrics(
        int pageWidth,
        int pageHeight,
        int marginTop,
        int marginBottom,
        int marginLeft,
        int marginRight,
        double scoreScale) {

    /** El alto reservado arriba para el encabezado y abajo para el pie, adentro de los margenes. */
    public static final int HEADER_HEIGHT = 96;
    public static final int FOOTER_HEIGHT = 28;

    /** El aire que separa una hoja de la siguiente cuando se las apila en pantalla. */
    public static final int PAGE_GAP = 36;

    private static final double PIXELS_PER_INCH = 100;
    private static final double MILLIMETRES_PER_INCH = 25.4;

    /** Ni el mas apretado de los margenes puede dejar la hoja sin nada de contenido. */
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

    /** Lo que mide el area de contenido en unidades de la partitura, ya sin la escala. */
    public int layoutWidth() {
        return (int) Math.round(contentWidth() / scoreScale);
    }

    public int layoutHeight() {
        return (int) Math.round(contentHeight() / scoreScale);
    }
}

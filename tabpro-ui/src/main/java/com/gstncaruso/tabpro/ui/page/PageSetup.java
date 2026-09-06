package com.gstncaruso.tabpro.ui.page;

/**
 * La configuracion de pagina para imprimir: la ventana de Configurar pagina [F8]. Guarda el papel,
 * la orientacion, los cuatro margenes en milimetros, el tamano de la partitura en porcentaje y que
 * lleva impreso el encabezado y el pie de cada hoja.
 *
 * <p>Es independiente de la impresora: describe el documento, no el aparato que lo saca.
 */
public record PageSetup(
        PaperFormat paperFormat,
        Orientation orientation,
        int marginTop,
        int marginBottom,
        int marginLeft,
        int marginRight,
        int scorePercent,
        PageBanner header,
        PageBanner footer) {

    public static final int MIN_SCORE_PERCENT = 25;
    public static final int MAX_SCORE_PERCENT = 200;

    public PageSetup {
        if (scorePercent < MIN_SCORE_PERCENT || scorePercent > MAX_SCORE_PERCENT) {
            throw new IllegalArgumentException(
                    "scorePercent debe estar entre " + MIN_SCORE_PERCENT + " y " + MAX_SCORE_PERCENT + ": " + scorePercent);
        }
        header = header == null ? PageBanner.header() : header;
        footer = footer == null ? PageBanner.footer() : footer;
    }

    public static PageSetup defaults() {
        return new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, 100,
                PageBanner.header(), PageBanner.footer());
    }
}

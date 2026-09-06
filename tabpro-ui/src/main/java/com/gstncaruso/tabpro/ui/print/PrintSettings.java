package com.gstncaruso.tabpro.ui.print;

/**
 * Lo que se elige en la ventana de Imprimir: que hojas salen -todas o un rango- y de que tamano
 * salen. La escala se da en porcentaje, o se deja que "ajustar a la hoja" la calcule sola para que
 * la hoja dibujada entre justa en el papel de la impresora.
 */
public record PrintSettings(int fromSheet, int toSheet, int scalePercent, boolean fitToPage) {

    public static final int MIN_SCALE_PERCENT = 10;
    public static final int MAX_SCALE_PERCENT = 400;

    public static PrintSettings everything(int sheetCount) {
        return of(1, sheetCount, sheetCount, 100, false);
    }

    /** El rango pedido, traido adentro de las hojas que la partitura realmente tiene. */
    public static PrintSettings of(int fromSheet, int toSheet, int sheetCount, int scalePercent, boolean fitToPage) {
        int last = Math.max(1, sheetCount);
        int from = Math.clamp(fromSheet, 1, last);
        return new PrintSettings(
                from,
                Math.clamp(toSheet, from, last),
                Math.clamp(scalePercent, MIN_SCALE_PERCENT, MAX_SCALE_PERCENT),
                fitToPage);
    }

    public int sheetsToPrint() {
        return toSheet - fromSheet + 1;
    }

    /** Que hoja de la partitura le toca a la enesima hoja que sale de la impresora. */
    public int sheetAt(int index) {
        return fromSheet + index;
    }

    /** Cuanto hay que escalar una hoja dibujada para que salga como se pidio en el papel. */
    public double scaleFor(double sheetWidth, double sheetHeight, double paperWidth, double paperHeight) {
        if (!fitToPage) {
            return scalePercent / 100.0;
        }
        return Math.min(paperWidth / sheetWidth, paperHeight / sheetHeight);
    }
}

package com.gstncaruso.tabpro.format.exchange.ascii;

/** Cuantas columnas de texto entran en un renglon antes de pasar al siguiente. */
public record AsciiTabExportOptions(int columnsPerLine) {

    private static final int MIN_COLUMNS_PER_LINE = 10;
    private static final int DEFAULT_COLUMNS_PER_LINE = 80;

    public AsciiTabExportOptions {
        if (columnsPerLine < MIN_COLUMNS_PER_LINE) {
            throw new IllegalArgumentException("columnsPerLine debe ser >= " + MIN_COLUMNS_PER_LINE + ": " + columnsPerLine);
        }
    }

    public static AsciiTabExportOptions standard() {
        return new AsciiTabExportOptions(DEFAULT_COLUMNS_PER_LINE);
    }
}

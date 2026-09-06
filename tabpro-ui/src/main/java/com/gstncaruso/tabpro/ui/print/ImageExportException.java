package com.gstncaruso.tabpro.ui.print;

/** Se pidio exportar una imagen en una condicion que el manual no permite (BMP fuera de modo Pagina). */
public class ImageExportException extends RuntimeException {

    public ImageExportException(String message) {
        super(message);
    }
}

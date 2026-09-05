package com.gstncaruso.tabpro.ui.instruments;

/** Para que mano se dibuja el diapason: para zurdos, se lo da vuelta. */
public enum Handedness {
    RIGHT_HANDED,
    LEFT_HANDED;

    /** Refleja una coordenada horizontal dentro de un ancho total, si el diapason esta al reves. */
    public int mirror(int x, int totalWidth) {
        return this == LEFT_HANDED ? totalWidth - x : x;
    }
}

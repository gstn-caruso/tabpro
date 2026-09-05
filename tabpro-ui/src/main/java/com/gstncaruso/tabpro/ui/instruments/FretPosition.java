package com.gstncaruso.tabpro.ui.instruments;

/**
 * Un lugar del mastil: una cuerda y un traste. A diferencia de {@code Note} no
 * arrastra ligaduras ni efectos, asi que sirve para comparar posiciones sin que
 * esos detalles interfieran.
 */
public record FretPosition(int string, int fret) {
}

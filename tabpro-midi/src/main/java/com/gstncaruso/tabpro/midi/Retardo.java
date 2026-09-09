package com.gstncaruso.tabpro.midi;

/** Quien decide cuando corre una accion diferida: el reloj real en produccion, al instante en los tests. */
interface Retardo {

    void luegoDe(long millis, Runnable accion);
}

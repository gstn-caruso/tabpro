package com.gstncaruso.tabpro.core.editing;

/** Que parte de la sesion de edicion cambio: solo donde esta parado el cursor (y la seleccion),
 * o la partitura misma. */
public enum EditorChange {
    CURSOR,
    CONTENT
}

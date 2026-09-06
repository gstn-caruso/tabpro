package com.gstncaruso.tabpro.core.editing;

/**
 * En que notacion esta parado el cursor: la tablatura (numeros de traste, cuerda por cuerda) o
 * el pentagrama (alturas, grado por grado). Vive junto al {@link Cursor} y no en la interfaz
 * porque cambia el significado de comandos del dominio -que hace Enter, que hacen las flechas-,
 * no solo como se dibuja: manual, linea 780, "The TAB (tabulation) key allows you to switch
 * notation".
 */
public enum Notation {
    TABLATURE,
    STANDARD;

    public Notation other() {
        return this == TABLATURE ? STANDARD : TABLATURE;
    }
}

package com.gstncaruso.tabpro.core.editing;

/**
 * Donde vive lo ultimo que se corto o copio. El manual permite copiar y pegar entre
 * dos sesiones de Guitar Pro ("it is easy to take a track from another file and paste
 * it in your current file"); este puerto es lo que hace falta cambiar para que eso
 * sea posible aca: quien implemente uno compartido afuera del proceso (el portapapeles
 * del sistema operativo, por ejemplo) no necesita que core sepa nada de eso.
 *
 * <p>{@link #inMemory()} es el comportamiento de siempre: privado de esta sesion, sin
 * compartir nada con ningun otro Editor.
 */
public interface ClipboardStorage {

    void hold(Clipboard.Clipping clipping);

    Clipboard.Clipping content();

    static ClipboardStorage inMemory() {
        return new ClipboardStorage() {
            private Clipboard.Clipping clipping = Clipboard.Clipping.EMPTY;

            @Override
            public void hold(Clipboard.Clipping clipping) {
                this.clipping = clipping;
            }

            @Override
            public Clipboard.Clipping content() {
                return clipping;
            }
        };
    }
}

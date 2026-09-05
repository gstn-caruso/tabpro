package com.gstncaruso.tabpro.ui.theme;

import java.util.List;

/**
 * Cambiar el aspecto de la ventana en caliente. El manual lo llama "skins"; aca
 * son temas, y quien sabe instalarlos es la aplicacion, no la interfaz.
 */
public interface ThemeSwitch {

    /** Un tema que no cambia nada, para cuando la aplicacion no ofrece ninguno. */
    ThemeSwitch NONE = new ThemeSwitch() {

        @Override
        public List<String> names() {
            return List.of();
        }

        @Override
        public String current() {
            return "";
        }

        @Override
        public void apply(String name) {
        }
    };

    List<String> names();

    String current();

    void apply(String name);
}

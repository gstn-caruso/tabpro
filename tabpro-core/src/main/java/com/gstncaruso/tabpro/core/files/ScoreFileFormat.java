package com.gstncaruso.tabpro.core.files;

import java.nio.file.Path;
import java.util.Locale;

/**
 * El manual tiene un solo Archivo &gt; Abrir, que reconoce indistintamente los formatos que
 * sabe leer: esta es la regla, por la extension del archivo, de a que lector le corresponde.
 */
public enum ScoreFileFormat {
    TABPRO,
    GUITAR_PRO,
    TAB_EDIT,
    POWER_TAB;

    public static ScoreFileFormat of(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".gp3") || name.endsWith(".gp4") || name.endsWith(".gp5") || name.endsWith(".gtp")) {
            return GUITAR_PRO;
        }
        if (name.endsWith(".tef")) {
            return TAB_EDIT;
        }
        if (name.endsWith(".ptb")) {
            return POWER_TAB;
        }
        return TABPRO;
    }
}

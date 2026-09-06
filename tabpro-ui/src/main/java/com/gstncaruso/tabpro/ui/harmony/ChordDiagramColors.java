package com.gstncaruso.tabpro.ui.harmony;

import java.awt.Color;

/**
 * La paleta propia del diagrama de acorde: tiene que leerse bien sobre el fondo oscuro de la
 * ventana, asi que no usa las claves genericas del look and feel (permitido para este dibujo,
 * como el diapason y el teclado tienen la suya).
 */
final class ChordDiagramColors {

    static final Color BACKGROUND = new Color(0x2B2D30);
    static final Color GRID = new Color(0x9A948C);
    static final Color NUT = new Color(0xCFCAC2);
    static final Color FINGER = new Color(0xE8A33D);
    static final Color FINGER_INK = new Color(0x1A1A1A);
    static final Color BARRE = new Color(0xE8A33D);
    static final Color OPEN_STRING = new Color(0x63BD63);
    static final Color MUTED_STRING = new Color(0xE05C5C);
    static final Color LABEL = new Color(0xD7D9DD);
    static final Color MUTED_LABEL = new Color(0x8B8F96);

    private ChordDiagramColors() {
    }
}

package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Medido en el render vectorial a 150dpi de la pagina 22 del manual de Guitar Pro 5
 * (pdftoppm -r 150), en la columna limpia entre los dos grupos de tresillos: la ultima
 * linea del pentagrama y la primera de la tablatura quedan a 42px de distancia, con un
 * espaciado de pentagrama de 9,4px en esa misma resolucion (42 / 9,4 ~= 4,5 espacios).
 * Aplicado al espaciado de pentagrama de tabpro (8px), esos 4,5 espacios dan 36px.
 */
class StaffToTabGapTest {

    @Test
    void theGapIsFourAndAHalfStaffLineSpacingsAsInGuitarPro5() {
        assertEquals(36, ScoreLayout.STAFF_TO_TAB_GAP);
    }
}

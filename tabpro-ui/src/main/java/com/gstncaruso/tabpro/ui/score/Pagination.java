package com.gstncaruso.tabpro.ui.score;

import java.util.List;

/**
 * En que hoja cae cada compas y cuantas hojas hay: lo que la barra de estado necesita saber para
 * decir "Pag. 4/7" sin tener que dibujar nada. Se describe por el primer compas de cada hoja.
 */
public record Pagination(List<Integer> firstMeasureOfPage) {

    public Pagination {
        firstMeasureOfPage = firstMeasureOfPage.isEmpty() ? List.of(0) : List.copyOf(firstMeasureOfPage);
    }

    /** Cuando la partitura no se reparte en hojas: todo cae en la primera. */
    public static Pagination single() {
        return new Pagination(List.of(0));
    }

    public static Pagination startingAtMeasures(List<Integer> firstMeasureOfPage) {
        return new Pagination(firstMeasureOfPage);
    }

    public int pageCount() {
        return firstMeasureOfPage.size();
    }

    /** En que hoja cae ese compas, contando desde uno como las cuenta el musico. */
    public int pageOf(int measure) {
        int page = 1;
        while (page < firstMeasureOfPage.size() && firstMeasureOfPage.get(page) <= measure) {
            page++;
        }
        return page;
    }
}

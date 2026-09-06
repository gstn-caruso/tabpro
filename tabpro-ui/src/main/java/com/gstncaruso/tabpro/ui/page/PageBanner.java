package com.gstncaruso.tabpro.ui.page;

import java.util.ArrayList;
import java.util.List;

/**
 * El encabezado o el pie de la hoja: la lista de casilleros que ofrece Configurar pagina, cada uno
 * con su texto. Sabe decir que va impreso en una hoja concreta -{@link #fillIn(PageFields)}-, ya
 * con los campos reemplazados y sin las lineas que quedarian vacias porque la partitura no tiene
 * ese dato.
 */
public record PageBanner(List<BannerLine> lines) {

    public PageBanner {
        lines = List.copyOf(lines);
    }

    /** Lo que va arriba de la hoja: el titulo, el subtitulo, quien lo toca y quien lo escribio. */
    public static PageBanner header() {
        return of(PageElement.TITLE, PageElement.SUBTITLE, PageElement.ARTIST,
                PageElement.ALBUM, PageElement.WORDS, PageElement.MUSIC);
    }

    /** Lo que va abajo de la hoja: el copyright y el numero de pagina. */
    public static PageBanner footer() {
        return of(PageElement.COPYRIGHT, PageElement.PAGE_NUMBER);
    }

    private static PageBanner of(PageElement... elements) {
        return new PageBanner(java.util.Arrays.stream(elements).map(BannerLine::shown).toList());
    }

    public PageBanner with(PageElement element, boolean shown, String text) {
        List<BannerLine> updated = new ArrayList<>(lines);
        updated.replaceAll(line -> line.element() == element ? new BannerLine(element, shown, text) : line);
        return new PageBanner(updated);
    }

    public boolean shows(PageElement element) {
        return lineOf(element).shown();
    }

    public String textOf(PageElement element) {
        return lineOf(element).text();
    }

    /** Que dice esta franja en la hoja que se esta imprimiendo, linea por linea. */
    public List<BannerText> fillIn(PageFields fields) {
        List<BannerText> printed = new ArrayList<>();
        for (BannerLine line : lines) {
            if (!line.shown() || !fields.hasAnythingToSay(line.text())) {
                continue;
            }
            String text = fields.fillIn(line.text()).strip();
            if (!text.isEmpty()) {
                printed.add(new BannerText(line.element(), text));
            }
        }
        return List.copyOf(printed);
    }

    private BannerLine lineOf(PageElement element) {
        return lines.stream()
                .filter(line -> line.element() == element)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("esta franja no tiene " + element));
    }
}

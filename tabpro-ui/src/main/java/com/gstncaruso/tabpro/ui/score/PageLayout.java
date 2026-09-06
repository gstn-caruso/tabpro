package com.gstncaruso.tabpro.ui.score;

import java.util.ArrayList;
import java.util.List;

/**
 * Como se reparten los sistemas de un {@link ScoreLayout} en hojas: cada hoja tiene su margen,
 * su encabezado y su pie, y ningun sistema se corta entre dos hojas. En Modo Pagina las hojas
 * miden todas lo mismo y el contenido que no entra pasa a la siguiente; en Modo Pergamino hay
 * una sola hoja que crece tanto como haga falta.
 */
public final class PageLayout {

    public static final int PAGE_WIDTH = 850;
    public static final int PAGE_HEIGHT = 1100;
    public static final int PAGE_MARGIN = 40;
    public static final int HEADER_HEIGHT = 96;
    public static final int FOOTER_HEIGHT = 28;
    public static final int PAGE_GAP = 36;

    private final List<Integer> firstSystemOfPage;
    private final List<Integer> pageContentHeight;
    private final int systemCount;

    private PageLayout(List<Integer> firstSystemOfPage, List<Integer> pageContentHeight, int systemCount) {
        this.firstSystemOfPage = firstSystemOfPage;
        this.pageContentHeight = pageContentHeight;
        this.systemCount = systemCount;
    }

    public static PageLayout of(ScoreLayout layout, boolean paginate) {
        if (layout.systemCount() == 0) {
            return new PageLayout(List.of(0), List.of(0), 0);
        }
        if (!paginate) {
            int height = layout.systemTop(layout.systemCount() - 1) + layout.systemHeight() - layout.systemTop(0);
            return new PageLayout(List.of(0), List.of(height), layout.systemCount());
        }

        int maxContentHeight = PAGE_HEIGHT - 2 * PAGE_MARGIN - HEADER_HEIGHT - FOOTER_HEIGHT;
        List<Integer> starts = new ArrayList<>(List.of(0));
        List<Integer> heights = new ArrayList<>();
        int pageStart = 0;
        for (int system = 1; system <= layout.systemCount(); system++) {
            boolean lastSystem = system == layout.systemCount();
            int contentSoFar = lastSystem
                    ? layout.systemTop(system - 1) + layout.systemHeight() - layout.systemTop(pageStart)
                    : layout.systemTop(system) + layout.systemHeight() - layout.systemTop(pageStart);
            if (!lastSystem && contentSoFar > maxContentHeight) {
                heights.add(layout.systemTop(system - 1) + layout.systemHeight() - layout.systemTop(pageStart));
                starts.add(system);
                pageStart = system;
            } else if (lastSystem) {
                heights.add(contentSoFar);
            }
        }
        return new PageLayout(starts, heights, layout.systemCount());
    }

    public int pageCount() {
        return firstSystemOfPage.size();
    }

    public int firstSystemOf(int page) {
        return firstSystemOfPage.get(page);
    }

    public int lastSystemOf(int page) {
        return page + 1 < firstSystemOfPage.size() ? firstSystemOfPage.get(page + 1) - 1 : systemCount - 1;
    }

    public int pageOf(int system) {
        int page = 0;
        while (page + 1 < firstSystemOfPage.size() && firstSystemOfPage.get(page + 1) <= system) {
            page++;
        }
        return page;
    }

    /** Cuanto mide de alto el contenido de esa hoja, sin contar margenes ni encabezado ni pie. */
    public int contentHeightOf(int page) {
        return pageContentHeight.get(page);
    }
}

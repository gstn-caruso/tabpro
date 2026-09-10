package com.gstncaruso.tabpro.ui.score;

import java.util.ArrayList;
import java.util.List;

public final class PageLayout {

    private final List<Integer> firstSystemOfPage;
    private final List<Integer> pageContentHeight;
    private final int systemCount;

    private PageLayout(List<Integer> firstSystemOfPage, List<Integer> pageContentHeight, int systemCount) {
        this.firstSystemOfPage = firstSystemOfPage;
        this.pageContentHeight = pageContentHeight;
        this.systemCount = systemCount;
    }

    public static PageLayout parchment(ScoreLayout layout) {
        if (layout.systemCount() == 0) {
            return empty();
        }
        int height = layout.systemTop(layout.systemCount() - 1) + layout.systemHeight() - layout.systemTop(0);
        return new PageLayout(List.of(0), List.of(height), layout.systemCount());
    }

    public static PageLayout paginated(ScoreLayout layout, int maxContentHeight) {
        if (layout.systemCount() == 0) {
            return empty();
        }
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

    private static PageLayout empty() {
        return new PageLayout(List.of(0), List.of(0), 0);
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

    public int contentHeightOf(int page) {
        return pageContentHeight.get(page);
    }
}

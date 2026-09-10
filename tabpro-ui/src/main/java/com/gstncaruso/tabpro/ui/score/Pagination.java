package com.gstncaruso.tabpro.ui.score;

import java.util.List;

public record Pagination(List<Integer> firstMeasureOfPage) {

    public Pagination {
        firstMeasureOfPage = firstMeasureOfPage.isEmpty() ? List.of(0) : List.copyOf(firstMeasureOfPage);
    }

    public static Pagination single() {
        return new Pagination(List.of(0));
    }

    public static Pagination startingAtMeasures(List<Integer> firstMeasureOfPage) {
        return new Pagination(firstMeasureOfPage);
    }

    public int pageCount() {
        return firstMeasureOfPage.size();
    }

    public int pageOf(int measure) {
        int page = 1;
        while (page < firstMeasureOfPage.size() && firstMeasureOfPage.get(page) <= measure) {
            page++;
        }
        return page;
    }
}

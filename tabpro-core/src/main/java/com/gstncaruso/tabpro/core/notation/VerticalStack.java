package com.gstncaruso.tabpro.core.notation;

import java.util.ArrayList;
import java.util.List;

public final class VerticalStack {

    private final int gap;
    private final List<Integer> claimedHeights = new ArrayList<>();

    public VerticalStack(int gap) {
        this.gap = gap;
    }

    public int claim(int symbolHeight) {
        int offset = claimedHeights.stream().mapToInt(height -> height + gap).sum();
        claimedHeights.add(symbolHeight);
        return offset;
    }

    public int totalHeight() {
        if (claimedHeights.isEmpty()) {
            return 0;
        }
        return claimedHeights.stream().mapToInt(Integer::intValue).sum() + gap * (claimedHeights.size() - 1);
    }
}

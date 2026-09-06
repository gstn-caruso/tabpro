package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VerticalStackTest {

    @Test
    void theFirstSymbolSitsRightAgainstTheEdge() {
        VerticalStack stack = new VerticalStack(3);

        assertEquals(0, stack.claim(10));
    }

    @Test
    void theSecondSymbolStartsAfterTheFirstOnePlusTheGap() {
        VerticalStack stack = new VerticalStack(3);
        stack.claim(10);

        assertEquals(13, stack.claim(5));
    }

    @Test
    void aTallerSymbolPushesTheNextOneFurtherAway() {
        VerticalStack tall = new VerticalStack(2);
        tall.claim(20);
        VerticalStack shortStack = new VerticalStack(2);
        shortStack.claim(4);

        assertTrue(tall.claim(1) > shortStack.claim(1));
    }

    @Test
    void totalHeightAddsEverySymbolAndTheGapsBetweenThem() {
        VerticalStack stack = new VerticalStack(3);
        stack.claim(10);
        stack.claim(5);

        assertEquals(18, stack.totalHeight());
    }

    @Test
    void anEmptyStackHasNoHeight() {
        assertEquals(0, new VerticalStack(3).totalHeight());
    }
}

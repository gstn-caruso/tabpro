package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ViewModeTest {

    @Test
    void everyModeHasAReadableLabelInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Page", english.text("views.ViewMode.PAGE"));
        assertEquals("Parchment", english.text("views.ViewMode.PARCHMENT"));
        assertEquals("Vertical Screen", english.text("views.ViewMode.SCREEN_VERTICAL"));
        assertEquals("Horizontal Screen", english.text("views.ViewMode.SCREEN_HORIZONTAL"));
    }

    @Test
    void onlyPageAndParchmentShowThePaper() {
        assertEquals(true, ViewMode.PAGE.showsPaper());
        assertEquals(true, ViewMode.PARCHMENT.showsPaper());
        assertEquals(false, ViewMode.SCREEN_VERTICAL.showsPaper());
        assertEquals(false, ViewMode.SCREEN_HORIZONTAL.showsPaper());
    }
}

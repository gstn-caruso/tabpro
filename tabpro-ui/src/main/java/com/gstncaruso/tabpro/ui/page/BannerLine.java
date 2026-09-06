package com.gstncaruso.tabpro.ui.page;

/** Un casillero del encabezado o del pie: si ese elemento se ve, y con que texto. */
public record BannerLine(PageElement element, boolean shown, String text) {

    public BannerLine {
        text = text == null ? "" : text;
    }

    public static BannerLine shown(PageElement element) {
        return new BannerLine(element, true, element.defaultText());
    }
}

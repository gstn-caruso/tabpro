package com.gstncaruso.tabpro.ui.page;

public record BannerLine(PageElement element, boolean shown, String text) {

    public BannerLine {
        text = text == null ? "" : text;
    }

    public static BannerLine shown(PageElement element) {
        return new BannerLine(element, true, element.defaultText());
    }
}

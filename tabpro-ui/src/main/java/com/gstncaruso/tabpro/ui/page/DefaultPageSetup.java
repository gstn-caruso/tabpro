package com.gstncaruso.tabpro.ui.page;

import java.util.prefs.Preferences;

/**
 * La configuracion de pagina por defecto: la que el manual guarda con "Save As Default Setup" y
 * aplica con "Apply Default Setup". Vive entre sesiones en java.util.prefs, igual que la lista de
 * acordes del usuario, y si lo guardado no se entiende se vuelve a la que trae tabpro.
 */
public final class DefaultPageSetup {

    private static final String SAVED = "saved";
    private static final String PAPER_FORMAT = "paperFormat";
    private static final String ORIENTATION = "orientation";
    private static final String MARGIN_TOP = "marginTop";
    private static final String MARGIN_BOTTOM = "marginBottom";
    private static final String MARGIN_LEFT = "marginLeft";
    private static final String MARGIN_RIGHT = "marginRight";
    private static final String SCORE_PERCENT = "scorePercent";
    private static final String HEADER_NODE = "header";
    private static final String FOOTER_NODE = "footer";
    private static final String SHOWN = ".shown";
    private static final String TEXT = ".text";

    private final Preferences store;

    public DefaultPageSetup(Preferences store) {
        this.store = store;
    }

    public static DefaultPageSetup userSetup() {
        return new DefaultPageSetup(Preferences.userNodeForPackage(DefaultPageSetup.class).node("pageSetup"));
    }

    public PageSetup get() {
        if (!store.getBoolean(SAVED, false)) {
            return PageSetup.defaults();
        }
        PageSetup fallback = PageSetup.defaults();
        try {
            return new PageSetup(
                    PaperFormat.valueOf(store.get(PAPER_FORMAT, fallback.paperFormat().name())),
                    Orientation.valueOf(store.get(ORIENTATION, fallback.orientation().name())),
                    store.getInt(MARGIN_TOP, fallback.marginTop()),
                    store.getInt(MARGIN_BOTTOM, fallback.marginBottom()),
                    store.getInt(MARGIN_LEFT, fallback.marginLeft()),
                    store.getInt(MARGIN_RIGHT, fallback.marginRight()),
                    store.getInt(SCORE_PERCENT, fallback.scorePercent()),
                    bannerFrom(store.node(HEADER_NODE), fallback.header()),
                    bannerFrom(store.node(FOOTER_NODE), fallback.footer()));
        } catch (IllegalArgumentException noSeEntiende) {
            return fallback;
        }
    }

    public void save(PageSetup setup) {
        store.put(PAPER_FORMAT, setup.paperFormat().name());
        store.put(ORIENTATION, setup.orientation().name());
        store.putInt(MARGIN_TOP, setup.marginTop());
        store.putInt(MARGIN_BOTTOM, setup.marginBottom());
        store.putInt(MARGIN_LEFT, setup.marginLeft());
        store.putInt(MARGIN_RIGHT, setup.marginRight());
        store.putInt(SCORE_PERCENT, setup.scorePercent());
        writeBanner(store.node(HEADER_NODE), setup.header());
        writeBanner(store.node(FOOTER_NODE), setup.footer());
        store.putBoolean(SAVED, true);
    }

    private static void writeBanner(Preferences node, PageBanner banner) {
        for (BannerLine line : banner.lines()) {
            node.putBoolean(line.element().name() + SHOWN, line.shown());
            node.put(line.element().name() + TEXT, line.text());
        }
    }

    private static PageBanner bannerFrom(Preferences node, PageBanner fallback) {
        PageBanner banner = fallback;
        for (BannerLine line : fallback.lines()) {
            String key = line.element().name();
            banner = banner.with(
                    line.element(),
                    node.getBoolean(key + SHOWN, line.shown()),
                    node.get(key + TEXT, line.text()));
        }
        return banner;
    }
}

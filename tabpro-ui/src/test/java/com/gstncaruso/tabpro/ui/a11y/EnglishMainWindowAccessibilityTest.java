package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.menu.MenuBar;
import com.gstncaruso.tabpro.ui.score.Zoom;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import com.gstncaruso.tabpro.ui.toolbar.ToolBars;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class EnglishMainWindowAccessibilityTest {

    @Test
    void theEnglishToolBarsAndMenusLeaveNoControlWithoutAnAccessibleNameOrTooltip() {
        Texts.install(Locale.ENGLISH);
        try {
            Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
            Commands commands = new Commands(
                    editor, port(Ports.Document.class), port(Ports.Dialogs.class),
                    port(Ports.Playback.class), port(Ports.View.class), List.of("dark", "light"));
            ToolBars toolBars = new ToolBars(editor, commands, new FixedZoom());
            AccessibilityWalker walker = new AccessibilityWalker();

            List<Violation> violations = new ArrayList<>();
            violations.addAll(walker.walk(toolBars.component()));
            violations.addAll(walker.walk(toolBars.effectsComponent()));
            violations.addAll(walker.walk(
                    new MenuBar(commands, () -> List.of(Path.of("/tmp/one.tabpro")), path -> { }).build()));

            assertEquals(List.of(), violations);
        } finally {
            Texts.install(Locale.forLanguageTag("es"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T port(Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null;
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
    }

    private static final class FixedZoom implements ZoomHolder {

        @Override
        public Zoom zoom() {
            return Zoom.whole();
        }

        @Override
        public void setZoom(Zoom zoom) {
        }

        @Override
        public void onZoomChange(Runnable listener) {
        }
    }
}

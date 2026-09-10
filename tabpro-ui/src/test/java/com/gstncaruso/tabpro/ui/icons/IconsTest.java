package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class IconsTest {

    private static final Color THEME_COLOR = new Color(10, 20, 30);

    @ParameterizedTest
    @MethodSource("everyPublicFactory")
    void theFactoryReturnsAnIconOfTheDeclaredSize(Method factory) throws ReflectiveOperationException {
        Icon icon = invoke(factory);

        assertEquals(Icons.SIZE, icon.getIconWidth(), factory.getName() + " width");
        assertEquals(Icons.SIZE, icon.getIconHeight(), factory.getName() + " height");
    }

    @ParameterizedTest
    @MethodSource("everyPublicFactory")
    void theFactoryPaintsWithTheComponentsForeground(Method factory) throws ReflectiveOperationException {
        Icon icon = invoke(factory);
        JPanel probe = new JPanel();
        probe.setForeground(THEME_COLOR);

        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        icon.paintIcon(probe, canvas, 0, 0);
        canvas.dispose();

        assertTrue(hasAPixelOfTheThemeColor(image), factory.getName() + " did not paint with the theme color");
    }

    @Test
    void thereIsAtLeastOneFactoryToTest() {
        assertTrue(everyPublicFactory().size() > 0);
    }

    static List<Method> everyPublicFactory() {
        return java.util.Arrays.stream(Icons.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.getReturnType() == Icon.class)
                .toList();
    }

    private static Icon invoke(Method factory) throws ReflectiveOperationException {
        try {
            return (Icon) factory.invoke(null, representativeArgumentsFor(factory));
        } catch (InvocationTargetException e) {
            throw new AssertionError(factory.getName() + " failed to invoke", e.getCause());
        }
    }

    private static Object[] representativeArgumentsFor(Method factory) {
        return java.util.Arrays.stream(factory.getParameterTypes())
                .map(IconsTest::representativeArgumentFor)
                .toArray();
    }

    private static Object representativeArgumentFor(Class<?> parameterType) {
        if (parameterType == NoteValue.class) {
            return NoteValue.QUARTER;
        }
        if (parameterType == int.class) {
            return 3;
        }
        if (parameterType == String.class) {
            return "PM";
        }
        throw new IllegalArgumentException("No representative argument for " + parameterType);
    }

    private static boolean hasAPixelOfTheThemeColor(BufferedImage image) {
        int themeRgb = THEME_COLOR.getRGB() & 0xFFFFFF;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                boolean visible = (pixel >>> 24) != 0;
                if (visible && (pixel & 0xFFFFFF) == themeRgb) {
                    return true;
                }
            }
        }
        return false;
    }
}

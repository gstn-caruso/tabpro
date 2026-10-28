package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.util.UIScale;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.tracks.MeasureGrid;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import com.gstncaruso.tabpro.ui.tracks.MixParameter;
import com.gstncaruso.tabpro.ui.tracks.MixTable;
import com.gstncaruso.tabpro.ui.tracks.MixTableRow;
import com.gstncaruso.tabpro.ui.tracks.ParameterCell;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Isolated;

/** Isolated because LookAndFeel, UI defaults and scaling properties are JVM-wide. */
@Isolated
class MixTableScalingTest {

    @Test
    void givenHiDpiMixer_whenLayingOutMaximumEffectValues_thenNumbersAndHeaderControlsRemainReadable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
            String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
            try {
                System.setProperty(FlatSystemProperties.UI_SCALE, "2");
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();
                assertEquals(2f, UIScale.getUserScaleFactor(), "Controlled HiDPI baseline");
                assertEquals(29, new JButton().getFont().getSize(), "Controlled desktop font");
                Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
                for (MixParameter effect : List.of(
                        MixParameter.CHORUS, MixParameter.REVERB, MixParameter.PHASER, MixParameter.TREMOLO)) {
                    effect.applyTo(editor, 0, 127);
                }
                MixTable table = new MixTable(editor);
                MixTableRow row = descendants(table, MixTableRow.class).getFirst();
                int headerHeight = table.getComponent(0).getPreferredSize().height;
                table.setSize(table.getPreferredSize().width, headerHeight + row.getPreferredSize().height);

                layoutRecursively(table);

                List<ParameterCell> cells = descendants(table, ParameterCell.class);
                List<JButton> headerButtons = descendants((Container) table.getComponent(0), JButton.class);
                assertEquals(4, cells.size(), "All four effects are present");
                assertEquals(2, headerButtons.size(), "Reduce and restore controls are present");
                assertAll("Maximum effect values and header glyphs fit the compact mixer",
                        Stream.concat(cells.stream().map(cell -> () -> {
                            JFormattedTextField field = descendants(cell, JFormattedTextField.class).getFirst();
                            Insets insets = field.getInsets();
                            int available = field.getWidth() - insets.left - insets.right;
                            int required = field.getFontMetrics(field.getFont()).stringWidth("127");
                            String label = field.getAccessibleContext().getAccessibleName();
                            assertAll(label,
                                    () -> assertTrue(cell.isVisible() && field.isVisible() && field.isEditable(),
                                            "The effect value remains visible and editable"),
                                    () -> assertEquals("127", field.getText(), "The maximum value is displayed"),
                                    () -> assertTrue(cell.getWidth() <= 84,
                                            "Effect column must remain compact: " + cell.getWidth() + "px > 84px"),
                                    () -> assertTrue(available >= required,
                                            label + ": text bounds " + field.getBounds() + ", insets " + insets
                                                    + "; available " + available + "px < required " + required
                                                    + "px for 127"));
                        }), headerButtons.stream().map(button -> () -> {
                            Insets insets = button.getInsets();
                            Rectangle available = new Rectangle(insets.left, insets.top,
                                    button.getWidth() - insets.left - insets.right,
                                    button.getHeight() - insets.top - insets.bottom);
                            String paintedText = SwingUtilities.layoutCompoundLabel(button,
                                    button.getFontMetrics(button.getFont()), button.getText(), button.getIcon(),
                                    button.getVerticalAlignment(), button.getHorizontalAlignment(),
                                    button.getVerticalTextPosition(), button.getHorizontalTextPosition(),
                                    available, new Rectangle(), new Rectangle(), button.getIconTextGap());
                            assertEquals(button.getText(), paintedText,
                                    "Header glyph must not become ellipsis in " + available);
                        })));
            } finally {
                restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
                try {
                    UIManager.setLookAndFeel(originalLaf);
                } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                    throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                } finally {
                    originalOverrides.forEach(UIManager::put);
                }
            }
        });
    }

    @Test
    @Tag("integration")
    void givenExistingScaledTrackPanel_whenChangingAndRestoringFontSize_thenRowsAndMeasureHitsStayAligned()
            throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
            String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
            JFrame window = new JFrame();
            try {
                System.clearProperty(FlatSystemProperties.UI_SCALE);
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();
                Theme theme = Theme.install();
                theme.useFontSize(12);
                assertEquals(2f, UIScale.getUserScaleFactor(), "Controlled HiDPI baseline");
                Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
                editor.addTrack(Track.standardBass("Bajo"));
                editor.selectTrack(0);
                TrackPanel panel = new TrackPanel(editor);
                window.setContentPane(panel);
                window.pack();
                window.setVisible(true);
                List<MixTableRow> rows = descendants(panel, MixTableRow.class);
                MeasureGrid grid = descendants(panel, MeasureGrid.class).getFirst();
                assertEquals(2, rows.size(), "Two existing mixer rows");

                assertAll("Existing panel stays aligned after changing and restoring the font",
                        Stream.of(18, 12).map(points -> () -> {
                            theme.useFontSize(points);
                            window.pack();
                            layoutRecursively(panel);
                            assertAll("Font adjustment " + points,
                                    () -> assertAll("Mixer rows match the actual grid cells",
                                            java.util.stream.IntStream.range(0, rows.size()).mapToObj(track -> () -> {
                                                MixTableRow row = rows.get(track);
                                                Rectangle cell = grid.cellBounds(track, 0);
                                                Point rowTop = SwingUtilities.convertPoint(row, 0, 0, panel);
                                                Point cellTop = SwingUtilities.convertPoint(grid, cell.x, cell.y, panel);
                                                assertAll("Track " + (track + 1),
                                                        () -> assertEquals(cell.height, row.getHeight(),
                                                                "Mixer row height matches grid cell height"),
                                                        () -> assertEquals(cellTop.y, rowTop.y,
                                                                "Mixer row top matches grid cell top"));
                                            })),
                                    () -> {
                                        MixTableRow secondRow = rows.get(1);
                                        Point besideSecondRow = SwingUtilities.convertPoint(secondRow, 0,
                                                secondRow.getHeight() / 2, grid);
                                        grid.dispatchEvent(new MouseEvent(grid, MouseEvent.MOUSE_PRESSED,
                                                System.currentTimeMillis(), 0, grid.cellBounds(1, 0).width / 2,
                                                besideSecondRow.y, 1, false));
                                        assertEquals(1, editor.cursor().track(),
                                                "Click beside the second mixer row selects the second track");
                                    });
                        }));
            } finally {
                window.dispose();
                restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
                try {
                    UIManager.setLookAndFeel(originalLaf);
                } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                    throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                } finally {
                    originalOverrides.forEach(UIManager::put);
                }
            }
        });
    }

    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) {
                layoutRecursively(nested);
            }
        }
    }

    private static <T extends Component> List<T> descendants(Container container, Class<T> type) {
        return Arrays.stream(container.getComponents())
                .flatMap(child -> Stream.concat(Stream.of(child), child instanceof Container nested
                        ? descendants(nested, Component.class).stream() : Stream.empty()))
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}

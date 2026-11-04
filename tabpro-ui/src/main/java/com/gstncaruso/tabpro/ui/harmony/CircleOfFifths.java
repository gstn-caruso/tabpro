package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleLibrary;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.function.BiConsumer;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

final class CircleOfFifths extends JPanel {

    private static final String[] MAJORS = {
        "C", "G", "D", "A", "E", "B", "F#/Gb", "Db", "Ab", "Eb", "Bb", "F"
    };
    private static final String[] MINORS = {
        "A", "E", "B", "F#", "C#", "G#", "D#/Eb", "Bb", "F", "C", "G", "D"
    };

    private final ButtonGroup selection = new ButtonGroup();
    private final JToggleButton[] buttons = new JToggleButton[24];

    CircleOfFifths(BiConsumer<PitchClass, Scale> choose) {
        super(new BorderLayout());
        JPanel rings = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                double diameter = 300;
                double[] x = new double[buttons.length];
                double[] y = new double[buttons.length];
                Dimension[] sizes = new Dimension[buttons.length];
                for (int index = 0; index < buttons.length; index++) {
                    double angle = (index % 12) * Math.PI / 6 - Math.PI / 2;
                    double radius = index < 12 ? 0.43 : 0.26;
                    x[index] = radius * Math.cos(angle);
                    y[index] = radius * Math.sin(angle);
                    sizes[index] = buttons[index].getPreferredSize();
                    diameter = Math.max(diameter,
                            (sizes[index].width + 2) / (1 - 2 * Math.abs(x[index])));
                    diameter = Math.max(diameter,
                            (sizes[index].height + 2) / (1 - 2 * Math.abs(y[index])));
                    for (int other = 0; other < index; other++) {
                        // Either axis must separate the rectangles, including rounding room.
                        double horizontal = ((sizes[index].width + sizes[other].width) / 2.0 + 2)
                                / Math.abs(x[index] - x[other]);
                        double vertical = ((sizes[index].height + sizes[other].height) / 2.0 + 2)
                                / Math.abs(y[index] - y[other]);
                        diameter = Math.max(diameter, Math.min(horizontal, vertical));
                    }
                }
                int extent = (int) Math.ceil(diameter);
                return new Dimension(Math.max(420, extent), extent);
            }

            @Override
            public void doLayout() {
                for (int index = 0; index < buttons.length; index++) {
                    JToggleButton button = buttons[index];
                    double angle = (index % 12) * Math.PI / 6 - Math.PI / 2;
                    double radius = Math.min(getWidth(), getHeight()) * (index < 12 ? 0.43 : 0.26);
                    Dimension size = button.getPreferredSize();
                    int x = (int) Math.round(getWidth() / 2.0 + radius * Math.cos(angle));
                    int y = (int) Math.round(getHeight() / 2.0 + radius * Math.sin(angle));
                    button.setBounds(x - size.width / 2, y - size.height / 2, size.width, size.height);
                }
            }
        };
        for (int index = 0; index < buttons.length; index++) {
            boolean minor = index >= 12;
            String note = (minor ? MINORS : MAJORS)[index % 12];
            String name = note + " " + Texts.get(minor
                    ? "views.ScalesDialog.circleMinor" : "views.ScalesDialog.circleMajor");
            JToggleButton button = new JToggleButton(note);
            button.setMargin(new Insets(2, 6, 2, 6));
            button.getAccessibleContext().setAccessibleName(name);
            button.setToolTipText(name);
            button.addActionListener(event -> choose.accept(tonic(note),
                    minor ? ScaleLibrary.naturalMinor() : ScaleLibrary.major()));
            selection.add(button);
            buttons[index] = button;
            rings.add(button);
        }
        add(rings, BorderLayout.CENTER);
        add(new JLabel(Texts.get("views.ScalesDialog.circleLegend"), SwingConstants.CENTER), BorderLayout.SOUTH);
    }

    void showSelection(PitchClass tonic, Scale scale) {
        selection.clearSelection();
        for (int index = 0; index < buttons.length; index++) {
            boolean minor = index >= 12;
            String note = (minor ? MINORS : MAJORS)[index % 12];
            if (tonic(note).semitone() == tonic.semitone()
                    && scale.equals(minor ? ScaleLibrary.naturalMinor() : ScaleLibrary.major())) {
                buttons[index].setSelected(true);
                return;
            }
        }
    }

    private static PitchClass tonic(String label) {
        return PitchClass.of(label.split("/")[0]);
    }
}

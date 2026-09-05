package com.gstncaruso.tabpro.ui.percussion;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.instruments.InstrumentEditing;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * El asistente de percusion del manual: en la zona (1) los sonidos GM por numero,
 * en la zona (2) el mismo sonido elegido por su posicion en el pentagrama. Solo
 * tiene sentido con una pista de percusion activa.
 */
public final class PercussionAssistant extends JPanel {

    private final Editor editor;
    private final PercussionSoundPalette palette;
    private final PercussionStaffPicker staff;
    private final JCheckBox electric;

    public PercussionAssistant(Editor editor, Player player) {
        super(new BorderLayout());
        this.editor = editor;
        setBackground(ScoreColors.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        InstrumentEditing editing = new InstrumentEditing(editor, player);

        electric = new JCheckBox("Usar el sonido eléctrico cuando esté disponible");
        electric.setOpaque(false);
        electric.setForeground(ScoreColors.LABEL);
        electric.setFont(electric.getFont().deriveFont(11f));

        palette = new PercussionSoundPalette(
                sound -> sound(player, sound),
                sound -> editing.pressFret(new Note(editor.cursor().string(), sound)));

        staff = new PercussionStaffPicker(
                sound -> sound(player, sound),
                line -> editing.pressFret(new Note(line.number(), line.soundToUse(electric.isSelected()))));
        electric.addActionListener(e -> staff.setPreferElectric(electric.isSelected()));

        JPanel zone1 = zone("Sonidos", scrollable(palette));
        JPanel zone2 = zone("Pentagrama de percusión", withCheckbox(staff, electric));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(zone1);
        body.add(zone2);

        add(body, BorderLayout.CENTER);
        add(warningLabel(), BorderLayout.SOUTH);
    }

    /** Solo una pista de percusion tiene sentido para este asistente. */
    public static boolean appliesTo(Track track) {
        return track.isPercussion();
    }

    public boolean appliesToCurrentTrack() {
        return appliesTo(editor.currentTrack());
    }

    /** Accesos para los tests: no hacen falta para usar el asistente. */
    PercussionSoundPalette soundPalette() {
        return palette;
    }

    PercussionStaffPicker staffPicker() {
        return staff;
    }

    JCheckBox electricCheckbox() {
        return electric;
    }

    private static void sound(Player player, int soundNumber) {
        player.playNote(new Pitch(soundNumber), Track.PERCUSSION_PROGRAM);
    }

    private JPanel zone(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(ScoreColors.MUTED_INK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 0));

        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JComponent scrollable(PercussionSoundPalette palette) {
        JScrollPane scroll = new JScrollPane(palette);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 160));
        return scroll;
    }

    private JComponent withCheckbox(PercussionStaffPicker picker, JCheckBox electric) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(picker, BorderLayout.CENTER);
        panel.add(electric, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel warningLabel() {
        String text = "Los sonidos fuera del rango " + PercussionKit.LOWEST_SOUND + "–"
                + PercussionKit.HIGHEST_SOUND + " pueden no sonar en todas las placas de sonido.";
        JLabel warning = new JLabel("<html>" + text + "</html>");
        warning.setForeground(ScoreColors.WARNING);
        warning.setFont(warning.getFont().deriveFont(10f));
        warning.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        return warning;
    }
}

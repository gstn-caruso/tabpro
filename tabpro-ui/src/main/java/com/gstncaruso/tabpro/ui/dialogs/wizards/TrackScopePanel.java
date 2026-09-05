package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/** Si un asistente trabaja sobre la pista activa o sobre todas. */
public final class TrackScopePanel extends JPanel {

    private final JRadioButton currentTrack = new JRadioButton("Pista activa", true);
    private final JRadioButton everyTrack = new JRadioButton("Todas las pistas");

    public TrackScopePanel() {
        super(new java.awt.GridLayout(0, 1, 0, DialogStyle.GAP_XS));
        ButtonGroup group = new ButtonGroup();
        group.add(currentTrack);
        group.add(everyTrack);
        add(currentTrack);
        add(everyTrack);
    }

    public boolean everyTrackSelected() {
        return everyTrack.isSelected();
    }
}

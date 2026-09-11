package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public final class TrackScopePanel extends JPanel {

    private final JRadioButton currentTrack = new JRadioButton(Texts.get("score_dialogs.TrackScopePanel.currentTrack"), true);
    private final JRadioButton everyTrack = new JRadioButton(Texts.get("score_dialogs.TrackScopePanel.everyTrack"));

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

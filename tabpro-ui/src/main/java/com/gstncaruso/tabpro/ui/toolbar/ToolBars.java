package com.gstncaruso.tabpro.ui.toolbar;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.actions.Command;
import com.gstncaruso.tabpro.ui.actions.Commands;
import com.gstncaruso.tabpro.ui.score.ZoomHolder;
import com.gstncaruso.tabpro.ui.theme.Palette;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public final class ToolBars {

    private final Commands commands;
    private final Editor editor;
    private final ZoomHolder zoomHolder;
    private final JPanel rows = new JPanel();
    final JToolBar documentToolBar;
    final JToolBar structureToolBar;
    final JToolBar notationToolBar;
    final JToolBar effectsToolBar;

    public ToolBars(Editor editor, Commands commands, ZoomHolder zoomHolder) {
        this.commands = commands;
        this.editor = editor;
        this.zoomHolder = zoomHolder;
        documentToolBar = leftAligned(documentRow());
        structureToolBar = leftAligned(structureRow());
        notationToolBar = leftAligned(notationRow());
        effectsToolBar = leftAligned(effectsRow());
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Palette.panel());
        rows.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Palette.separator()));
        rows.add(documentToolBar);
        rows.add(structureToolBar);
        rows.add(notationToolBar);
    }

    public JComponent component() {
        return rows;
    }

    public JComponent effectsComponent() {
        return effectsToolBar;
    }

    public void setVisible(boolean visible) {
        rows.setVisible(visible);
    }

    public boolean isVisible() {
        return rows.isVisible();
    }

    public void setDocumentToolBarVisible(boolean visible) {
        documentToolBar.setVisible(visible);
    }

    public boolean isDocumentToolBarVisible() {
        return documentToolBar.isVisible();
    }

    public void setStructureToolBarVisible(boolean visible) {
        structureToolBar.setVisible(visible);
    }

    public boolean isStructureToolBarVisible() {
        return structureToolBar.isVisible();
    }

    public void setNotationToolBarVisible(boolean visible) {
        notationToolBar.setVisible(visible);
    }

    public boolean isNotationToolBarVisible() {
        return notationToolBar.isVisible();
    }

    public void setEffectsToolBarVisible(boolean visible) {
        effectsToolBar.setVisible(visible);
    }

    public boolean isEffectsToolBarVisible() {
        return effectsToolBar.isVisible();
    }

    public void addToSoundRow(JComponent component) {
        structureToolBar.add(Box.createHorizontalStrut(4));
        structureToolBar.add(component);
    }

    private JToolBar documentRow() {
        JToolBar bar = emptyBar();
        add(bar, "file.new", "file.open", "file.save");
        bar.addSeparator();
        add(bar, "file.information");
        bar.addSeparator();
        add(bar, "file.pageSetup", "file.print");
        bar.addSeparator();
        add(bar, "edit.undo", "edit.redo");
        bar.addSeparator();
        add(bar, "track.add", "track.properties", "track.moveUp", "track.moveDown", "track.delete",
                "tool.checkBarDurations");
        bar.addSeparator();
        add(bar, "bar.insert", "bar.delete");
        bar.addSeparator();
        add(bar, "edit.cut", "options.preferences");
        bar.addSeparator();
        add(bar, "view.multitrack");
        bar.addSeparator();
        add(bar, "view.page", "view.parchment", "view.verticalScreen", "view.horizontalScreen");
        bar.addSeparator();
        bar.add(new ZoomSelector(zoomHolder, commands));
        bar.addSeparator();
        add(bar, "view.fretboard", "view.keyboard", "view.mixTable");
        bar.addSeparator();
        add(bar, "edit.copy", "edit.paste");
        bar.addSeparator();
        bar.add(new TrackSelector(editor, commands));
        return bar;
    }

    private JToolBar structureRow() {
        JToolBar bar = emptyBar();
        add(bar, "bar.keySignature", "bar.timeSignature", "bar.tripletFeel");
        bar.addSeparator();
        add(bar, "bar.repeatOpen", "bar.repeatClose");
        bar.addSeparator();
        add(bar, "bar.doubleBar");
        bar.addSeparator();
        add(bar, "bar.alternateEndings", "bar.forceLineBreak", "bar.preventLineBreak");
        bar.addSeparator();
        add(bar, "marker.insert", "marker.edit", "marker.previous", "marker.next", "marker.list");
        bar.addSeparator();
        add(bar, "sound.play", "nav.firstBar", "nav.lastBar", "sound.metronome", "sound.countDown",
                "sound.loop");
        bar.add(soundFontToggle());
        bar.addSeparator();
        add(bar, "tool.transpose");
        bar.addSeparator();
        add(bar, "nav.previousBar", "nav.nextBar", "tool.scales", "tool.tuner");
        bar.addSeparator();
        return bar;
    }

    private JToolBar notationRow() {
        JToolBar bar = emptyBar();
        add(bar, "note.value.WHOLE", "note.value.HALF", "note.value.QUARTER", "note.value.EIGHTH",
                "note.value.SIXTEENTH", "note.value.THIRTY_SECOND", "note.value.SIXTY_FOURTH");
        bar.addSeparator();
        add(bar, "note.dot", "note.triplet", "note.tieBeat");
        bar.addSeparator();
        add(bar, "note.rest");
        bar.addSeparator();
        add(bar, "note.tie", "note.soundDuration");
        bar.addSeparator();
        add(bar, "bar.octave8va", "bar.octave8vb", "bar.octave15ma", "bar.octave15mb");
        bar.addSeparator();
        add(bar, "view.hideStandardNotation", "view.hideTablature");
        bar.addSeparator();
        add(bar, "note.preventBeamBreak", "note.forceBeamBreak", "note.resetBeamBreak");
        bar.addSeparator();
        add(bar, "note.stemUp", "note.stemDown", "note.stemAutomatic");
        bar.addSeparator();
        add(bar, "note.dynamic.PIANO_PIANISSIMO", "note.dynamic.PIANISSIMO", "note.dynamic.PIANO",
                "note.dynamic.MEZZO_PIANO", "note.dynamic.MEZZO_FORTE", "note.dynamic.FORTE",
                "note.dynamic.FORTISSIMO", "note.dynamic.FORTE_FORTISSIMO");
        return bar;
    }

    private JToolBar effectsRow() {
        JToolBar bar = emptyBar();
        add(bar, "effect.deadNote", "effect.graceNote", "effect.ghostNote", "effect.accent",
                "effect.heavyAccent", "effect.letRing", "effect.naturalHarmonic", "effect.artificialHarmonic");
        bar.addSeparator();
        add(bar, "effect.hammer", "effect.legatoSlide", "effect.shiftSlide", "effect.bend", "effect.tremoloBar",
                "effect.vibrato", "effect.wideVibrato");
        bar.addSeparator();
        add(bar, "effect.trill", "effect.tremoloPicking", "effect.palmMute", "effect.staccato");
        bar.addSeparator();
        add(bar, "effect.tapping", "effect.slapping", "effect.popping");
        bar.addSeparator();
        add(bar, "effect.fadeIn", "effect.pickstrokeDown", "effect.pickstrokeUp");
        bar.addSeparator();
        add(bar, "note.chord", "effect.text", "note.mixTableChange", "note.fingering",
                "note.fingeringRightHand");
        bar.addSeparator();
        add(bar, "effect.strokeUp", "effect.strokeDown");
        return bar;
    }

    private static <T extends JComponent> T leftAligned(T row) {
        row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JToolBar emptyBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return bar;
    }

    private void add(JToolBar bar, String... names) {
        for (String name : names) {
            bar.add(button(commands.get(name)));
        }
    }

    private static JButton button(Command command) {
        JButton button = new JButton(command);
        button.setText(null);
        button.setForeground(Palette.text());
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText(tooltipOf(command));
        button.getAccessibleContext().setAccessibleName(command.label());
        button.setPreferredSize(new Dimension(26, 24));
        button.setMaximumSize(new Dimension(26, 24));
        button.addChangeListener(event -> button.setContentAreaFilled(
                button.getModel().isRollover() || button.getModel().isPressed()));
        return button;
    }

    private JToggleButton soundFontToggle() {
        return toggleButton(commands.get("sound.soundFont"));
    }

    private static JToggleButton toggleButton(Command command) {
        JToggleButton button = new JToggleButton(command);
        button.setText(null);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(button.isSelected());
        button.setToolTipText(tooltipOf(command));
        button.getAccessibleContext().setAccessibleName(command.label());
        button.setPreferredSize(new Dimension(26, 24));
        button.setMaximumSize(new Dimension(26, 24));
        button.addChangeListener(event -> button.setContentAreaFilled(
                button.getModel().isRollover() || button.getModel().isPressed() || button.isSelected()));
        return button;
    }

    private static String tooltipOf(Command command) {
        String shortcut = command.acceleratorText();
        return shortcut.isEmpty() ? command.description() : command.description() + "  [" + shortcut + "]";
    }
}

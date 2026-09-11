package com.gstncaruso.tabpro.ui.percussion;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public final class PercussionSoundPalette extends JPanel {

    private static final int COLUMNS = 4;

    private final JList<Integer> list;

    public PercussionSoundPalette(IntConsumer onPlay, IntConsumer onAdd) {
        super(new BorderLayout());
        setOpaque(false);

        list = new JList<>(PercussionKit.sounds().toArray(new Integer[0]));
        list.getAccessibleContext().setAccessibleName(Texts.get("views.PercussionSoundPalette.sounds"));
        list.setToolTipText(Texts.get("views.PercussionSoundPalette.sounds"));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(ScoreColors.SURFACE);
        list.setForeground(ScoreColors.INK);
        SoundRenderer renderer = new SoundRenderer();
        list.setCellRenderer(renderer);
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(rowsToFitFourColumns(list.getModel().getSize()));
        Dimension cellSize = widestCellSize(list, renderer);
        list.setFixedCellHeight(cellSize.height);
        list.setFixedCellWidth(cellSize.width);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0 || !list.getCellBounds(index, index).contains(e.getPoint())) {
                    return;
                }
                int sound = list.getModel().getElementAt(index);
                if (e.getClickCount() >= 2) {
                    onAdd.accept(sound);
                } else {
                    onPlay.accept(sound);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    JList<Integer> soundList() {
        return list;
    }

    private static int rowsToFitFourColumns(int soundCount) {
        return (soundCount + COLUMNS - 1) / COLUMNS;
    }

    private static Dimension widestCellSize(JList<Integer> list, SoundRenderer renderer) {
        int width = 0;
        int height = 0;
        for (int sound : PercussionKit.sounds()) {
            Dimension preferred = renderer
                    .getListCellRendererComponent(list, sound, 0, false, false)
                    .getPreferredSize();
            width = Math.max(width, preferred.width);
            height = Math.max(height, preferred.height);
        }
        return new Dimension(width, height);
    }

    private static final class SoundRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> jlist, Object value, int index, boolean isSelected, boolean hasFocus) {
            super.getListCellRendererComponent(jlist, value, index, isSelected, hasFocus);
            int sound = (Integer) value;
            setText(sound + " — " + Labels.percussionSoundName(sound).orElse("?"));
            if (!isSelected) {
                setBackground(ScoreColors.SURFACE);
                setForeground(ScoreColors.INK);
            }
            return this;
        }
    }
}

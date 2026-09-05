package com.gstncaruso.tabpro.ui.percussion;

import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * La zona (1) del asistente de percusion: los numeros de sonido de la placa GM.
 * Un clic los escucha, un doble clic los agrega al beat en la linea del cursor.
 */
public final class PercussionSoundPalette extends JPanel {

    private final JList<Integer> list;

    public PercussionSoundPalette(IntConsumer onPlay, IntConsumer onAdd) {
        super(new BorderLayout());
        setOpaque(false);

        list = new JList<>(PercussionKit.sounds().toArray(new Integer[0]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(ScoreColors.SURFACE);
        list.setForeground(ScoreColors.INK);
        list.setCellRenderer(new SoundRenderer());
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

    /** La lista en si, para los tests: no hace falta para usar el panel. */
    JList<Integer> soundList() {
        return list;
    }

    private static final class SoundRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> jlist, Object value, int index, boolean isSelected, boolean hasFocus) {
            super.getListCellRendererComponent(jlist, value, index, isSelected, hasFocus);
            int sound = (Integer) value;
            setText(sound + " — " + PercussionKit.nameOf(sound).orElse("?"));
            if (!isSelected) {
                setBackground(ScoreColors.SURFACE);
                setForeground(ScoreColors.INK);
            }
            return this;
        }
    }
}

package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.TrackChords;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;

/**
 * La ventana de acordes del manual, con sus seis zonas: construir el acorde por
 * nombre, el diagrama principal editable, la lista de posiciones, los nombres
 * alternativos, los acordes usados en la pista y la biblioteca propia.
 */
public final class ChordDialog {

    private ChordDialog() {
    }

    public static void show(Component parent, Editor editor, Player player, boolean showBassInChordName) {
        ChordEditorModel model = ChordEditorModel.forBeat(
                editor.currentBeat(), editor.currentTrack().tuning(), showBassInChordName, FingeringMemory.userMemory());
        ChordLibrary library = ChordLibrary.userLibrary();
        Panel panel = new Panel(model, library, editor, player);
        if (DialogShell.ask(parent, "Acorde", panel)) {
            model.applyTo(editor);
        }
    }

    /** El armado de la ventana, separado del dialogo para poder probarlo. */
    static final class Panel extends JPanel {

        private final ChordEditorModel model;
        private final ChordLibrary library;
        private final Editor editor;
        private final Player player;

        private final JComboBox<PitchClass> roots = new JComboBox<>();
        private final JComboBox<ChordType> types = new JComboBox<>(ChordType.values());
        private final JComboBox<PitchClass> basses = new JComboBox<>();
        private final JComboBox<ChordComplexity> complexities = new JComboBox<>(ChordComplexity.values());
        private final JComboBox<BarrePreference> barres = new JComboBox<>(BarrePreference.values());
        private final JTextField name = new JTextField(12);
        private final JCheckBox useDiagram = new JCheckBox("Usar diagrama", true);
        private final JCheckBox showFingering = new JCheckBox("Digitación", true);
        private final ChordDiagramCanvas canvas = new ChordDiagramCanvas();
        private final JScrollBar baseFret = new JScrollBar(JScrollBar.VERTICAL, 1, 1, 1, Tuning.MAX_FRET + 1);
        private final JPanel omitChecks = new JPanel();
        private final DefaultListModel<ChordDiagram> candidates = new DefaultListModel<>();
        private final DefaultListModel<Chord> alternatives = new DefaultListModel<>();
        private final DefaultListModel<ChordDiagram> used = new DefaultListModel<>();
        private final DefaultListModel<ChordDiagram> saved = new DefaultListModel<>();
        private boolean updating;

        Panel(ChordEditorModel model, ChordLibrary library, Editor editor, Player player) {
            this.model = model;
            this.library = library;
            this.editor = editor;
            this.player = player;
            PitchClasses.chromatic().forEach(pitchClass -> {
                roots.addItem(pitchClass);
                basses.addItem(pitchClass);
            });
            omitChecks.setLayout(new BoxLayout(omitChecks, BoxLayout.Y_AXIS));
            omitChecks.setBorder(BorderFactory.createTitledBorder("Omitir"));
            setLayout(new BorderLayout(10, 10));
            add(constructionZone(), BorderLayout.WEST);
            add(mainDiagramZone(), BorderLayout.CENTER);
            add(listsZone(), BorderLayout.EAST);
            add(candidatesZone(), BorderLayout.SOUTH);
            wireUp();
            refresh();
        }

        /** Zona A: el acorde se arma eligiendo su nombre. */
        private JPanel constructionZone() {
            JPanel zone = titled("Acorde");
            zone.add(labelled("Fundamental", roots));
            zone.add(labelled("Tipo", types));
            zone.add(labelled("Bajo", basses));
            zone.add(labelled("Posiciones", complexities));
            zone.add(labelled("Cejilla", barres));
            return zone;
        }

        /** Zona B: el diagrama que se va a escribir en la partitura. */
        private JPanel mainDiagramZone() {
            JPanel zone = new JPanel(new BorderLayout(6, 6));
            zone.setBorder(BorderFactory.createTitledBorder("Diagrama"));
            zone.add(name, BorderLayout.NORTH);
            zone.add(canvas, BorderLayout.CENTER);
            zone.add(baseFret, BorderLayout.WEST);
            zone.add(omitChecks, BorderLayout.EAST);
            zone.add(bottomControls(), BorderLayout.SOUTH);
            return zone;
        }

        private JPanel bottomControls() {
            JPanel controls = new JPanel(new GridLayout(0, 1, 4, 4));
            controls.add(useDiagram);
            controls.add(showFingering);
            JButton listen = new JButton("Escuchar");
            listen.addActionListener(event -> ChordSound.play(
                    model.current(), model.tuning(), player, editor.currentTrack().channel().program()));
            controls.add(listen);
            JButton clear = new JButton("Sacar el acorde del beat");
            clear.addActionListener(event -> {
                editor.setChord(null);
                model.setUseDiagram(false);
            });
            controls.add(clear);
            return controls;
        }

        /** Listas D, E y F: nombres alternativos, acordes usados y biblioteca. */
        private JPanel listsZone() {
            JPanel zone = new JPanel();
            zone.setLayout(new BoxLayout(zone, BoxLayout.Y_AXIS));
            zone.add(namedList("Nombres alternativos", new JList<>(alternatives), 90));
            zone.add(Box.createVerticalStrut(6));
            zone.add(namedList("Usados en la pista", diagramList(used), 90));
            zone.add(Box.createVerticalStrut(6));
            zone.add(libraryZone());
            return zone;
        }

        private JPanel libraryZone() {
            JPanel zone = new JPanel(new BorderLayout(4, 4));
            JList<ChordDiagram> list = diagramList(saved);
            zone.add(namedList("Biblioteca", list, 90), BorderLayout.CENTER);
            JPanel buttons = new JPanel(new GridLayout(1, 0, 4, 0));
            JButton add = new JButton("+");
            add.addActionListener(event -> {
                library.add(model.current());
                refreshLists();
            });
            JButton remove = new JButton("−");
            remove.addActionListener(event -> {
                if (list.getSelectedIndex() >= 0) {
                    library.remove(list.getSelectedIndex());
                    refreshLists();
                }
            });
            JButton update = new JButton("Actualizar");
            update.addActionListener(event -> {
                if (list.getSelectedIndex() >= 0) {
                    library.update(list.getSelectedIndex(), model.current());
                    refreshLists();
                }
            });
            JButton sort = new JButton("Ordenar");
            sort.addActionListener(event -> {
                library.sortByName();
                refreshLists();
            });
            buttons.add(add);
            buttons.add(remove);
            buttons.add(update);
            buttons.add(sort);
            zone.add(buttons, BorderLayout.SOUTH);
            return zone;
        }

        /** Zona C: todas las posiciones posibles del acorde construido. */
        private JScrollPane candidatesZone() {
            JList<ChordDiagram> list = diagramList(candidates);
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(1);
            list.addListSelectionListener(event -> {
                if (!updating && list.getSelectedValue() != null) {
                    model.pickCandidate(list.getSelectedValue());
                    refresh();
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createTitledBorder("Posiciones"));
            scroll.setPreferredSize(new Dimension(640, 70));
            return scroll;
        }

        private void wireUp() {
            roots.addActionListener(event -> whenSelecting(() -> model.selectRoot((PitchClass) roots.getSelectedItem())));
            types.addActionListener(event -> whenSelecting(() -> model.selectType((ChordType) types.getSelectedItem())));
            basses.addActionListener(event -> whenSelecting(() -> model.selectBass((PitchClass) basses.getSelectedItem())));
            complexities.addActionListener(event ->
                    whenSelecting(() -> model.selectComplexity((ChordComplexity) complexities.getSelectedItem())));
            barres.addActionListener(event ->
                    whenSelecting(() -> model.selectBarrePreference((BarrePreference) barres.getSelectedItem())));
            name.addActionListener(event -> model.setCustomName(name.getText()));
            useDiagram.addActionListener(event -> model.setUseDiagram(useDiagram.isSelected()));
            showFingering.addActionListener(event -> model.setShowFingering(showFingering.isSelected()));
            baseFret.addAdjustmentListener(event -> {
                if (!updating) {
                    model.setBaseFret(baseFret.getValue());
                    refresh();
                }
            });
            canvas.onFretClick((string, fret) -> {
                model.toggleFret(string, fret);
                refresh();
            });
            canvas.onHeaderClick(string -> {
                model.toggleOpenOrMuted(string);
                refresh();
            });
            canvas.onFingerClick(string -> {
                model.cycleFinger(string);
                refresh();
            });
        }

        private void whenSelecting(Runnable change) {
            if (updating) {
                return;
            }
            change.run();
            refresh();
        }

        private void refresh() {
            updating = true;
            roots.setSelectedItem(model.selection().root());
            types.setSelectedItem(model.selection().type());
            basses.setSelectedItem(model.selection().bass());
            complexities.setSelectedItem(model.selection().complexity());
            barres.setSelectedItem(model.barrePreference());
            name.setText(model.current().name());
            useDiagram.setSelected(model.useDiagram());
            showFingering.setSelected(model.showFingering());
            baseFret.setValue(model.current().baseFret());
            canvas.show(model.current(), model.tuning());
            refreshOmitChecks();
            refreshLists();
            updating = false;
        }

        /** Los casilleros 1', 3', 5'... cambian con el tipo de acorde, asi que se arman de nuevo. */
        private void refreshOmitChecks() {
            omitChecks.removeAll();
            for (Interval tone : model.omittableTones()) {
                JCheckBox check = new JCheckBox(tone.degreeNumber() + "'");
                check.setSelected(model.omittedTones().contains(tone));
                check.addActionListener(event -> {
                    model.setToneOmitted(tone, check.isSelected());
                    refresh();
                });
                omitChecks.add(check);
            }
            omitChecks.revalidate();
            omitChecks.repaint();
        }

        private void refreshLists() {
            fill(candidates, model.candidates());
            fill(alternatives, model.alternativeNames());
            fill(used, TrackChords.usedIn(editor.currentTrack()));
            fill(saved, library.all());
        }

        private static <T> void fill(DefaultListModel<T> listModel, List<T> values) {
            listModel.clear();
            values.forEach(listModel::addElement);
        }

        /**
         * Una lista de diagramas: se ve el nombre y la forma, y mientras el mouse esta encima de
         * un elemento aparece el diagrama en una ventanita de ayuda, como describe el manual.
         */
        private JList<ChordDiagram> diagramList(DefaultListModel<ChordDiagram> listModel) {
            JList<ChordDiagram> list = new JList<>(listModel) {

                private ChordDiagram hovered;

                @Override
                public String getToolTipText(MouseEvent event) {
                    int index = locationToIndex(event.getPoint());
                    if (index < 0 || !getCellBounds(index, index).contains(event.getPoint())) {
                        hovered = null;
                        return null;
                    }
                    hovered = getModel().getElementAt(index);
                    return hovered.name();
                }

                @Override
                public JToolTip createToolTip() {
                    JToolTip tip = new JToolTip();
                    tip.setLayout(new BorderLayout());
                    if (hovered != null) {
                        ChordDiagramCanvas preview = new ChordDiagramCanvas();
                        preview.setPreferredSize(new Dimension(110, 130));
                        preview.show(hovered, model.tuning());
                        tip.add(preview, BorderLayout.CENTER);
                    }
                    return tip;
                }
            };
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setToolTipText("");
            list.setCellRenderer(new javax.swing.DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> owner, Object value, int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(owner, value, index, selected, focused);
                    if (value instanceof ChordDiagram diagram) {
                        setText(diagram.name() + "  " + shape(diagram));
                    }
                    return this;
                }
            });
            return list;
        }

        /** La forma del acorde escrita como la escribe un guitarrista: x32010. */
        private static String shape(ChordDiagram diagram) {
            StringBuilder shape = new StringBuilder();
            for (int string = diagram.stringCount(); string >= 1; string--) {
                int fret = diagram.fretOfString(string);
                shape.append(fret == ChordDiagram.MUTED ? "x" : Integer.toHexString(fret));
            }
            return shape.toString();
        }

        private static JScrollPane namedList(String title, JList<?> list, int height) {
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createTitledBorder(title));
            scroll.setPreferredSize(new Dimension(190, height));
            return scroll;
        }

        private static JPanel titled(String title) {
            JPanel zone = new JPanel();
            zone.setLayout(new BoxLayout(zone, BoxLayout.Y_AXIS));
            zone.setBorder(BorderFactory.createTitledBorder(title));
            return zone;
        }

        private static JPanel labelled(String label, Component field) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.add(new JLabel(label), BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            return row;
        }
    }
}

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
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;

public final class ChordDialog {

    private ChordDialog() {
    }

    public static void show(Component parent, Editor editor, Player player, boolean showBassInChordName) {
        ChordEditorModel model = ChordEditorModel.forBeat(
                editor.currentBeat(), editor.currentTrack().tuning(), showBassInChordName, FingeringMemory.userMemory());
        ChordLibrary library = ChordLibrary.userLibrary();
        Panel panel = new Panel(model, library, editor, player);
        if (DialogShell.ask(parent, Texts.get("views.ChordDialog.title"), panel)) {
            model.applyTo(editor);
        }
    }

    static final class Panel extends JPanel {

        private final ChordEditorModel model;
        private final ChordLibrary library;
        private final Editor editor;
        private final Player player;

        private final JComboBox<PitchClass> roots = new JComboBox<>();
        private final JComboBox<ChordType> types = new JComboBox<>(ChordType.values());
        private final JComboBox<Interval> inversions = new JComboBox<>();
        private final JComboBox<PitchClass> basses = new JComboBox<>();
        private final Map<ChordComplexity, JRadioButton> complexityButtons = new EnumMap<>(ChordComplexity.class);
        private final Map<BarrePreference, JRadioButton> barreButtons = new EnumMap<>(BarrePreference.class);
        private final JTextField name = new JTextField(12);
        private final JCheckBox useDiagram = new JCheckBox(Texts.get("views.ChordDialog.useDiagram"), true);
        private final JCheckBox showFingering = new JCheckBox(Texts.get("views.ChordDialog.fingering"), true);
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
            roots.setRenderer(new LabeledListCellRenderer());
            basses.setRenderer(new LabeledListCellRenderer());
            types.setRenderer(new LabeledListCellRenderer());
            inversions.setRenderer(inversionRenderer());
            name.getAccessibleContext().setAccessibleName(Texts.get("views.ChordDialog.chordName"));
            name.setToolTipText(Texts.get("views.ChordDialog.chordName"));
            baseFret.getAccessibleContext().setAccessibleName(Texts.get("views.ChordDialog.baseFret"));
            baseFret.setToolTipText(Texts.get("views.ChordDialog.baseFret"));
            omitChecks.setLayout(new BoxLayout(omitChecks, BoxLayout.Y_AXIS));
            omitChecks.setBorder(BorderFactory.createTitledBorder(Texts.get("views.ChordDialog.omit")));
            setLayout(new BorderLayout(10, 10));
            add(constructionZone(), BorderLayout.WEST);
            add(mainDiagramZone(), BorderLayout.CENTER);
            add(listsZone(), BorderLayout.EAST);
            add(candidatesZone(), BorderLayout.SOUTH);
            wireUp();
            refresh();
        }

        private JPanel constructionZone() {
            JPanel zone = titled(Texts.get("views.ChordDialog.chordSection"));
            zone.add(labelled(Texts.get("views.ChordDialog.fundamental"), roots));
            zone.add(labelled(Texts.get("views.ChordDialog.type"), types));
            zone.add(labelled(Texts.get("views.ChordDialog.inversion"), inversions));
            zone.add(labelled(Texts.get("views.ChordDialog.bass"), basses));
            zone.add(labelled(Texts.get("views.ChordDialog.positions"), complexityChoice()));
            zone.add(labelled(Texts.get("views.ChordDialog.barre"), barreChoice()));
            return zone;
        }

        private JPanel complexityChoice() {
            JPanel choice = new JPanel(new GridLayout(0, 1));
            ButtonGroup group = new ButtonGroup();
            for (ChordComplexity complexity : ChordComplexity.values()) {
                JRadioButton radio = new JRadioButton(Labels.of(complexity));
                radio.addActionListener(event -> whenSelecting(() -> model.selectComplexity(complexity)));
                group.add(radio);
                complexityButtons.put(complexity, radio);
                choice.add(radio);
            }
            return choice;
        }

        private JPanel barreChoice() {
            JPanel choice = new JPanel(new GridLayout(0, 1));
            ButtonGroup group = new ButtonGroup();
            for (BarrePreference preference : BarrePreference.values()) {
                JRadioButton radio = new JRadioButton(Labels.of(preference));
                radio.addActionListener(event -> whenSelecting(() -> model.selectBarrePreference(preference)));
                group.add(radio);
                barreButtons.put(preference, radio);
                choice.add(radio);
            }
            return choice;
        }

        private JPanel mainDiagramZone() {
            JPanel zone = new JPanel(new BorderLayout(6, 6));
            zone.setBorder(BorderFactory.createTitledBorder(Texts.get("views.ChordDialog.diagramSection")));
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
            JButton listen = new JButton(Texts.get("views.ChordDialog.listen"));
            listen.addActionListener(event -> ChordSound.play(
                    model.current(), model.tuning(), player, editor.currentTrack().channel().program()));
            controls.add(listen);
            JButton clear = new JButton(Texts.get("views.ChordDialog.clearChord"));
            clear.addActionListener(event -> {
                editor.setChord(null);
                model.setUseDiagram(false);
            });
            controls.add(clear);
            return controls;
        }

        private JPanel listsZone() {
            JPanel zone = new JPanel();
            zone.setLayout(new BoxLayout(zone, BoxLayout.Y_AXIS));
            JList<Chord> alternativeNames = new JList<>(alternatives);
            alternativeNames.setCellRenderer(new LabeledListCellRenderer());
            zone.add(namedList(Texts.get("views.ChordDialog.alternativeNames"), alternativeNames, 90));
            zone.add(Box.createVerticalStrut(6));
            zone.add(namedList(Texts.get("views.ChordDialog.usedInTrack"), diagramList(used), 90));
            zone.add(Box.createVerticalStrut(6));
            zone.add(libraryZone());
            return zone;
        }

        private JPanel libraryZone() {
            JPanel zone = new JPanel(new BorderLayout(4, 4));
            JList<ChordDiagram> list = diagramList(saved);
            zone.add(namedList(Texts.get("views.ChordDialog.library"), list, 90), BorderLayout.CENTER);
            JPanel buttons = new JPanel(new GridLayout(1, 0, 4, 0));
            JButton add = new JButton("+");
            add.getAccessibleContext().setAccessibleName(Texts.get("views.ChordDialog.addToLibrary"));
            add.addActionListener(event -> {
                library.add(model.current());
                refreshLists();
            });
            JButton remove = new JButton("−");
            remove.getAccessibleContext().setAccessibleName(Texts.get("views.ChordDialog.removeFromLibrary"));
            remove.addActionListener(event -> {
                if (list.getSelectedIndex() >= 0) {
                    library.remove(list.getSelectedIndex());
                    refreshLists();
                }
            });
            JButton update = new JButton(Texts.get("views.ChordDialog.update"));
            update.addActionListener(event -> {
                if (list.getSelectedIndex() >= 0) {
                    library.update(list.getSelectedIndex(), model.current());
                    refreshLists();
                }
            });
            JButton sort = new JButton(Texts.get("views.ChordDialog.sort"));
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

        private JScrollPane candidatesZone() {
            JList<ChordDiagram> list = diagramList(candidates);
            list.getAccessibleContext().setAccessibleName(Texts.get("views.ChordDialog.positions"));
            list.setToolTipText(Texts.get("views.ChordDialog.positions"));
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(1);
            list.addListSelectionListener(event -> {
                if (!updating && list.getSelectedValue() != null) {
                    model.pickCandidate(list.getSelectedValue());
                    refresh();
                }
            });
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createTitledBorder(Texts.get("views.ChordDialog.positions")));
            scroll.setPreferredSize(new Dimension(640, 70));
            return scroll;
        }

        private void wireUp() {
            roots.addActionListener(event -> whenSelecting(() -> model.selectRoot((PitchClass) roots.getSelectedItem())));
            types.addActionListener(event -> whenSelecting(() -> model.selectType((ChordType) types.getSelectedItem())));
            inversions.addActionListener(event -> whenSelecting(this::selectChosenInversion));
            basses.addActionListener(event -> whenSelecting(() -> model.selectBass((PitchClass) basses.getSelectedItem())));
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

        private void selectChosenInversion() {
            Interval degree = (Interval) inversions.getSelectedItem();
            if (degree != null) {
                model.selectInversion(degree);
            }
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
            refreshInversions();
            basses.setSelectedItem(model.selection().bass());
            complexityButtons.get(model.selection().complexity()).setSelected(true);
            barreButtons.get(model.barrePreference()).setSelected(true);
            name.setText(model.current().name());
            useDiagram.setSelected(model.useDiagram());
            showFingering.setSelected(model.showFingering());
            baseFret.setValue(model.current().baseFret());
            canvas.show(model.current(), model.tuning());
            refreshOmitChecks();
            refreshLists();
            updating = false;
        }

        private void refreshInversions() {
            inversions.removeAllItems();
            model.omittableTones().forEach(inversions::addItem);
            inversions.setSelectedItem(currentInversion());
        }

        private Interval currentInversion() {
            PitchClass root = model.selection().root();
            PitchClass bass = model.selection().bass();
            return model.omittableTones().stream()
                    .filter(degree -> degree.from(root).equals(bass))
                    .findFirst()
                    .orElse(null);
        }

        private javax.swing.ListCellRenderer<Object> inversionRenderer() {
            return new javax.swing.DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> owner, Object value, int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(owner, value, index, selected, focused);
                    if (value instanceof Interval degree) {
                        setText(inversionLabel(degree));
                    }
                    return this;
                }
            };
        }

        private String inversionLabel(Interval degree) {
            if (degree == Interval.ROOT) {
                return Texts.get("views.ChordDialog.fundamental");
            }
            PitchClass note = degree.from(model.selection().root());
            return note.name() + " (" + Labels.of(degree) + ")";
        }

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

        private static String shape(ChordDiagram diagram) {
            StringBuilder shape = new StringBuilder();
            for (int string = diagram.stringCount(); string >= 1; string--) {
                int fret = diagram.fretOfString(string);
                shape.append(fret == ChordDiagram.MUTED ? "x" : Integer.toHexString(fret));
            }
            return shape.toString();
        }

        private static JScrollPane namedList(String title, JList<?> list, int height) {
            list.getAccessibleContext().setAccessibleName(title);
            list.setToolTipText(title);
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
            JLabel text = new JLabel(label);
            if (field instanceof javax.swing.JComponent labeledField) {
                text.setLabelFor(labeledField);
            }
            row.add(text, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            return row;
        }
    }
}

package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import javax.swing.JTabbedPane;

public final class NoteEffectsDialog {

    private NoteEffectsDialog() {
    }

    public static final String BEND = Texts.get("edit_dialogs.NoteEffectsDialog.bend");
    public static final String TREMOLO_BAR = Texts.get("edit_dialogs.NoteEffectsDialog.tremoloBar");
    public static final String GRACE_NOTE = Texts.get("edit_dialogs.NoteEffectsDialog.graceNote");
    public static final String STROKE = Texts.get("edit_dialogs.NoteEffectsDialog.stroke");
    public static final String TRILL = Texts.get("edit_dialogs.NoteEffectsDialog.trill");
    public static final String TREMOLO_PICKING = Texts.get("edit_dialogs.NoteEffectsDialog.tremoloPicking");
    public static final String HARMONICS = Texts.get("edit_dialogs.NoteEffectsDialog.harmonics");

    public static void show(Component parent, Editor editor) {
        show(parent, editor, BEND);
    }

    public static void show(Component parent, Editor editor, String openOn) {
        NoteEffects noteEffects = editor.currentNote().map(note -> note.effects()).orElse(NoteEffects.none());
        BeatEffects beatEffects = editor.currentBeat().effects();

        TogglableEffectPanel<BendPanel> bendTab = new TogglableEffectPanel<>(
                noteEffects.bend().isPresent(), new BendPanel(noteEffects.bend().orElse(defaultBend())));
        TogglableEffectPanel<BendPanel> tremoloBarTab = new TogglableEffectPanel<>(
                beatEffects.tremoloBar().isPresent(),
                new BendPanel(beatEffects.tremoloBar().orElse(defaultTremoloBar()), BendType.tremoloBarTypes()));
        TogglableEffectPanel<GraceNotePanel> graceTab = new TogglableEffectPanel<>(
                noteEffects.grace().isPresent(), new GraceNotePanel(noteEffects.grace().orElse(GraceNote.before(0))));
        TogglableEffectPanel<StrokePanel> strokeTab = new TogglableEffectPanel<>(
                beatEffects.stroke().isPresent(), new StrokePanel(beatEffects.stroke().orElse(Stroke.of(StrokeDirection.DOWN))));
        TogglableEffectPanel<TrillPanel> trillTab = new TogglableEffectPanel<>(
                noteEffects.trill().isPresent(), new TrillPanel(noteEffects.trill().orElse(Trill.to(0))));
        TogglableEffectPanel<TremoloPickingPanel> tremoloPickingTab = new TogglableEffectPanel<>(
                noteEffects.tremoloPicking().isPresent(),
                new TremoloPickingPanel(noteEffects.tremoloPicking().orElse(TremoloPicking.at(NoteValue.SIXTEENTH))));
        TogglableEffectPanel<HarmonicPanel> harmonicTab = new TogglableEffectPanel<>(
                noteEffects.harmonic().isPresent(), new HarmonicPanel(noteEffects.harmonic().orElse(HarmonicType.NATURAL)));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(BEND, bendTab);
        tabs.addTab(TREMOLO_BAR, tremoloBarTab);
        tabs.addTab(GRACE_NOTE, graceTab);
        tabs.addTab(STROKE, strokeTab);
        tabs.addTab(TRILL, trillTab);
        tabs.addTab(TREMOLO_PICKING, tremoloPickingTab);
        tabs.addTab(HARMONICS, harmonicTab);

        selectTab(tabs, openOn);

        boolean accepted = DialogShell.ask(parent, Texts.get("edit_dialogs.NoteEffectsDialog.title"), tabs);
        if (!accepted) {
            return;
        }
        editor.setBend(bendTab.isActive() ? bendTab.content().toBend() : null);
        editor.setTremoloBar(tremoloBarTab.isActive() ? tremoloBarTab.content().toBend() : null);
        editor.setGraceNote(graceTab.isActive() ? graceTab.content().toGraceNote() : null);
        editor.setStroke(strokeTab.isActive() ? strokeTab.content().toStroke() : null);
        editor.setTrill(trillTab.isActive() ? trillTab.content().toTrill() : null);
        editor.setTremoloPicking(tremoloPickingTab.isActive() ? tremoloPickingTab.content().toTremoloPicking() : null);
        editor.setHarmonic(harmonicTab.isActive() ? harmonicTab.content().toHarmonicType() : null);
    }

    private static Bend defaultBend() {
        return Bend.of(BendType.BEND, 4);
    }

    private static Bend defaultTremoloBar() {
        return Bend.of(BendType.DIP, 4);
    }

    private static void selectTab(JTabbedPane tabs, String title) {
        int index = tabs.indexOfTab(title);
        if (index >= 0) {
            tabs.setSelectedIndex(index);
        }
    }
}

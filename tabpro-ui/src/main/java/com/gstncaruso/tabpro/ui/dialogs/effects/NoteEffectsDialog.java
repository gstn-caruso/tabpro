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
import java.awt.Component;
import javax.swing.JTabbedPane;

/** La ventana de efectos de nota: bend, palanca, adorno, rasgueo, trino, tremolo de pua y armonicos. */
public final class NoteEffectsDialog {

    private NoteEffectsDialog() {
    }

    public static void show(Component parent, Editor editor) {
        NoteEffects noteEffects = editor.currentNote().map(note -> note.effects()).orElse(NoteEffects.none());
        BeatEffects beatEffects = editor.currentBeat().effects();

        TogglableEffectPanel<BendPanel> bendTab = new TogglableEffectPanel<>(
                noteEffects.bend().isPresent(), new BendPanel(noteEffects.bend().orElse(defaultBend())));
        TogglableEffectPanel<BendPanel> tremoloBarTab = new TogglableEffectPanel<>(
                beatEffects.tremoloBar().isPresent(), new BendPanel(beatEffects.tremoloBar().orElse(defaultBend())));
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
        tabs.addTab("Bend", bendTab);
        tabs.addTab("Palanca", tremoloBarTab);
        tabs.addTab("Nota de adorno", graceTab);
        tabs.addTab("Rasgueo", strokeTab);
        tabs.addTab("Trino", trillTab);
        tabs.addTab("Tremolo de pua", tremoloPickingTab);
        tabs.addTab("Armonicos", harmonicTab);

        boolean accepted = DialogShell.ask(parent, "Efectos de nota", tabs);
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
}

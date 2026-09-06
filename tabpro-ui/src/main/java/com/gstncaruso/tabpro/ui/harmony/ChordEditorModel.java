package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordDiagramGenerator;
import com.gstncaruso.tabpro.core.harmony.ChordNamer;
import com.gstncaruso.tabpro.core.harmony.ChordTone;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Todo lo que decide la ventana de acordes, sin Swing: que diagrama esta armado (zona B), que
 * posiciones ofrece la busqueda (zona C), que otros nombres explican al principal (zona D), y si
 * el usuario paso a armar el diagrama a mano ("Personalizado", con el nombre para escribir).
 */
public final class ChordEditorModel {

    private final Tuning tuning;
    private final boolean showBassInChordName;
    private final FingeringMemory fingeringMemory;
    private ChordSelection selection;
    private BarrePreference barrePreference = BarrePreference.ANY;
    private Set<Interval> omittedTones = Set.of();
    private ChordDiagram current;
    private List<ChordDiagram> candidates = List.of();
    private List<Chord> alternativeNames = List.of();
    private boolean custom;
    private boolean useDiagram = true;
    private boolean showFingering = true;

    public ChordEditorModel(Tuning tuning) {
        this(tuning, true, FingeringMemory.userMemory());
    }

    public ChordEditorModel(Tuning tuning, boolean showBassInChordName, FingeringMemory fingeringMemory) {
        this(tuning, ChordSelection.initial(), showBassInChordName, fingeringMemory);
    }

    private ChordEditorModel(
            Tuning tuning, ChordSelection selection, boolean showBassInChordName, FingeringMemory fingeringMemory) {
        this.tuning = tuning;
        this.selection = selection;
        this.showBassInChordName = showBassInChordName;
        this.fingeringMemory = fingeringMemory;
        rebuildFromSelection();
    }

    /**
     * El estado con el que abre la ventana: si el beat ya tiene un acorde lo carga, si no pero
     * tiene notas arma el diagrama con ellas, y si esta vacio arranca con la seleccion inicial.
     */
    public static ChordEditorModel forBeat(Beat beat, Tuning tuning) {
        return forBeat(beat, tuning, true, FingeringMemory.userMemory());
    }

    public static ChordEditorModel forBeat(
            Beat beat, Tuning tuning, boolean showBassInChordName, FingeringMemory fingeringMemory) {
        if (beat.effects().chord().isPresent()) {
            ChordEditorModel model = new ChordEditorModel(tuning, showBassInChordName, fingeringMemory);
            model.loadDiagram(beat.effects().chord().get());
            return model;
        }
        if (!beat.isRest() && !beat.notes().isEmpty()) {
            ChordEditorModel model = new ChordEditorModel(tuning, showBassInChordName, fingeringMemory);
            model.loadDiagram(ChordDiagrams.fromBeat(beat, tuning));
            return model;
        }
        return new ChordEditorModel(tuning, showBassInChordName, fingeringMemory);
    }

    // ---- consultas ----------------------------------------------------

    public Tuning tuning() {
        return tuning;
    }

    public ChordSelection selection() {
        return selection;
    }

    public BarrePreference barrePreference() {
        return barrePreference;
    }

    public ChordDiagram current() {
        return current;
    }

    public List<ChordDiagram> candidates() {
        return candidates;
    }

    public List<Chord> alternativeNames() {
        return alternativeNames;
    }

    public boolean isCustom() {
        return custom;
    }

    public boolean useDiagram() {
        return useDiagram;
    }

    public boolean showFingering() {
        return showFingering;
    }

    /** Los tonos del acorde elegido que se pueden tildar para omitir: 1', 3', 5'... */
    public List<Interval> omittableTones() {
        return selection.chord().type().tones().stream().map(ChordTone::interval).toList();
    }

    /** Los tonos que el usuario tildo para que no haga falta que suenen. */
    public Set<Interval> omittedTones() {
        return omittedTones;
    }

    /** El diagrama tal como hay que escribirlo en el beat: respeta "usar diagrama" y "digitacion". */
    public ChordDiagram result() {
        ChordDiagram diagram = current.shownAs(useDiagram);
        if (!showFingering) {
            diagram = diagram.withFingering(blankFingering());
        }
        return diagram;
    }

    // ---- zona A: construccion ------------------------------------------

    public void selectRoot(PitchClass root) {
        applySelection(selection.withRoot(root));
    }

    public void selectType(ChordType type) {
        applySelection(selection.withType(type));
    }

    public void selectBass(PitchClass bass) {
        applySelection(selection.withBass(bass));
    }

    public void selectComplexity(ChordComplexity complexity) {
        applySelection(selection.withComplexity(complexity));
    }

    public void selectBarrePreference(BarrePreference preference) {
        this.barrePreference = preference;
        rebuildFromSelection();
    }

    /** Tildar o destildar un casillero 1', 3', 5'... de la zona B. */
    public void setToneOmitted(Interval tone, boolean omitted) {
        Set<Interval> updated = new LinkedHashSet<>(omittedTones);
        if (omitted) {
            updated.add(tone);
        } else {
            updated.remove(tone);
        }
        omittedTones = updated;
        rebuildFromSelection();
    }

    private void applySelection(ChordSelection newSelection) {
        this.selection = newSelection;
        this.custom = false;
        this.omittedTones = Set.of();
        rebuildFromSelection();
    }

    private void rebuildFromSelection() {
        candidates = candidatesFor(selection.chord());
        current = candidates.isEmpty() ? ChordDiagram.justTheName(nameFor(selection.chord())) : candidates.get(0);
        recomputeAlternativeNames();
    }

    private List<ChordDiagram> candidatesFor(Chord chord) {
        return ChordDiagramGenerator
                .generate(chord, tuning, ChordDiagramGenerator.DEFAULT_MAX_SPAN, selection.complexity(), omittedTones)
                .stream()
                .filter(barrePreference::accepts)
                .map(diagram -> diagram.withName(nameFor(chord)))
                .map(this::withRememberedFingering)
                .toList();
    }

    /** Si esta forma ya se digito a mano alguna vez, se usa esa digitacion en vez de la automatica. */
    private ChordDiagram withRememberedFingering(ChordDiagram diagram) {
        return fingeringMemory.fingeringFor(diagram.shape()).map(diagram::withFingering).orElse(diagram);
    }

    private String nameFor(Chord chord) {
        return chord.name(showBassInChordName);
    }

    // ---- zona C: posiciones ---------------------------------------------

    public void pickCandidate(ChordDiagram diagram) {
        current = diagram;
        custom = false;
        recomputeAlternativeNames();
    }

    // ---- zona D: nombres alternativos ------------------------------------

    public void pickAlternativeName(Chord chord) {
        selection = new ChordSelection(chord.root(), chord.type(), chord.bass(), selection.complexity());
        custom = false;
        omittedTones = Set.of();
        current = current.withName(nameFor(chord));
        candidates = candidatesFor(selection.chord());
        recomputeAlternativeNames();
    }

    private void recomputeAlternativeNames() {
        alternativeNames = ChordNamer.namesFor(current, tuning);
    }

    // ---- zona B: el diagrama a mano ---------------------------------------

    /** Un clic en una cuerda a ese traste: la agrega, o la saca si ya estaba ahi. */
    public void toggleFret(int string, int fret) {
        int already = current.fretOfString(string);
        editDiagram(already == fret ? ChordDiagram.MUTED : fret, string);
    }

    /** El circulo/cruz de arriba de cada cuerda: alterna entre al aire y muda. */
    public void toggleOpenOrMuted(int string) {
        editDiagram(current.fretOfString(string) == 0 ? ChordDiagram.MUTED : 0, string);
    }

    public void setFinger(int string, Finger finger) {
        List<Finger> fingering = mutableFingering();
        fingering.set(string - 1, finger);
        current = current.withFingering(fingering);
        fingeringMemory.remember(current.shape(), fingering);
    }

    /**
     * El clic en el numero debajo del diagrama: pasa al dedo siguiente (indice, medio, anular,
     * menique, pulgar) y de ahi vuelve a "sin dedo". Una cuerda al aire o muda no se digita.
     */
    public void cycleFinger(int string) {
        if (current.fretOfString(string) <= 0) {
            return;
        }
        setFinger(string, nextFinger(current.fingerOfString(string)));
    }

    private static Finger nextFinger(Optional<Finger> current) {
        Finger[] order = Finger.values();
        if (current.isEmpty()) {
            return order[0];
        }
        int next = current.get().ordinal() + 1;
        return next >= order.length ? null : order[next];
    }

    public void setBaseFret(int baseFret) {
        current = ChordDiagrams.withBaseFret(current, baseFret);
    }

    /** Solo tiene efecto en modo personalizado: ahi el nombre lo escribe el usuario. */
    public void setCustomName(String name) {
        current = current.withName(name);
    }

    public void setUseDiagram(boolean useDiagram) {
        this.useDiagram = useDiagram;
    }

    public void setShowFingering(boolean showFingering) {
        this.showFingering = showFingering;
    }

    private void editDiagram(int fret, int string) {
        current = current.withFretOnString(string, fret).withName("");
        custom = true;
        recomputeAlternativeNames();
    }

    private List<Finger> mutableFingering() {
        List<Finger> fingering = new ArrayList<>(current.stringCount());
        for (int i = 0; i < current.stringCount(); i++) {
            fingering.add(current.fingerOfString(i + 1).orElse(null));
        }
        return fingering;
    }

    private List<Finger> blankFingering() {
        return new ArrayList<>(Collections.nCopies(current.stringCount(), null));
    }

    private void loadDiagram(ChordDiagram diagram) {
        current = diagram;
        List<Chord> names = ChordNamer.namesFor(diagram, tuning);
        if (names.isEmpty()) {
            custom = true;
            alternativeNames = List.of();
        } else {
            Chord best = names.get(0);
            selection = new ChordSelection(best.root(), best.type(), best.bass(), selection.complexity());
            custom = false;
            current = current.withName(diagram.name().isBlank() ? nameFor(best) : diagram.name());
            candidates = candidatesFor(selection.chord());
            alternativeNames = names;
        }
    }

    // ---- aplicar ------------------------------------------------------

    /**
     * Graba el diagrama en el beat actual del editor. Si el beat no tenia notas propias,
     * ademas escribe las del diagrama -asi el pentagrama y la tablatura suenan igual que lo
     * que se armo aca.
     */
    public void applyTo(Editor editor) {
        ChordDiagram toWrite = result();
        boolean beatHadNoNotes = editor.currentBeat().notes().isEmpty();
        editor.setChord(toWrite);
        if (beatHadNoNotes) {
            writeNotes(editor, toWrite);
        }
    }

    private void writeNotes(Editor editor, ChordDiagram diagram) {
        var cursor = editor.cursor();
        for (int string = 1; string <= diagram.stringCount(); string++) {
            if (diagram.isPlayed(string)) {
                editor.moveTo(cursor.measure(), cursor.beat(), string);
                editor.setFret(diagram.fretOfString(string));
            }
        }
        editor.moveTo(cursor.measure(), cursor.beat(), cursor.string());
    }
}

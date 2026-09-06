package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.ChordFretting;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackSettings;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceNote;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/** La sesion de edicion: la partitura, donde esta parado el cursor y como cambiarla. */
public final class Editor {

    private Score score;
    private Cursor cursor;
    private Cursor selectionAnchor;
    private boolean selectingWholeMeasures;
    private final EditorHistory history = new EditorHistory();
    private final Clipboard clipboard = new Clipboard();
    private final List<EditorListener> listeners = new ArrayList<>();
    private boolean undoEnabled = true;

    public Editor(Score initial) {
        this.score = initial;
        this.cursor = new Cursor(0, 0, 0, 1);
    }

    public Score score() {
        return score;
    }

    public Cursor cursor() {
        return cursor;
    }

    public Clipboard clipboard() {
        return clipboard;
    }

    public Track currentTrack() {
        return score.track(cursor.track());
    }

    public Measure currentMeasure() {
        return currentTrack().measure(cursor.measure());
    }

    public Voice currentVoice() {
        return currentMeasure().voice(cursor.voice());
    }

    public Beat currentBeat() {
        return currentVoice().beat(cursor.beat());
    }

    public Optional<Note> currentNote() {
        return currentBeat().noteOn(cursor.string());
    }

    // ---- notas ------------------------------------------------------------

    public void setFret(int fret) {
        Note existing = currentNote().orElse(new Note(cursor.string(), fret));
        changeCurrentBeat(beat -> beat.withNote(existing.withFret(fret)));
    }

    public void clearNote() {
        changeCurrentBeat(beat -> beat.withoutNoteOn(cursor.string()));
    }

    public void clearBeat() {
        changeCurrentBeat(beat -> Beat.rest(beat.duration()));
    }

    /** Mueve la nota a otra cuerda sin cambiar su altura, como el Alt+flecha del manual. */
    public void moveNoteToString(int string) {
        Optional<Note> note = currentNote();
        Track track = currentTrack();
        if (note.isEmpty() || string < 1 || string > track.stringCount()) {
            return;
        }
        Optional<Note> moved = track.tuning().noteFor(track.tuning().pitchOf(note.get()), string);
        if (moved.isEmpty()) {
            return;
        }
        Note relocated = moved.get().withEffects(note.get().effects()).tied(note.get().tied());
        changeBeatAndCursor(beat -> beat.withoutNoteOn(cursor.string()).withNote(relocated), cursor.onString(string));
    }

    public void moveNoteUpOneString() {
        moveNoteToString(cursor.string() - 1);
    }

    public void moveNoteDownOneString() {
        moveNoteToString(cursor.string() + 1);
    }

    public void transposeNote(int semitones) {
        changeCurrentNote(note -> note.transposed(semitones));
    }

    public void toggleTie() {
        changeCurrentNote(note -> note.tied(!note.tied()));
    }

    /** Liga todas las notas del beat con las del anterior, como el Ctrl+L del manual. */
    public void tieWholeBeat() {
        changeCurrentBeat(beat -> beat.mappingEveryNote(note -> note.tied(true)));
    }

    // ---- figuras ----------------------------------------------------------

    public void lengthenDuration() {
        changeCurrentBeat(beat -> beat.withDuration(beat.duration().longer()));
    }

    public void shortenDuration() {
        changeCurrentBeat(beat -> beat.withDuration(beat.duration().shorter()));
    }

    public void setNoteValue(NoteValue value) {
        changeCurrentBeat(beat -> beat.withDuration(new Duration(value, beat.duration().dotted(), beat.duration().tuplet())));
    }

    public void toggleDot() {
        changeCurrentBeat(beat -> beat.withDuration(beat.duration().toggledDot()));
    }

    public void setTuplet(int enters) {
        changeCurrentBeat(beat -> beat.withDuration(beat.duration().in(Tuplet.of(enters))));
    }

    public void toggleTriplet() {
        toggleTuplet(3);
    }

    /** El manual: cualquier n-tuplet (quintillo, seisillo...) se pone y se saca igual que el
     * tresillo, nomas que con otro numero. */
    public void toggleTuplet(int enters) {
        Tuplet current = currentBeat().duration().tuplet();
        setTuplet(current.enters() == enters ? 1 : enters);
    }

    // ---- efectos de nota --------------------------------------------------

    public void toggleOrnament(Ornament ornament) {
        changeCurrentNote(note -> note.toggling(ornament));
    }

    public void setDynamic(Dynamic dynamic) {
        changeCurrentNote(note -> note.withDynamic(dynamic));
    }

    /** La dinamica del acorde entero, como ofrece el menu Nota del manual. */
    public void setChordDynamic(Dynamic dynamic) {
        changeCurrentBeat(beat -> beat.mappingEveryNote(note -> note.withDynamic(dynamic)));
    }

    public void setBend(Bend bend) {
        changeNoteEffects(effects -> bend == null ? effects.withoutBend() : effects.withBend(bend));
    }

    public void setSlide(SlideType slide) {
        changeNoteEffects(effects -> slide == null ? effects.withoutSlide() : effects.withSlide(slide));
    }

    public void setHarmonic(HarmonicType harmonic) {
        changeNoteEffects(effects -> harmonic == null ? effects.withoutHarmonic() : effects.withHarmonic(harmonic));
    }

    public void setTrill(Trill trill) {
        changeNoteEffects(effects -> trill == null ? effects.withoutTrill() : effects.withTrill(trill));
    }

    public void setTremoloPicking(TremoloPicking picking) {
        changeNoteEffects(effects -> picking == null ? effects.withoutTremoloPicking() : effects.withTremoloPicking(picking));
    }

    public void setGraceNote(GraceNote grace) {
        changeNoteEffects(effects -> grace == null ? effects.withoutGrace() : effects.withGrace(grace));
    }

    /** Cuanto suena la nota respecto de su figura, como pide Nota > Duracion del sonido. */
    public void setSoundDuration(int percent) {
        changeNoteEffects(effects -> effects.withSoundDuration(percent));
    }

    public void setLeftHandFinger(Finger finger) {
        changeNoteEffects(effects -> effects.withLeftHand(finger));
    }

    public void setRightHandFinger(Finger finger) {
        changeNoteEffects(effects -> effects.withRightHand(finger));
    }

    // ---- efectos de beat --------------------------------------------------

    public void setStroke(Stroke stroke) {
        changeBeatEffects(effects -> effects.withStroke(stroke));
    }

    public void setPickstroke(PickstrokeDirection pickstroke) {
        changeBeatEffects(effects -> effects.withPickstroke(pickstroke));
    }

    public void toggleFadeIn() {
        changeBeatEffects(effects -> effects.withFadeIn(!effects.fadeIn()));
    }

    public void toggleTapping() {
        changeBeatEffects(effects -> effects.withTapping(!effects.tapping()));
    }

    public void toggleSlapping() {
        changeBeatEffects(effects -> effects.withSlapping(!effects.slapping()));
    }

    public void togglePopping() {
        changeBeatEffects(effects -> effects.withPopping(!effects.popping()));
    }

    public void toggleWideVibrato() {
        changeBeatEffects(effects -> effects.withWideVibrato(!effects.wideVibrato()));
    }

    public void setTremoloBar(Bend tremoloBar) {
        changeBeatEffects(effects -> effects.withTremoloBar(tremoloBar));
    }

    public void setWah(Wah wah) {
        changeBeatEffects(effects -> effects.withWah(wah));
    }

    public void setText(String text) {
        changeBeatEffects(effects -> effects.withText(text));
    }

    /**
     * El manual (linea 923): el agrupamiento por barra de union es automatico, pero "es posible
     * cambiar a mano las barras... usando el menu Nota". Misma forma que {@link #setLineBreak}:
     * automatico por default, forzado a lo que pida el usuario sobre el beat del cursor.
     */
    public void setBeamBreak(BeamBreak beamBreak) {
        changeBeatEffects(effects -> effects.withBeamBreak(beamBreak));
    }

    /** El manual (linea 923): "...y la direccion de la plica", con la misma forma de arriba. */
    public void setStemOverride(StemOverride stemOverride) {
        changeBeatEffects(effects -> effects.withStemOverride(stemOverride));
    }

    public void setParameterChange(ParameterChange change) {
        changeBeatEffects(effects -> effects.withParameterChange(change));
    }

    public void setChord(ChordDiagram chord) {
        changeBeatEffects(effects -> effects.withChord(chord));
    }

    // ---- beats ------------------------------------------------------------

    public void insertBeat() {
        Beat rest = Beat.rest(currentBeat().duration());
        changeVoiceAndCursor(voice -> voice.withBeatInsertedAt(cursor.beat(), rest), cursor);
    }

    public void deleteBeat() {
        Voice updated = currentVoice().withoutBeatAt(cursor.beat());
        int beat = Math.min(cursor.beat(), updated.beatCount() - 1);
        changeVoiceAndCursor(voice -> voice.withoutBeatAt(cursor.beat()), cursor.onBeat(beat));
    }

    /** Repite el beat hasta llenar el compas, como la tecla C del manual. */
    public void repeatBeatToTheEndOfTheMeasure() {
        Beat beat = currentBeat();
        Measure measure = currentMeasure();
        long room = measure.timeSignature().ticksPerMeasure() - measure.durationTicks();
        List<Beat> copies = new ArrayList<>();
        while (room >= beat.duration().ticks()) {
            copies.add(beat);
            room -= beat.duration().ticks();
        }
        if (copies.isEmpty()) {
            return;
        }
        changeVoiceAndCursor(voice -> {
            Voice grown = voice;
            for (Beat copy : copies) {
                grown = grown.withBeatAppended(copy);
            }
            return grown;
        }, cursor);
    }

    // ---- voces ------------------------------------------------------------

    public void editVoice(VoicePart part) {
        if (currentMeasure().voice(part).isUnused()) {
            Measure started = currentMeasure().withVoice(part, Voice.restingFor(Duration.quarter()));
            change(withCurrentMeasure(started), cursor.onVoice(part).onBeat(0));
            return;
        }
        int beat = Math.min(cursor.beat(), currentMeasure().voice(part).beatCount() - 1);
        moveCursor(cursor.onVoice(part).onBeat(beat));
    }

    // ---- compases ---------------------------------------------------------

    public void insertMeasure() {
        change(score.withMeasureInsertedInEveryTrackAt(cursor.measure()), cursor.onBeat(0));
    }

    public void deleteMeasure() {
        Score next = score.withoutMeasureInEveryTrackAt(cursor.measure());
        int measure = Math.min(cursor.measure(), next.track(cursor.track()).measureCount() - 1);
        change(next, cursor.at(measure, 0));
    }

    /** Vacia el compas dejando sus atributos, como el Compas > Vaciar del manual. */
    public void emptyCurrentMeasure(boolean everyTrack) {
        int index = cursor.measure();
        Score next = everyTrack
                ? mapEveryTrack(track -> emptyMeasureOf(track, index))
                : score.mappingTrack(cursor.track(), track -> emptyMeasureOf(track, index));
        change(next, cursor.onBeat(0));
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        change(score.withTimeSignatureFrom(cursor.measure(), timeSignature), cursor.onBeat(0));
    }

    /** La armadura rige desde este compas hasta el proximo cambio, como pide el manual. */
    public void setKeySignature(KeySignature keySignature) {
        change(score.withKeySignatureFrom(cursor.measure(), keySignature), cursor);
    }

    /** El triplet feel rige desde este compas hasta el proximo cambio, como pide el manual. */
    public void setTripletFeel(TripletFeel tripletFeel) {
        change(score.withTripletFeelFrom(cursor.measure(), tripletFeel), cursor);
    }

    public void toggleDoubleBar() {
        changeAttributes(attributes -> attributes.withDoubleBar(!attributes.doubleBar()));
    }

    public void toggleRepeatOpen() {
        changeAttributes(attributes -> attributes.withRepeatOpen(!attributes.repeatOpen()));
    }

    public void setRepeatCount(int times) {
        changeAttributes(attributes -> attributes.withRepeatCount(times));
    }

    public void setAlternateEndings(List<Integer> passes) {
        changeAttributes(attributes -> attributes.withAlternateEndings(passes));
    }

    public void setDirectionSymbol(DirectionSymbol symbol) {
        changeAttributes(attributes -> attributes.withSymbol(symbol));
    }

    public void setDirectionJump(DirectionJump jump) {
        changeAttributes(attributes -> attributes.withJump(jump));
    }

    public void setMarker(Marker marker) {
        changeAttributes(attributes -> attributes.withMarker(marker));
    }

    /**
     * A diferencia del resto de los atributos del compas, el salto de linea no rige para toda
     * la partitura: el manual dice que vale solo para la pista activa, salvo que se este en la
     * vista multipista, donde vale para esa vista, compartida por todas las pistas.
     */
    public void setLineBreak(LineBreak lineBreak, boolean everyTrack) {
        if (everyTrack) {
            changeAttributes(attributes -> attributes.withLineBreak(lineBreak));
        } else {
            change(score.withLineBreakInTrackAt(cursor.track(), cursor.measure(), lineBreak), cursor);
        }
    }

    /**
     * 8va/8vb/15ma/15mb del manual: cambian donde se escribe la nota en el pentagrama de la
     * pista activa, nunca como suena ni que dice la tablatura -y, como es una decision de
     * notacion de esa pista en ese pasaje, no de toda la partitura, vale solo para la pista
     * activa (nunca se propaga a las demas).
     */
    public void setOctaveMark(OctaveMark octaveMark) {
        change(score.withOctaveMarkInTrackAt(cursor.track(), cursor.measure(), octaveMark), cursor);
    }

    // ---- pistas -----------------------------------------------------------

    public void addTrack(Track track) {
        addTrackAt(score.trackCount(), track);
    }

    public void addTrackAt(int index, Track track) {
        Score next = score.withTrackInsertedAt(index, alignedToTheScore(track));
        change(next, clampedCursorIn(next, index));
    }

    public void removeCurrentTrack() {
        Score next = score.withoutTrackAt(cursor.track());
        change(next, clampedCursorIn(next, Math.max(0, cursor.track() - 1)));
    }

    public void moveCurrentTrack(int offset) {
        int target = cursor.track() + offset;
        if (target < 0 || target >= score.trackCount()) {
            return;
        }
        change(score.withTrackMoved(cursor.track(), target), cursor.onTrack(target));
    }

    public void selectTrack(int index) {
        if (index < 0 || index >= score.trackCount()) {
            throw new IllegalArgumentException("track fuera de rango: " + index);
        }
        moveCursor(clampedCursorIn(score, index));
    }

    public void renameTrack(int index, String name) {
        changeTrack(index, track -> track.withName(name));
    }

    public void setTrackSettings(int index, TrackSettings settings) {
        changeTrack(index, track -> track.withSettings(settings));
    }

    public void setTuning(int index, Tuning tuning) {
        Score next = score.mappingTrack(index, track -> retuned(track, tuning));
        Cursor nextCursor = index == cursor.track()
                ? cursor.onString(Math.min(cursor.string(), tuning.stringCount()))
                : cursor;
        change(next, nextCursor);
    }

    public void setProgram(int index, int program) {
        changeChannel(index, channel -> channel.withProgram(program));
    }

    public void setVolume(int index, int volume) {
        changeChannel(index, channel -> channel.withVolume(volume));
    }

    public void setPan(int index, int pan) {
        changeChannel(index, channel -> channel.withPan(pan));
    }

    public void setChorus(int index, int chorus) {
        changeChannel(index, channel -> channel.withChorus(chorus));
    }

    public void setReverb(int index, int reverb) {
        changeChannel(index, channel -> channel.withReverb(reverb));
    }

    public void setPhaser(int index, int phaser) {
        changeChannel(index, channel -> channel.withPhaser(phaser));
    }

    public void setTremolo(int index, int tremolo) {
        changeChannel(index, channel -> channel.withTremolo(tremolo));
    }

    public void setPort(int index, int port) {
        changeChannel(index, channel -> channel.withPort(port));
    }

    public void setChannelNumber(int index, int number) {
        changeChannel(index, channel -> channel.withNumber(number));
    }

    public void setEffectChannel(int index, int number) {
        changeChannel(index, channel -> channel.withEffectChannel(number));
    }

    public void toggleMute(int index) {
        changeChannel(index, Channel::toggledMute);
    }

    public void toggleSolo(int index) {
        changeChannel(index, Channel::toggledSolo);
    }

    // ---- partitura --------------------------------------------------------

    public void setTempo(int bpm) {
        change(score.withTempo(bpm), cursor);
    }

    public void setInfo(ScoreInfo info) {
        change(score.withInfo(info), cursor);
    }

    public void setTitle(String title) {
        change(score.withTitle(title), cursor);
    }

    public void setLyrics(Lyrics lyrics) {
        change(score.withLyrics(lyrics), cursor);
    }

    public void replaceScore(Score score) {
        this.score = score;
        this.cursor = new Cursor(0, 0, 0, 1);
        this.selectionAnchor = null;
        history.forget();
        notifyListeners();
    }

    /** Aplica el resultado de un asistente, que trabaja sobre la partitura entera. */
    public void apply(UnaryOperator<Score> wizard) {
        Score next = wizard.apply(score);
        change(next, clampedCursorIn(next, Math.min(cursor.track(), next.trackCount() - 1)));
    }

    // ---- navegacion -------------------------------------------------------

    public void moveTo(int measure, int beat, int string) {
        Track track = currentTrack();
        if (measure < 0 || measure >= track.measureCount()) {
            throw new IllegalArgumentException("measure fuera de rango: " + measure);
        }
        Voice voice = track.measure(measure).voice(cursor.voice());
        if (beat < 0 || beat >= voice.beatCount()) {
            throw new IllegalArgumentException("beat fuera de rango: " + beat);
        }
        if (string < 1 || string > track.stringCount()) {
            throw new IllegalArgumentException("string fuera de rango: " + string);
        }
        moveCursor(new Cursor(cursor.track(), measure, cursor.voice(), beat, string));
    }

    public void moveDown() {
        moveCursor(cursor.onString(Math.min(currentTrack().stringCount(), cursor.string() + 1)));
    }

    public void moveUp() {
        moveCursor(cursor.onString(Math.max(1, cursor.string() - 1)));
    }

    public void moveLeft() {
        if (cursor.beat() > 0) {
            moveCursor(cursor.onBeat(cursor.beat() - 1));
            return;
        }
        if (cursor.measure() > 0) {
            int previous = cursor.measure() - 1;
            int lastBeat = currentTrack().measure(previous).voice(cursor.voice()).beatCount() - 1;
            moveCursor(cursor.at(previous, Math.max(0, lastBeat)));
        }
    }

    public void moveRight() {
        Measure measure = currentMeasure();
        Voice voice = currentVoice();
        if (cursor.beat() + 1 < voice.beatCount()) {
            moveCursor(cursor.onBeat(cursor.beat() + 1));
            return;
        }
        if (measure.isTooShort()) {
            Beat rest = Beat.rest(currentBeat().duration());
            int newBeat = voice.beatCount();
            changeVoiceAndCursor(it -> it.withBeatAppended(rest), cursor.onBeat(newBeat));
            return;
        }
        Track track = currentTrack();
        if (cursor.measure() + 1 < track.measureCount()) {
            moveCursor(cursor.at(cursor.measure() + 1, 0));
            return;
        }
        int newMeasure = track.measureCount();
        change(score.withMeasureInsertedInEveryTrackAt(newMeasure), cursor.at(newMeasure, 0));
    }

    public void moveToPreviousMeasure() {
        moveCursor(cursor.at(Math.max(0, cursor.measure() - 1), 0));
    }

    public void moveToNextMeasure() {
        moveCursor(cursor.at(Math.min(currentTrack().measureCount() - 1, cursor.measure() + 1), 0));
    }

    public void moveToFirstMeasure() {
        moveCursor(cursor.at(0, 0));
    }

    public void moveToLastMeasure() {
        moveCursor(cursor.at(currentTrack().measureCount() - 1, 0));
    }

    public void moveToPreviousTrack() {
        moveCursor(clampedCursorIn(score, Math.max(0, cursor.track() - 1)));
    }

    public void moveToNextTrack() {
        moveCursor(clampedCursorIn(score, Math.min(score.trackCount() - 1, cursor.track() + 1)));
    }

    /**
     * Los marcadores son la forma rapida de moverse entre las partes de la partitura. Si no hay
     * ninguno hacia donde se pide, el cursor se queda donde esta.
     */
    public void moveToNextMarker() {
        for (int measure = cursor.measure() + 1; measure < currentTrack().measureCount(); measure++) {
            if (score.attributesOf(measure).marker().isPresent()) {
                moveCursor(cursor.at(measure, 0));
                return;
            }
        }
    }

    public void moveToPreviousMarker() {
        for (int measure = cursor.measure() - 1; measure >= 0; measure--) {
            if (score.attributesOf(measure).marker().isPresent()) {
                moveCursor(cursor.at(measure, 0));
                return;
            }
        }
    }

    public void moveToMeasureStart() {
        moveCursor(cursor.onBeat(0));
    }

    public void moveToMeasureEnd() {
        moveCursor(cursor.onBeat(currentVoice().beatCount() - 1));
    }

    // ---- seleccion --------------------------------------------------------

    public Optional<Selection> selection() {
        return Optional.ofNullable(selectionAnchor)
                .map(anchor -> Selection.of(anchor, cursor, selectingWholeMeasures));
    }

    public void startSelection(boolean wholeMeasures) {
        selectionAnchor = cursor;
        selectingWholeMeasures = wholeMeasures;
        notifyListeners();
    }

    public void clearSelection() {
        selectionAnchor = null;
        notifyListeners();
    }

    public void selectAll() {
        selectionAnchor = cursor.at(0, 0);
        selectingWholeMeasures = true;
        moveCursor(cursor.at(currentTrack().measureCount() - 1, 0));
    }

    public void selectMeasures(int fromMeasure, int toMeasure) {
        selectionAnchor = cursor.at(fromMeasure, 0);
        selectingWholeMeasures = true;
        moveCursor(cursor.at(toMeasure, 0));
    }

    // ---- cortar, copiar y pegar -------------------------------------------

    /**
     * Copia lo seleccionado. Dentro de un compas copia beats; en cualquier otro
     * caso, compases enteros, de la pista activa o de todas.
     */
    public void copy(boolean everyTrack) {
        Selection range = selection().orElseGet(this::justTheCurrentMeasure);
        clipboard.hold(clippingOf(range, everyTrack));
    }

    /** Corta compases de todas las pistas, como el Compas > Cortar del manual. */
    public void cut() {
        Selection range = selection().orElseGet(this::justTheCurrentMeasure);
        clipboard.hold(clippingOf(range, true));
        if (clipboard.content().holdsBeats()) {
            removeSelectedBeats(range);
            return;
        }
        Score next = score;
        for (int measure = range.toMeasure(); measure >= range.fromMeasure(); measure--) {
            next = next.withoutMeasureInEveryTrackAt(measure);
        }
        clearSelection();
        change(next, clampedCursorIn(next, cursor.track()).at(Math.min(range.fromMeasure(), next.track(cursor.track()).measureCount() - 1), 0));
    }

    public void paste(PasteOptions options) {
        Clipboard.Clipping clipping = clipboard.content();
        if (clipping.isEmpty()) {
            return;
        }
        if (clipping.holdsBeats()) {
            pasteBeats(clipping, options);
            return;
        }
        pasteMeasures(clipping, options);
    }

    private void pasteBeats(Clipboard.Clipping clipping, PasteOptions options) {
        List<Beat> pasted = new ArrayList<>();
        for (int pass = 0; pass < options.repetitions(); pass++) {
            pasted.addAll(clipping.beats());
        }
        changeVoiceAndCursor(voice -> {
            Voice updated = voice;
            if (!options.inserting()) {
                for (int index = 0; index < pasted.size() && cursor.beat() < updated.beatCount(); index++) {
                    updated = updated.withoutBeatAt(cursor.beat());
                }
            }
            for (int index = pasted.size() - 1; index >= 0; index--) {
                updated = updated.withBeatInsertedAt(Math.min(cursor.beat(), updated.beatCount()), pasted.get(index));
            }
            return updated;
        }, cursor);
    }

    private void pasteMeasures(Clipboard.Clipping clipping, PasteOptions options) {
        List<List<Measure>> pasted = clipping.measuresByTrack();
        Score next = score;
        for (int trackIndex = 0; trackIndex < score.trackCount(); trackIndex++) {
            List<Measure> source = sourceFor(clipping, pasted, trackIndex);
            if (source.isEmpty()) {
                continue;
            }
            next = next.withTrack(trackIndex, pasteInto(next.track(trackIndex), source, options));
        }
        clearSelection();
        change(next, clampedCursorIn(next, cursor.track()));
    }

    private List<Measure> sourceFor(Clipboard.Clipping clipping, List<List<Measure>> pasted, int trackIndex) {
        if (clipping.spansEveryTrack()) {
            return trackIndex < pasted.size() ? pasted.get(trackIndex) : List.of();
        }
        if (trackIndex != cursor.track()) {
            return List.of();
        }
        return clipping.fitsATrackOf(score.track(trackIndex).stringCount()) ? pasted.getFirst() : List.of();
    }

    private Track pasteInto(Track track, List<Measure> source, PasteOptions options) {
        List<Measure> measures = new ArrayList<>(track.measures());
        int at = Math.min(cursor.measure(), measures.size());
        for (int pass = 0; pass < options.repetitions(); pass++) {
            for (Measure measure : source) {
                if (options.inserting() || at >= measures.size()) {
                    measures.add(Math.min(at, measures.size()), measure);
                } else {
                    measures.set(at, measure);
                }
                at++;
            }
        }
        return track.withMeasures(measures);
    }

    private Clipboard.Clipping clippingOf(Selection range, boolean everyTrack) {
        Track track = score.track(range.track());
        if (range.spansOneMeasure() && !range.wholeMeasures()) {
            Voice voice = track.measure(range.fromMeasure()).voice(cursor.voice());
            int last = Math.min(range.toBeat(), voice.beatCount() - 1);
            return Clipboard.Clipping.ofBeats(
                    List.copyOf(voice.beats().subList(range.fromBeat(), last + 1)), track.stringCount());
        }
        if (!everyTrack) {
            return Clipboard.Clipping.ofMeasures(List.of(measuresOf(track, range)), track.stringCount());
        }
        return Clipboard.Clipping.ofMeasures(
                score.tracks().stream().map(it -> measuresOf(it, range)).toList(), track.stringCount());
    }

    private static List<Measure> measuresOf(Track track, Selection range) {
        int from = Math.min(range.fromMeasure(), track.measureCount() - 1);
        int to = Math.min(range.toMeasure(), track.measureCount() - 1);
        return List.copyOf(track.measures().subList(from, to + 1));
    }

    private void removeSelectedBeats(Selection range) {
        int last = range.toBeat();
        changeVoiceAndCursor(voice -> {
            Voice updated = voice;
            for (int beat = Math.min(last, updated.beatCount() - 1); beat >= range.fromBeat(); beat--) {
                updated = updated.withoutBeatAt(beat);
            }
            return updated;
        }, cursor.onBeat(range.fromBeat()));
        clearSelection();
    }

    private Selection justTheCurrentMeasure() {
        return Selection.ofMeasures(cursor.track(), cursor.measure(), cursor.measure());
    }

    // ---- historia ---------------------------------------------------------

    public boolean isUndoEnabled() {
        return undoEnabled;
    }

    /**
     * Deshacer y rehacer se pueden apagar para no cargar memoria en una
     * computadora vieja, tal como ofrece Preferencias. Al apagarla se olvida
     * lo que ya se podia deshacer.
     */
    public void setUndoEnabled(boolean undoEnabled) {
        this.undoEnabled = undoEnabled;
        if (!undoEnabled) {
            history.forget();
        }
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    public void undo() {
        if (history.canUndo()) {
            restore(history.undo(currentSnapshot()));
        }
    }

    public void redo() {
        if (history.canRedo()) {
            restore(history.redo(currentSnapshot()));
        }
    }

    public void addListener(EditorListener listener) {
        listeners.add(listener);
    }

    // ---- como se aplican los cambios --------------------------------------

    void change(Score next, Cursor nextCursor) {
        if (next.equals(score)) {
            moveCursor(nextCursor);
            return;
        }
        if (undoEnabled) {
            history.remember(currentSnapshot());
        }
        score = next;
        cursor = nextCursor;
        notifyListeners();
    }

    private void changeCurrentNote(UnaryOperator<Note> howToChange) {
        if (currentNote().isEmpty()) {
            return;
        }
        changeCurrentBeat(beat -> beat.mappingNoteOn(cursor.string(), howToChange));
    }

    private void changeNoteEffects(UnaryOperator<NoteEffects> howToChange) {
        changeCurrentNote(note -> note.withEffects(howToChange.apply(note.effects())));
    }

    private void changeBeatEffects(UnaryOperator<BeatEffects> howToChange) {
        changeCurrentBeat(beat -> beat.withEffects(howToChange.apply(beat.effects())));
    }

    private void changeCurrentBeat(UnaryOperator<Beat> howToChange) {
        changeBeatAndCursor(howToChange, cursor);
    }

    private void changeBeatAndCursor(UnaryOperator<Beat> howToChange, Cursor nextCursor) {
        changeVoiceAndCursor(voice -> voice.withBeat(cursor.beat(), howToChange.apply(voice.beat(cursor.beat()))), nextCursor);
    }

    private void changeVoiceAndCursor(UnaryOperator<Voice> howToChange, Cursor nextCursor) {
        Measure updated = currentMeasure().mappingVoice(cursor.voice(), howToChange);
        change(withCurrentMeasure(updated), nextCursor);
    }

    private void changeAttributes(UnaryOperator<MeasureAttributes> howToChange) {
        MeasureAttributes updated = howToChange.apply(score.attributesOf(cursor.measure()));
        change(score.withAttributesInEveryTrackAt(cursor.measure(), updated), cursor);
    }

    private void changeChannel(int index, UnaryOperator<Channel> howToChange) {
        changeTrack(index, track -> track.withChannel(howToChange.apply(track.channel())));
    }

    private void changeTrack(int index, UnaryOperator<Track> howToChange) {
        change(score.mappingTrack(index, howToChange), cursor);
    }

    private Score mapEveryTrack(UnaryOperator<Track> howToChange) {
        List<Track> updated = score.tracks().stream().map(howToChange).toList();
        Score next = score;
        for (int index = 0; index < updated.size(); index++) {
            next = next.withTrack(index, updated.get(index));
        }
        return next;
    }

    private static Track emptyMeasureOf(Track track, int index) {
        return index < track.measureCount() ? track.mappingMeasure(index, Measure::emptied) : track;
    }

    /**
     * Al cambiar la afinacion, cada nota conserva su altura y se reubica en la cuerda y el
     * traste de la afinacion nueva que la produzcan (el mismo truco de ChordFretting, que ya
     * resuelve reunir alturas sueltas en cuerdas). Solo se pierde si no entra en ninguna cuerda.
     */
    private static Track retuned(Track track, Tuning tuning) {
        Tuning oldTuning = track.tuning();
        int fretLimit = Tuning.MAX_FRET;
        Track relocated = track.mappingMeasures(measure -> {
            Measure updated = measure;
            for (VoicePart part : VoicePart.values()) {
                updated = updated.mappingVoice(part, voice -> voice.mappingBeats(beat ->
                        beat.withNotes(relocatedNotes(oldTuning, tuning, fretLimit, beat.notes()))));
            }
            return updated;
        });
        return relocated.withTuning(tuning);
    }

    private static List<Note> relocatedNotes(Tuning oldTuning, Tuning newTuning, int fretLimit, List<Note> notes) {
        Map<Pitch, Deque<Note>> byPitch = new LinkedHashMap<>();
        List<Pitch> pitches = new ArrayList<>();
        for (Note note : notes) {
            Pitch pitch = oldTuning.pitchOf(note);
            pitches.add(pitch);
            byPitch.computeIfAbsent(pitch, p -> new ArrayDeque<>()).add(note);
        }
        List<Note> relocated = new ArrayList<>();
        for (Note placed : ChordFretting.assign(newTuning, fretLimit, pitches)) {
            Note original = byPitch.get(newTuning.pitchOf(placed)).poll();
            relocated.add(placed.withEffects(original.effects()).tied(original.tied()));
        }
        return relocated;
    }

    /**
     * Una pista nueva entra con la misma cantidad de compases que la partitura,
     * con los mismos atributos -porque un compas vale igual en todas las pistas-
     * y, si no es de percusion, en el proximo canal libre: sin esto quedaria en
     * el canal por defecto de {@link Channel#playing}, que colisiona con
     * cualquier otra pista que tampoco lo haya tocado.
     */
    private Track alignedToTheScore(Track track) {
        Track aligned = track.isPercussion()
                ? track
                : track.withChannel(track.channel().withNextFreeChannelPairAfter(channelsInUse()));
        while (aligned.measureCount() < score.measureCount()) {
            aligned = aligned.withMeasureInsertedAt(aligned.measureCount(), Measure.empty(TimeSignature.fourFour(), Duration.quarter()));
        }
        Track reference = score.track(0);
        for (int index = 0; index < aligned.measureCount() && index < reference.measureCount(); index++) {
            Measure model = reference.measure(index);
            aligned = aligned.mappingMeasure(index,
                    measure -> measure.withTimeSignature(model.timeSignature()).withAttributes(model.attributes()));
        }
        return aligned;
    }

    /** Los canales -limpio y de efectos- que ya estan usando las pistas no percutivas de la partitura. */
    private Set<Integer> channelsInUse() {
        Set<Integer> used = new HashSet<>();
        for (int index = 0; index < score.trackCount(); index++) {
            Track existing = score.track(index);
            if (!existing.isPercussion()) {
                used.add(existing.channel().number());
                used.add(existing.channel().effectChannel());
            }
        }
        return used;
    }

    private Cursor clampedCursorIn(Score next, int trackIndex) {
        Track track = next.track(trackIndex);
        int measure = Math.min(cursor.measure(), track.measureCount() - 1);
        VoicePart voice = track.measure(measure).voice(cursor.voice()).isUnused() ? VoicePart.LEAD : cursor.voice();
        int beat = Math.min(cursor.beat(), track.measure(measure).voice(voice).beatCount() - 1);
        int string = Math.min(cursor.string(), track.stringCount());
        return new Cursor(trackIndex, measure, voice, beat, string);
    }

    private Score withCurrentMeasure(Measure measure) {
        return score.mappingTrack(cursor.track(), track -> track.withMeasure(cursor.measure(), measure));
    }

    private EditorHistory.Snapshot currentSnapshot() {
        return new EditorHistory.Snapshot(score, cursor);
    }

    private void restore(EditorHistory.Snapshot snapshot) {
        score = snapshot.score();
        cursor = snapshot.cursor();
        notifyListeners();
    }

    private void moveCursor(Cursor next) {
        cursor = next;
        notifyListeners();
    }

    private void notifyListeners() {
        for (EditorListener listener : listeners) {
            listener.editorChanged();
        }
    }
}

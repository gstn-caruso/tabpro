package com.gstncaruso.tabpro.ui.actions;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.effects.BeamBreak;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import com.gstncaruso.tabpro.ui.EdtEditorListener;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.icons.Icons;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Commands {

    private final Editor editor;
    private final Ports.Dialogs dialogs;
    private final Ports.Playback playback;
    private final Ports.View view;
    private final Ports.Document document;
    private final java.util.List<String> themes;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public Commands(
            Editor editor,
            Ports.Document document,
            Ports.Dialogs dialogs,
            Ports.Playback playback,
            Ports.View view) {
        this(editor, document, dialogs, playback, view, java.util.List.of());
    }

    public Commands(
            Editor editor,
            Ports.Document document,
            Ports.Dialogs dialogs,
            Ports.Playback playback,
            Ports.View view,
            java.util.List<String> themes) {
        this.themes = java.util.List.copyOf(themes);
        this.editor = editor;
        this.document = document;
        this.dialogs = dialogs;
        this.playback = playback;
        this.view = view;
        defineFileCommands();
        defineEditCommands();
        defineBarCommands();
        defineTrackCommands();
        defineNoteCommands();
        defineEffectCommands();
        defineMarkerCommands();
        defineToolCommands();
        defineSoundCommands();
        defineViewCommands();
        defineHelpCommands();
    }

    public Command get(String name) {
        Command command = commands.get(name);
        if (command == null) {
            throw new IllegalArgumentException("no command named " + name);
        }
        return command;
    }

    public Map<String, Command> all() {
        return Map.copyOf(commands);
    }

    private void defineFileCommands() {
        define("file.new", Texts.get("menus.file.new"), document::newScore)
                .withAccelerator("ctrl N").withIcon(Icons.newScore());
        define("file.open", Texts.get("menus.file.open"), document::open)
                .withAccelerator("ctrl O").withIcon(Icons.open());
        define("file.browse", Texts.get("menus.file.browse"), document::browse).withAccelerator("ctrl B");
        define("file.save", Texts.get("menus.file.save"), document::save)
                .withAccelerator("ctrl S").withIcon(Icons.save());
        define("file.saveAs", Texts.get("menus.file.saveAs"), document::saveAs);
        define("file.importMidi", Texts.get("menus.file.importMidi"), document::importMidi);
        define("file.importAscii", Texts.get("menus.file.importAscii"), document::importAscii);
        define("file.importMusicXml", Texts.get("menus.file.importMusicXml"), document::importMusicXml);
        define("file.importGuitarPro", Texts.get("menus.file.importGuitarPro"), document::importGuitarPro);
        define("file.importTabEdit", Texts.get("menus.file.importTabEdit"), document::importTabEdit);
        define("file.importPowerTab", Texts.get("menus.file.importPowerTab"), document::importPowerTab);
        define("file.exportMidi", Texts.get("menus.file.exportMidi"), document::exportMidi);
        define("file.exportWave", Texts.get("menus.file.exportWave"), document::exportWave);
        define("file.exportAscii", Texts.get("menus.file.exportAscii"), document::exportAscii);
        define("file.exportMusicXml", Texts.get("menus.file.exportMusicXml"), document::exportMusicXml);
        define("file.exportGuitarPro", Texts.get("menus.file.exportGuitarPro"), document::exportGuitarPro);
        define("file.exportImage", Texts.get("menus.file.exportImage"), document::exportImage);
        define("file.exportPdf", Texts.get("menus.file.exportPdf"), document::exportPdf);
        define("file.information", Texts.get("menus.file.information"), dialogs::scoreInformation)
                .withAccelerator("F5").withIcon(Icons.scoreInformation());
        define("file.pageSetup", Texts.get("menus.file.pageSetup"), dialogs::pageSetup)
                .withAccelerator("F8").withIcon(Icons.pageSetup());
        define("file.print", Texts.get("menus.file.print"), document::print)
                .withAccelerator("ctrl P").withIcon(Icons.print());
        define("file.quit", Texts.get("menus.file.quit"), document::quit);
    }

    private void defineEditCommands() {
        define("edit.undo", Texts.get("menus.edit.undo"), editor::undo)
                .withAccelerator("ctrl Z").withIcon(Icons.undo());
        define("edit.redo", Texts.get("menus.edit.redo"), editor::redo)
                .withAccelerator("ctrl shift Z").withIcon(Icons.redo());
        define("edit.cut", Texts.get("menus.edit.cut"), editor::cut).withAccelerator("ctrl X").withIcon(Icons.cut());
        define("edit.copy", Texts.get("menus.edit.copy"), () -> editor.copy(true))
                .withAccelerator("ctrl C").withIcon(Icons.copy());
        define("edit.copyTrack", Texts.get("menus.edit.copyTrack"), () -> editor.copy(false));
        define("edit.paste", Texts.get("menus.edit.paste"), () -> editor.paste(PasteOptions.replacingOnce()))
                .withAccelerator("ctrl V").withIcon(Icons.paste());
        define("edit.pasteOptions", Texts.get("menus.edit.pasteOptions"), dialogs::pasteOptions);
        define("edit.selectAll", Texts.get("menus.edit.selectAll"), editor::selectAll)
                .withAccelerator("ctrl A");
        define("edit.leadVoice", Texts.get("menus.edit.leadVoice"), () -> editor.editVoice(VoicePart.LEAD))
                .withAccelerator("ctrl 1");
        define("edit.bassVoice", Texts.get("menus.edit.bassVoice"), () -> editor.editVoice(VoicePart.BASS))
                .withAccelerator("ctrl 2");
        define("edit.emptyBar", Texts.get("menus.edit.emptyBar"), () -> editor.emptyCurrentMeasure(false));
        define("edit.emptyBarEveryTrack", Texts.get("menus.edit.emptyBarEveryTrack"),
                () -> editor.emptyCurrentMeasure(true));
    }

    private void defineBarCommands() {
        define("bar.insert", Texts.get("menus.bar.insert"), editor::insertMeasure)
                .withAccelerator("ctrl INSERT").withIcon(Icons.insertBar());
        define("bar.delete", Texts.get("menus.bar.delete"), editor::deleteMeasure).withIcon(Icons.deleteBar());
        define("bar.keySignature", Texts.get("menus.bar.keySignature"), dialogs::keySignature)
                .withIcon(Icons.keySignature());
        define("bar.timeSignature", Texts.get("menus.bar.timeSignature"), dialogs::timeSignature)
                .withIcon(Icons.timeSignature());
        define("bar.tripletFeel", Texts.get("menus.bar.tripletFeel"), dialogs::tripletFeel)
                .withIcon(Icons.tripletFeel());
        define("bar.doubleBar", Texts.get("menus.bar.doubleBar"), editor::toggleDoubleBar)
                .withIcon(Icons.doubleBar());
        define("bar.repeatOpen", Texts.get("menus.bar.repeatOpen"), editor::toggleRepeatOpen)
                .withIcon(Icons.repeatOpen());
        define("bar.repeatClose", Texts.get("menus.bar.repeatClose"), dialogs::repeatClose)
                .withIcon(Icons.repeatClose());
        define("bar.alternateEndings", Texts.get("menus.bar.alternateEndings"), dialogs::alternateEndings)
                .withIcon(Icons.alternateEndings());
        define("bar.directions", Texts.get("menus.bar.directions"), dialogs::musicalDirections);
        define("bar.forceLineBreak", Texts.get("menus.bar.forceLineBreak"),
                () -> editor.setLineBreak(LineBreak.FORCED, view.isMultitrack())).withIcon(Icons.forceLineBreak());
        define("bar.preventLineBreak", Texts.get("menus.bar.preventLineBreak"),
                () -> editor.setLineBreak(LineBreak.PREVENTED, view.isMultitrack()))
                .withIcon(Icons.preventLineBreak());
        define("bar.resetLineBreak", Texts.get("menus.bar.resetLineBreak"),
                () -> editor.setLineBreak(LineBreak.AUTOMATIC, view.isMultitrack()));
        define("bar.octave8va", Texts.get("menus.bar.octave8va"),
                () -> editor.setOctaveMark(OctaveMark.OTTAVA_ALTA)).withIcon(Icons.octave8va());
        define("bar.octave8vb", Texts.get("menus.bar.octave8vb"),
                () -> editor.setOctaveMark(OctaveMark.OTTAVA_BASSA)).withIcon(Icons.octave8vb());
        define("bar.octave15ma", Texts.get("menus.bar.octave15ma"),
                () -> editor.setOctaveMark(OctaveMark.QUINDICESIMA_ALTA)).withIcon(Icons.octave15ma());
        define("bar.octave15mb", Texts.get("menus.bar.octave15mb"),
                () -> editor.setOctaveMark(OctaveMark.QUINDICESIMA_BASSA)).withIcon(Icons.octave15mb());
        define("bar.octaveNone", Texts.get("menus.bar.octaveNone"), () -> editor.setOctaveMark(OctaveMark.NONE));
    }

    private void defineTrackCommands() {
        define("track.add", Texts.get("menus.track.add"), dialogs::addTrack)
                .withAccelerator("ctrl shift INSERT").withIcon(Icons.addTrack());
        define("track.addGuitar", Texts.get("menus.track.addGuitar"),
                () -> editor.addTrack(Track.standardGuitar(Texts.get("defaults.guitarTrack"))));
        define("track.addBass", Texts.get("menus.track.addBass"),
                () -> editor.addTrack(Track.standardBass(Texts.get("defaults.bassTrack"))));
        define("track.addPercussion", Texts.get("menus.track.addPercussion"),
                () -> editor.addTrack(Track.percussion(Texts.get("defaults.percussionTrack"))));
        define("track.delete", Texts.get("menus.track.delete"), editor::removeCurrentTrack)
                .withAccelerator("ctrl shift DELETE").withIcon(Icons.trackDelete());
        define("track.moveUp", Texts.get("menus.track.moveUp"), () -> editor.moveCurrentTrack(-1))
                .withAccelerator("ctrl alt UP").withIcon(Icons.trackMoveUp());
        define("track.moveDown", Texts.get("menus.track.moveDown"), () -> editor.moveCurrentTrack(1))
                .withAccelerator("ctrl alt DOWN").withIcon(Icons.trackMoveDown());
        define("track.properties", Texts.get("menus.track.properties"), dialogs::trackProperties)
                .withAccelerator("F6").withIcon(Icons.trackProperties());
        define("track.instrument", Texts.get("menus.track.instrument"), dialogs::instrument)
                .withAccelerator("F7");
        define("track.previous", Texts.get("menus.track.previous"), editor::moveToPreviousTrack)
                .withAccelerator("ctrl UP").withIcon(Icons.chevronLeft());
        define("track.next", Texts.get("menus.track.next"), editor::moveToNextTrack)
                .withAccelerator("ctrl DOWN").withIcon(Icons.chevronRight());
    }

    private void defineNoteCommands() {
        define("note.shorter", Texts.get("menus.note.shorter"), editor::shortenDuration)
                .withAccelerator("PLUS");
        define("note.longer", Texts.get("menus.note.longer"), editor::lengthenDuration)
                .withAccelerator("MINUS");
        define("note.dot", Texts.get("menus.note.dot"), editor::toggleDot)
                .withAccelerator("PERIOD").withIcon(Icons.dottedNote());
        define("note.rest", Texts.get("menus.note.rest"), editor::clearBeat)
                .withAccelerator("R").withIcon(Icons.rest());
        define("note.triplet", Texts.get("menus.note.triplet"), editor::toggleTriplet)
                .withAccelerator("SLASH").withIcon(Icons.tuplet(3));
        defineTupletCommands();
        define("note.tie", Texts.get("menus.note.tie"), editor::toggleTie)
                .withAccelerator("L").withIcon(Icons.tie());
        define("note.tieBeat", Texts.get("menus.note.tieBeat"), editor::tieWholeBeat)
                .withAccelerator("ctrl L").withIcon(Icons.tieBeat());
        define("note.insertBeat", Texts.get("menus.note.insertBeat"), editor::insertBeat)
                .withAccelerator("INSERT");
        define("note.deleteNote", Texts.get("menus.note.deleteNote"), editor::clearNote)
                .withAccelerator("DELETE");
        define("note.deleteBeat", Texts.get("menus.note.deleteBeat"), editor::deleteBeat)
                .withAccelerator("ctrl DELETE");
        define("note.up", Texts.get("menus.note.up"), () -> editor.transposeNote(1))
                .withAccelerator("shift PLUS");
        define("note.down", Texts.get("menus.note.down"), () -> editor.transposeNote(-1))
                .withAccelerator("shift MINUS");
        define("note.toUpperString", Texts.get("menus.note.toUpperString"), editor::moveNoteUpOneString)
                .withAccelerator("alt UP");
        define("note.toLowerString", Texts.get("menus.note.toLowerString"), editor::moveNoteDownOneString)
                .withAccelerator("alt DOWN");
        define("note.repeatToEndOfBar", Texts.get("menus.note.repeatToEndOfBar"),
                editor::repeatBeatToTheEndOfTheMeasure).withAccelerator("C");
        define("note.dynamics", Texts.get("menus.note.dynamics"), dialogs::dynamics);
        for (Dynamic dynamic : Dynamic.values()) {
            define("note.dynamic." + dynamic.name(), Texts.get("menus.note.dynamic." + dynamic.name()),
                    () -> editor.setDynamic(dynamic)).withIcon(Icons.italicLetter(dynamic.symbol()));
        }
        define("note.soundDuration", Texts.get("menus.note.soundDuration"), dialogs::soundDuration)
                .withIcon(Icons.soundDuration());
        define("note.fingering", Texts.get("menus.note.fingering"), dialogs::fingering)
                .withIcon(Icons.fingering());
        define("note.fingeringRightHand", Texts.get("menus.note.fingeringRightHand"), dialogs::fingeringRightHand)
                .withIcon(Icons.fingeringRightHand());
        define("note.chord", Texts.get("menus.note.chord"), dialogs::chordDiagram)
                .withAccelerator("A").withIcon(Icons.chordDiagram());
        define("note.mixTableChange", Texts.get("menus.note.mixTableChange"), dialogs::mixTableChange)
                .withAccelerator("F10").withIcon(Icons.mixTable());
        defineBeamAndStemCommands();
        for (NoteValue value : NoteValue.values()) {
            define("note.value." + value.name(), Texts.get("menus.note.value." + value.name()),
                    () -> editor.setNoteValue(value)).withIcon(Icons.note(value));
        }
    }

    private void defineTupletCommands() {
        for (int enters : Tuplet.AVAILABLE) {
            if (enters == 1 || enters == 3) {
                continue;
            }
            define("note.tuplet." + enters, Texts.get("menus.note.tuplet." + enters),
                    () -> editor.toggleTuplet(enters)).withIcon(Icons.tuplet(enters));
        }
    }

    private void defineBeamAndStemCommands() {
        define("note.forceBeamBreak", Texts.get("menus.note.forceBeamBreak"),
                () -> editor.setBeamBreak(BeamBreak.FORCED)).withIcon(Icons.forceBeamBreak());
        define("note.preventBeamBreak", Texts.get("menus.note.preventBeamBreak"),
                () -> editor.setBeamBreak(BeamBreak.PREVENTED)).withIcon(Icons.preventBeamBreak());
        define("note.resetBeamBreak", Texts.get("menus.note.resetBeamBreak"),
                () -> editor.setBeamBreak(BeamBreak.AUTOMATIC)).withIcon(Icons.resetBeamBreak());
        define("note.stemUp", Texts.get("menus.note.stemUp"), () -> editor.setStemOverride(StemOverride.UP))
                .withIcon(Icons.stemUp());
        define("note.stemDown", Texts.get("menus.note.stemDown"), () -> editor.setStemOverride(StemOverride.DOWN))
                .withIcon(Icons.stemDown());
        define("note.stemAutomatic", Texts.get("menus.note.stemAutomatic"),
                () -> editor.setStemOverride(StemOverride.AUTOMATIC)).withIcon(Icons.stemAutomatic());
    }

    private void defineEffectCommands() {
        define("effect.hammer", Texts.get("menus.effect.hammer"),
                () -> editor.toggleOrnament(Ornament.HAMMER_ON_PULL_OFF))
                .withAccelerator("H").withIcon(Icons.hammerOn());
        define("effect.legatoSlide", Texts.get("menus.effect.legatoSlide"), () -> editor.setSlide(SlideType.LEGATO))
                .withAccelerator("S").withIcon(Icons.slide());
        define("effect.shiftSlide", Texts.get("menus.effect.shiftSlide"), () -> editor.setSlide(SlideType.SHIFT))
                .withAccelerator("alt S").withIcon(Icons.shiftSlide());
        define("effect.slideInFromBelow", Texts.get("menus.effect.slideInFromBelow"),
                () -> editor.setSlide(SlideType.IN_FROM_BELOW));
        define("effect.slideInFromAbove", Texts.get("menus.effect.slideInFromAbove"),
                () -> editor.setSlide(SlideType.IN_FROM_ABOVE));
        define("effect.slideOutDownwards", Texts.get("menus.effect.slideOutDownwards"),
                () -> editor.setSlide(SlideType.OUT_DOWNWARDS));
        define("effect.slideOutUpwards", Texts.get("menus.effect.slideOutUpwards"),
                () -> editor.setSlide(SlideType.OUT_UPWARDS));
        define("effect.noSlide", Texts.get("menus.effect.noSlide"), () -> editor.setSlide(null));
        define("effect.bend", Texts.get("menus.effect.bend"), dialogs::bend)
                .withAccelerator("B").withIcon(Icons.bend());
        define("effect.tremoloBar", Texts.get("menus.effect.tremoloBar"), dialogs::tremoloBar)
                .withIcon(Icons.tremoloBar());
        define("effect.vibrato", Texts.get("menus.effect.vibrato"), () -> editor.toggleOrnament(Ornament.VIBRATO))
                .withAccelerator("V").withIcon(Icons.vibrato());
        define("effect.wideVibrato", Texts.get("menus.effect.wideVibrato"), editor::toggleWideVibrato)
                .withIcon(Icons.wideVibrato());
        define("effect.trill", Texts.get("menus.effect.trill"), dialogs::trill).withIcon(Icons.trill());
        define("effect.tremoloPicking", Texts.get("menus.effect.tremoloPicking"), dialogs::tremoloPicking)
                .withIcon(Icons.tremoloPicking());
        define("effect.palmMute", Texts.get("menus.effect.palmMute"), () -> editor.toggleOrnament(Ornament.PALM_MUTE))
                .withAccelerator("P").withIcon(Icons.letter("PM"));
        define("effect.letRing", Texts.get("menus.effect.letRing"), () -> editor.toggleOrnament(Ornament.LET_RING))
                .withAccelerator("I").withIcon(Icons.letter("LR"));
        define("effect.staccato", Texts.get("menus.effect.staccato"), () -> editor.toggleOrnament(Ornament.STACCATO))
                .withIcon(Icons.staccato());
        define("effect.deadNote", Texts.get("menus.effect.deadNote"), () -> editor.toggleOrnament(Ornament.DEAD))
                .withAccelerator("X").withIcon(Icons.deadNote());
        define("effect.ghostNote", Texts.get("menus.effect.ghostNote"), () -> editor.toggleOrnament(Ornament.GHOST))
                .withAccelerator("O").withIcon(Icons.ghostNote());
        define("effect.accent", Texts.get("menus.effect.accent"), () -> editor.toggleOrnament(Ornament.ACCENTED))
                .withIcon(Icons.accent());
        define("effect.heavyAccent", Texts.get("menus.effect.heavyAccent"),
                () -> editor.toggleOrnament(Ornament.HEAVY_ACCENTED)).withIcon(Icons.heavyAccent());
        define("effect.fadeIn", Texts.get("menus.effect.fadeIn"), editor::toggleFadeIn)
                .withAccelerator("F").withIcon(Icons.fadeIn());
        define("effect.graceNote", Texts.get("menus.effect.graceNote"), dialogs::graceNote)
                .withAccelerator("G").withIcon(Icons.graceNote());
        define("effect.harmonics", Texts.get("menus.effect.harmonics"), dialogs::harmonics)
                .withIcon(Icons.harmonic());
        define("effect.naturalHarmonic", Texts.get("menus.effect.naturalHarmonic"),
                () -> editor.setHarmonic(HarmonicType.NATURAL)).withIcon(Icons.naturalHarmonic());
        define("effect.artificialHarmonic", Texts.get("menus.effect.artificialHarmonic"),
                () -> editor.setHarmonic(HarmonicType.ARTIFICIAL)).withIcon(Icons.artificialHarmonic());
        define("effect.tapping", Texts.get("menus.effect.tapping"), editor::toggleTapping)
                .withIcon(Icons.letter("T"));
        define("effect.slapping", Texts.get("menus.effect.slapping"), editor::toggleSlapping)
                .withIcon(Icons.letter("S"));
        define("effect.popping", Texts.get("menus.effect.popping"), editor::togglePopping)
                .withIcon(Icons.letter("P"));
        define("effect.strokeUp", Texts.get("menus.effect.strokeUp"),
                () -> editor.setStroke(Stroke.of(StrokeDirection.UP)))
                .withAccelerator("ctrl U").withIcon(Icons.strokeUp());
        define("effect.strokeDown", Texts.get("menus.effect.strokeDown"),
                () -> editor.setStroke(Stroke.of(StrokeDirection.DOWN)))
                .withAccelerator("ctrl D").withIcon(Icons.strokeDown());
        define("effect.strokeOptions", Texts.get("menus.effect.strokeOptions"), dialogs::stroke);
        define("effect.pickstrokeUp", Texts.get("menus.effect.pickstrokeUp"),
                () -> editor.setPickstroke(PickstrokeDirection.UP)).withIcon(Icons.pickstrokeUp());
        define("effect.pickstrokeDown", Texts.get("menus.effect.pickstrokeDown"),
                () -> editor.setPickstroke(PickstrokeDirection.DOWN)).withIcon(Icons.pickstrokeDown());
        define("effect.wahOpen", Texts.get("menus.effect.wahOpen"), () -> editor.setWah(Wah.OPEN));
        define("effect.wahClosed", Texts.get("menus.effect.wahClosed"), () -> editor.setWah(Wah.CLOSED));
        define("effect.wahOff", Texts.get("menus.effect.wahOff"), () -> editor.setWah(Wah.OFF));
        define("effect.text", Texts.get("menus.effect.text"), dialogs::text)
                .withAccelerator("T").withIcon(Icons.text());
    }

    private void defineMarkerCommands() {
        define("marker.insert", Texts.get("menus.marker.insert"), dialogs::insertMarker)
                .withAccelerator("shift INSERT").withIcon(Icons.marker());
        define("marker.edit", Texts.get("menus.marker.edit"), dialogs::editMarker).withIcon(Icons.markerEdit());
        define("marker.list", Texts.get("menus.marker.list"), dialogs::markerList).withIcon(Icons.markerList());
        define("marker.previous", Texts.get("menus.marker.previous"), editor::moveToPreviousMarker)
                .withAccelerator("shift TAB").withIcon(Icons.markerPrevious());
        define("marker.next", Texts.get("menus.marker.next"), editor::moveToNextMarker)
                .withAccelerator("ctrl TAB").withIcon(Icons.markerNext());
        editor.addListener(EdtEditorListener.onEdt(this::refreshEditMarkerCommand));
        refreshEditMarkerCommand();
    }

    private void refreshEditMarkerCommand() {
        get("marker.edit").setEnabled(editor.score().measureOfMarkerInEffectAt(editor.cursor().measure()).isPresent());
    }

    private void defineToolCommands() {
        define("tool.letRingOptions", Texts.get("menus.tool.letRingOptions"), dialogs::letRingOptions);
        define("tool.palmMuteOptions", Texts.get("menus.tool.palmMuteOptions"), dialogs::palmMuteOptions);
        define("tool.dynamicOptions", Texts.get("menus.tool.dynamicOptions"), dialogs::dynamicOptions);
        define("tool.arrangeBars", Texts.get("menus.tool.arrangeBars"), dialogs::arrangeBars);
        define("tool.completeBars", Texts.get("menus.tool.completeBars"), dialogs::completeBarsWithRests);
        define("tool.automaticFingering", Texts.get("menus.tool.automaticFingering"), dialogs::automaticFingering);
        define("tool.transpose", Texts.get("menus.tool.transpose"), dialogs::transpose).withIcon(Icons.transpose());
        define("tool.checkBarDurations", Texts.get("menus.tool.checkBarDurations"), dialogs::checkBarDurations)
                .withAccelerator("F4").withIcon(Icons.checkBarDurations());
        define("tool.scales", Texts.get("menus.tool.scales"), dialogs::scales).withIcon(Icons.scales());
        define("tool.tuner", Texts.get("menus.tool.tuner"), dialogs::tuner).withIcon(Icons.tuner());
    }

    private void defineSoundCommands() {
        define("sound.play", Texts.get("menus.sound.play"), playback::togglePlay)
                .withAccelerator("SPACE").withIcon(Icons.play());
        define("sound.playFromStart", Texts.get("menus.sound.playFromStart"), playback::playFromTheBeginning)
                .withAccelerator("ctrl SPACE");
        define("sound.loop", Texts.get("menus.sound.loop"), playback::loopAndSpeedTrainer)
                .withAccelerator("F9").withIcon(Icons.loop());
        define("sound.tempo", Texts.get("menus.sound.tempo"), playback::tempo);
        define("sound.relativeTempo", Texts.get("menus.sound.relativeTempo"), playback::relativeTempo);
        define("sound.metronome", Texts.get("menus.sound.metronome"), playback::toggleMetronome)
                .withIcon(Icons.metronome());
        define("sound.metronomeSettings", Texts.get("menus.sound.metronomeSettings"), dialogs::metronomeSettings);
        define("sound.countDown", Texts.get("menus.sound.countDown"), playback::toggleCountDown)
                .withIcon(Icons.countDown());
        define("sound.stepBack", Texts.get("menus.sound.stepBack"), playback::stepBack);
        define("sound.midiInput", Texts.get("menus.sound.midiInput"), playback::toggleMidiInput);
        define("sound.soundFont", Texts.get("menus.sound.soundFont"), this::toggleSoundFontAndRefreshItsCheckbox)
                .withAccelerator("F2").withIcon(Icons.letter("SF"));
        if (playback.soundFontActive()) {
            get("sound.soundFont").checkedByDefault();
        }
        define("sound.stepForward", Texts.get("menus.sound.stepForward"), playback::stepForward);
        define("nav.firstBar", Texts.get("menus.nav.firstBar"), editor::moveToFirstMeasure)
                .withAccelerator("ctrl HOME").withIcon(Icons.firstBar());
        define("nav.previousBar", Texts.get("menus.nav.previousBar"), editor::moveToPreviousMeasure)
                .withAccelerator("ctrl LEFT").withIcon(Icons.previousBar());
        define("nav.nextBar", Texts.get("menus.nav.nextBar"), editor::moveToNextMeasure)
                .withAccelerator("ctrl RIGHT").withIcon(Icons.nextBar());
        define("nav.nextNote", Texts.get("menus.nav.nextNote"), editor::enter).withAccelerator("ENTER");
        define("nav.lastBar", Texts.get("menus.nav.lastBar"), editor::moveToLastMeasure)
                .withAccelerator("ctrl END").withIcon(Icons.lastBar());
    }

    private void toggleSoundFontAndRefreshItsCheckbox() {
        playback.toggleSoundFont();
        get("sound.soundFont").setChecked(playback.soundFontActive());
    }

    private void defineViewCommands() {
        define("view.page", Texts.get("menus.view.page"), view::pageMode).withIcon(Icons.pageMode());
        define("view.parchment", Texts.get("menus.view.parchment"), view::parchmentMode)
                .withIcon(Icons.parchmentMode());
        define("view.verticalScreen", Texts.get("menus.view.verticalScreen"), view::verticalScreenMode)
                .withIcon(Icons.verticalScreen());
        define("view.horizontalScreen", Texts.get("menus.view.horizontalScreen"), view::horizontalScreenMode)
                .withIcon(Icons.horizontalScreen());
        define("view.zoomIn", Texts.get("menus.view.zoomIn"), view::zoomIn).withAccelerator("ctrl EQUALS");
        define("view.zoomOut", Texts.get("menus.view.zoomOut"), view::zoomOut).withAccelerator("ctrl MINUS");
        define("view.resetZoom", Texts.get("menus.view.resetZoom"), view::resetZoom).withAccelerator("ctrl 0");
        define("view.multitrack", Texts.get("menus.view.multitrack"), view::toggleMultitrack)
                .withIcon(Icons.multitrack());
        define("view.grayInactiveVoice", Texts.get("menus.view.grayInactiveVoice"), view::toggleGrayInactiveVoice)
                .withAccelerator("ctrl G");
        define("view.dynamicNotes", Texts.get("menus.view.dynamicNotes"), view::toggleShowsDynamicNotes)
                .withAccelerator("F11");
        define("view.hideStandardNotation", Texts.get("menus.view.hideStandardNotation"), view::toggleStandardNotation)
                .withIcon(Icons.hideStandardNotation());
        define("view.hideTablature", Texts.get("menus.view.hideTablature"), view::toggleTablature)
                .withIcon(Icons.hideTablature());
        define("view.fretboard", Texts.get("menus.view.fretboard"), view::toggleFretboard)
                .withAccelerator("ctrl 3").withIcon(Icons.fretboard());
        define("view.keyboard", Texts.get("menus.view.keyboard"), view::toggleKeyboard)
                .withAccelerator("ctrl 4").withIcon(Icons.keyboard());
        define("view.percussion", Texts.get("menus.view.percussion"), view::togglePercussionAssistant);
        define("view.mixTable", Texts.get("menus.view.mixTable"), view::toggleMixTable)
                .withIcon(Icons.mixTable());
        define("view.toggleView", Texts.get("menus.view.toggleView"), view::toggleView);
        define("view.toolBars", Texts.get("menus.view.toolBars"), view::toggleToolBars);
        define("view.toolBars.document", Texts.get("menus.view.toolBars.document"), view::toggleDocumentToolBar)
                .checkedByDefault();
        define("view.toolBars.structure", Texts.get("menus.view.toolBars.structure"), view::toggleStructureToolBar)
                .checkedByDefault();
        define("view.toolBars.notation", Texts.get("menus.view.toolBars.notation"), view::toggleNotationToolBar)
                .checkedByDefault();
        define("view.toolBars.effects", Texts.get("menus.view.toolBars.effects"), view::toggleEffectsToolBar)
                .checkedByDefault();
        for (String theme : themes) {
            define("view.theme." + theme, Texts.get("window.Theme." + theme), () -> view.useTheme(theme));
        }
        define("options.midiSetup", Texts.get("menus.options.midiSetup"), dialogs::midiSetup);
        define("options.preferences", Texts.get("menus.options.preferences"), dialogs::preferences)
                .withAccelerator("F12").withIcon(Icons.preferences());
    }

    private void defineHelpCommands() {
        define("help.contents", Texts.get("menus.help.contents"), dialogs::help).withAccelerator("F1");
        define("help.about", Texts.get("menus.help.about"), dialogs::about);
    }

    private Command define(String name, String label, Runnable body) {
        Command command = Command.named(label, body);
        commands.put(name, command);
        return command;
    }
}

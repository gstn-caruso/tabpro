package com.gstncaruso.tabpro.ui.actions;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.LineBreak;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Stroke;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import com.gstncaruso.tabpro.ui.icons.Icons;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Todos los comandos del manual, cada uno con su nombre, su atajo y su icono.
 * Los menus y las barras de herramientas se arman con esta misma lista, asi que
 * un atajo se escribe una sola vez.
 */
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

    /** El comando registrado con ese nombre interno. */
    public Command get(String name) {
        Command command = commands.get(name);
        if (command == null) {
            throw new IllegalArgumentException("no hay ningun comando llamado " + name);
        }
        return command;
    }

    public Map<String, Command> all() {
        return Map.copyOf(commands);
    }

    // ---- archivo ----------------------------------------------------------

    private void defineFileCommands() {
        define("file.new", "Nuevo", document::newScore).withAccelerator("ctrl N").withIcon(Icons.newScore());
        define("file.open", "Abrir…", document::open).withAccelerator("ctrl O").withIcon(Icons.open());
        define("file.browse", "Explorar…", document::browse).withAccelerator("ctrl B");
        define("file.save", "Guardar", document::save).withAccelerator("ctrl S").withIcon(Icons.save());
        define("file.saveAs", "Guardar como…", document::saveAs);
        define("file.importMidi", "MIDI…", document::importMidi);
        define("file.importAscii", "Tablatura ASCII…", document::importAscii);
        define("file.importMusicXml", "MusicXML…", document::importMusicXml);
        define("file.importGuitarPro", "Guitar Pro…", document::importGuitarPro);
        define("file.exportMidi", "MIDI…", document::exportMidi);
        define("file.exportWave", "WAVE…", document::exportWave);
        define("file.exportAscii", "Tablatura ASCII…", document::exportAscii);
        define("file.exportMusicXml", "MusicXML…", document::exportMusicXml);
        define("file.exportImage", "Imagen…", document::exportImage);
        define("file.exportPdf", "PDF…", document::exportPdf);
        define("file.information", "Información de la partitura…", dialogs::scoreInformation)
                .withAccelerator("F5").withIcon(Icons.scoreInformation());
        define("file.pageSetup", "Configurar página…", dialogs::pageSetup)
                .withAccelerator("F8").withIcon(Icons.pageSetup());
        define("file.print", "Imprimir…", document::print).withAccelerator("ctrl P").withIcon(Icons.print());
        define("file.quit", "Salir", document::quit);
    }

    // ---- edicion ----------------------------------------------------------

    private void defineEditCommands() {
        define("edit.undo", "Deshacer", editor::undo).withAccelerator("ctrl Z").withIcon(Icons.undo());
        define("edit.redo", "Rehacer", editor::redo).withAccelerator("ctrl shift Z").withIcon(Icons.redo());
        define("edit.cut", "Cortar", editor::cut).withAccelerator("ctrl X").withIcon(Icons.cut());
        define("edit.copy", "Copiar", () -> editor.copy(true)).withAccelerator("ctrl C").withIcon(Icons.copy());
        define("edit.copyTrack", "Copiar sólo esta pista", () -> editor.copy(false));
        define("edit.paste", "Pegar", () -> editor.paste(PasteOptions.replacingOnce()))
                .withAccelerator("ctrl V").withIcon(Icons.paste());
        define("edit.pasteOptions", "Pegar con opciones…", dialogs::pasteOptions);
        define("edit.selectAll", "Seleccionar todo", editor::selectAll).withAccelerator("ctrl A");
        define("edit.leadVoice", "Voz 1 (principal)", () -> editor.editVoice(VoicePart.LEAD)).withAccelerator("ctrl 1");
        define("edit.bassVoice", "Voz 2 (bajos)", () -> editor.editVoice(VoicePart.BASS)).withAccelerator("ctrl 2");
        define("edit.emptyBar", "Vaciar el compás", () -> editor.emptyCurrentMeasure(false));
        define("edit.emptyBarEveryTrack", "Vaciar el compás en todas las pistas",
                () -> editor.emptyCurrentMeasure(true));
    }

    // ---- compas -----------------------------------------------------------

    private void defineBarCommands() {
        define("bar.insert", "Insertar un compás", editor::insertMeasure)
                .withAccelerator("ctrl INSERT").withIcon(Icons.insertBar());
        define("bar.delete", "Borrar el compás", editor::deleteMeasure).withIcon(Icons.deleteBar());
        define("bar.keySignature", "Armadura…", dialogs::keySignature).withIcon(Icons.keySignature());
        define("bar.timeSignature", "Medida del compás…", dialogs::timeSignature).withIcon(Icons.timeSignature());
        define("bar.tripletFeel", "Triplet feel…", dialogs::tripletFeel);
        define("bar.doubleBar", "Doble barra", editor::toggleDoubleBar).withIcon(Icons.doubleBar());
        define("bar.repeatOpen", "Repetición: abrir", editor::toggleRepeatOpen).withIcon(Icons.repeatOpen());
        define("bar.repeatClose", "Repetición: cerrar…", dialogs::repeatClose).withIcon(Icons.repeatClose());
        define("bar.alternateEndings", "Finales alternativos…", dialogs::alternateEndings)
                .withIcon(Icons.alternateEndings());
        define("bar.directions", "Direcciones musicales…", dialogs::musicalDirections);
        define("bar.forceLineBreak", "Forzar salto de línea", () -> editor.setLineBreak(LineBreak.FORCED));
        define("bar.preventLineBreak", "Impedir salto de línea", () -> editor.setLineBreak(LineBreak.PREVENTED));
        define("bar.resetLineBreak", "Reiniciar la organización", () -> editor.setLineBreak(LineBreak.AUTOMATIC));
    }

    // ---- pistas -----------------------------------------------------------

    private void defineTrackCommands() {
        define("track.add", "Agregar una pista…", dialogs::addTrack)
                .withAccelerator("ctrl shift INSERT").withIcon(Icons.addTrack());
        define("track.addGuitar", "Agregar una guitarra", () -> editor.addTrack(Track.standardGuitar("Guitarra")));
        define("track.addBass", "Agregar un bajo", () -> editor.addTrack(Track.standardBass("Bajo")));
        define("track.addPercussion", "Agregar percusión", () -> editor.addTrack(Track.percussion("Batería")));
        define("track.delete", "Borrar la pista", editor::removeCurrentTrack).withAccelerator("ctrl shift DELETE");
        define("track.moveUp", "Subir la pista", () -> editor.moveCurrentTrack(-1)).withAccelerator("ctrl alt UP");
        define("track.moveDown", "Bajar la pista", () -> editor.moveCurrentTrack(1)).withAccelerator("ctrl alt DOWN");
        define("track.properties", "Propiedades de la pista…", dialogs::trackProperties).withAccelerator("F6");
        define("track.instrument", "Instrumento…", dialogs::instrument).withAccelerator("F7");
        define("track.previous", "Pista anterior", editor::moveToPreviousTrack).withAccelerator("ctrl UP");
        define("track.next", "Pista siguiente", editor::moveToNextTrack).withAccelerator("ctrl DOWN");
    }

    // ---- notas ------------------------------------------------------------

    private void defineNoteCommands() {
        // El manual: "+" divide la duracion por dos y "-" la multiplica, asi que "+" acorta.
        define("note.shorter", "Acortar la figura", editor::shortenDuration).withAccelerator("PLUS");
        define("note.longer", "Alargar la figura", editor::lengthenDuration).withAccelerator("MINUS");
        define("note.dot", "Puntillo", editor::toggleDot).withAccelerator("PERIOD").withIcon(Icons.dottedNote());
        define("note.rest", "Silencio", editor::clearBeat).withAccelerator("R").withIcon(Icons.rest());
        define("note.triplet", "Tresillo", editor::toggleTriplet).withAccelerator("SLASH").withIcon(Icons.tuplet());
        define("note.tie", "Ligar la nota", editor::toggleTie).withAccelerator("L").withIcon(Icons.tie());
        define("note.tieBeat", "Ligar el beat", editor::tieWholeBeat).withAccelerator("ctrl L");
        define("note.insertBeat", "Insertar un beat", editor::insertBeat).withAccelerator("INSERT");
        define("note.deleteNote", "Borrar la nota", editor::clearNote).withAccelerator("DELETE");
        define("note.deleteBeat", "Borrar el beat", editor::deleteBeat).withAccelerator("ctrl DELETE");
        define("note.up", "Subir un semitono", () -> editor.transposeNote(1)).withAccelerator("shift PLUS");
        define("note.down", "Bajar un semitono", () -> editor.transposeNote(-1)).withAccelerator("shift MINUS");
        define("note.toUpperString", "Mover a la cuerda de arriba", editor::moveNoteUpOneString)
                .withAccelerator("alt UP");
        define("note.toLowerString", "Mover a la cuerda de abajo", editor::moveNoteDownOneString)
                .withAccelerator("alt DOWN");
        define("note.repeatToEndOfBar", "Copiar el beat hasta el final del compás",
                editor::repeatBeatToTheEndOfTheMeasure).withAccelerator("C");
        define("note.dynamics", "Dinámica…", dialogs::dynamics);
        define("note.soundDuration", "Duración del sonido…", dialogs::soundDuration);
        define("note.fingering", "Digitación…", dialogs::fingering);
        define("note.chord", "Acorde…", dialogs::chordDiagram).withAccelerator("A").withIcon(Icons.chordDiagram());
        define("note.mixTableChange", "Cambio de parámetros…", dialogs::mixTableChange)
                .withAccelerator("F10").withIcon(Icons.mixTable());
        for (NoteValue value : NoteValue.values()) {
            define("note.value." + value.name(), nameOf(value), () -> editor.setNoteValue(value))
                    .withIcon(Icons.note(value));
        }
    }

    private static String nameOf(NoteValue value) {
        return switch (value) {
            case WHOLE -> "Redonda";
            case HALF -> "Blanca";
            case QUARTER -> "Negra";
            case EIGHTH -> "Corchea";
            case SIXTEENTH -> "Semicorchea";
            case THIRTY_SECOND -> "Fusa";
            case SIXTY_FOURTH -> "Semifusa";
        };
    }

    // ---- efectos ----------------------------------------------------------

    private void defineEffectCommands() {
        define("effect.hammer", "Ligado (hammer on / pull off)", () -> editor.toggleOrnament(Ornament.HAMMER_ON_PULL_OFF))
                .withAccelerator("H").withIcon(Icons.hammerOn());
        define("effect.legatoSlide", "Slide legato", () -> editor.setSlide(SlideType.LEGATO))
                .withAccelerator("S").withIcon(Icons.slide());
        define("effect.shiftSlide", "Slide con ataque", () -> editor.setSlide(SlideType.SHIFT))
                .withAccelerator("alt S");
        define("effect.noSlide", "Sin slide", () -> editor.setSlide(null));
        define("effect.bend", "Bend…", dialogs::bend).withAccelerator("B").withIcon(Icons.bend());
        define("effect.tremoloBar", "Palanca…", dialogs::tremoloBar);
        define("effect.vibrato", "Vibrato", () -> editor.toggleOrnament(Ornament.VIBRATO))
                .withAccelerator("V").withIcon(Icons.vibrato());
        define("effect.wideVibrato", "Vibrato amplio", editor::toggleWideVibrato).withIcon(Icons.wideVibrato());
        define("effect.trill", "Trino…", dialogs::trill);
        define("effect.tremoloPicking", "Trémolo de púa…", dialogs::tremoloPicking);
        define("effect.palmMute", "Palm mute", () -> editor.toggleOrnament(Ornament.PALM_MUTE))
                .withAccelerator("P").withIcon(Icons.letter("PM"));
        define("effect.letRing", "Let ring", () -> editor.toggleOrnament(Ornament.LET_RING))
                .withAccelerator("I").withIcon(Icons.letter("LR"));
        define("effect.staccato", "Staccato", () -> editor.toggleOrnament(Ornament.STACCATO))
                .withIcon(Icons.staccato());
        define("effect.deadNote", "Nota muerta", () -> editor.toggleOrnament(Ornament.DEAD))
                .withAccelerator("X").withIcon(Icons.deadNote());
        define("effect.ghostNote", "Nota fantasma", () -> editor.toggleOrnament(Ornament.GHOST))
                .withAccelerator("O").withIcon(Icons.ghostNote());
        define("effect.accent", "Nota acentuada", () -> editor.toggleOrnament(Ornament.ACCENTED))
                .withIcon(Icons.accent());
        define("effect.heavyAccent", "Nota muy acentuada", () -> editor.toggleOrnament(Ornament.HEAVY_ACCENTED));
        define("effect.fadeIn", "Fade in", editor::toggleFadeIn).withAccelerator("F");
        define("effect.graceNote", "Nota de adorno…", dialogs::graceNote).withAccelerator("G");
        define("effect.harmonics", "Armónicos…", dialogs::harmonics).withIcon(Icons.harmonic());
        define("effect.tapping", "Tapping", editor::toggleTapping).withIcon(Icons.letter("T"));
        define("effect.slapping", "Slap", editor::toggleSlapping).withIcon(Icons.letter("S"));
        define("effect.popping", "Pop", editor::togglePopping).withIcon(Icons.letter("P"));
        define("effect.strokeUp", "Rasgueo hacia arriba", () -> editor.setStroke(Stroke.of(StrokeDirection.UP)))
                .withAccelerator("ctrl U").withIcon(Icons.strokeUp());
        define("effect.strokeDown", "Rasgueo hacia abajo", () -> editor.setStroke(Stroke.of(StrokeDirection.DOWN)))
                .withAccelerator("ctrl D").withIcon(Icons.strokeDown());
        define("effect.strokeOptions", "Rasgueo…", dialogs::stroke);
        define("effect.pickstrokeUp", "Púa hacia arriba", () -> editor.setPickstroke(PickstrokeDirection.UP));
        define("effect.pickstrokeDown", "Púa hacia abajo", () -> editor.setPickstroke(PickstrokeDirection.DOWN));
        define("effect.wahOpen", "Wah abierto", () -> editor.setWah(Wah.OPEN));
        define("effect.wahClosed", "Wah cerrado", () -> editor.setWah(Wah.CLOSED));
        define("effect.wahOff", "Wah apagado", () -> editor.setWah(Wah.OFF));
        define("effect.text", "Texto…", dialogs::text).withAccelerator("T").withIcon(Icons.text());
    }

    // ---- marcadores -------------------------------------------------------

    private void defineMarkerCommands() {
        define("marker.insert", "Insertar un marcador…", dialogs::insertMarker)
                .withAccelerator("shift INSERT").withIcon(Icons.marker());
        define("marker.list", "Lista de marcadores…", dialogs::markerList);
        define("marker.previous", "Marcador anterior", editor::moveToPreviousMarker)
                .withAccelerator("shift TAB");
        define("marker.next", "Marcador siguiente", editor::moveToNextMarker)
                .withAccelerator("ctrl TAB");
    }

    // ---- herramientas -----------------------------------------------------

    private void defineToolCommands() {
        define("tool.letRingOptions", "Opciones de let ring…", dialogs::letRingOptions);
        define("tool.palmMuteOptions", "Opciones de palm mute…", dialogs::palmMuteOptions);
        define("tool.dynamicOptions", "Opciones de dinámica…", dialogs::dynamicOptions);
        define("tool.arrangeBars", "Organizador de compases…", dialogs::arrangeBars);
        define("tool.completeBars", "Completar y reducir compases con silencios…", dialogs::completeBarsWithRests);
        define("tool.automaticFingering", "Digitación automática…", dialogs::automaticFingering);
        define("tool.transpose", "Transponer…", dialogs::transpose);
        define("tool.checkBarDurations", "Verificar la duración de los compases", dialogs::checkBarDurations)
                .withAccelerator("F4");
        define("tool.scales", "Escalas…", dialogs::scales).withIcon(Icons.scales());
        define("tool.tuner", "Afinador…", dialogs::tuner).withIcon(Icons.tuner());
    }

    // ---- sonido -----------------------------------------------------------

    private void defineSoundCommands() {
        define("sound.play", "Reproducir / Detener", playback::togglePlay)
                .withAccelerator("SPACE").withIcon(Icons.play());
        define("sound.playFromStart", "Reproducir desde el principio", playback::playFromTheBeginning)
                .withAccelerator("ctrl SPACE");
        define("sound.loop", "Loop / Entrenador de velocidad…", playback::loopAndSpeedTrainer)
                .withAccelerator("F9").withIcon(Icons.loop());
        define("sound.tempo", "Tempo…", playback::tempo);
        define("sound.relativeTempo", "Tempo relativo…", playback::relativeTempo);
        define("sound.metronome", "Metrónomo", playback::toggleMetronome).withIcon(Icons.metronome());
        define("sound.countDown", "Cuenta regresiva", playback::toggleCountDown).withIcon(Icons.countDown());
        define("sound.stepBack", "Nota anterior", playback::stepBack);
        define("sound.midiInput", "Entrada MIDI activa", playback::toggleMidiInput);
        define("sound.stepForward", "Nota siguiente", playback::stepForward);
        define("nav.firstBar", "Primer compás", editor::moveToFirstMeasure)
                .withAccelerator("ctrl HOME").withIcon(Icons.firstBar());
        define("nav.previousBar", "Compás anterior", editor::moveToPreviousMeasure)
                .withAccelerator("ctrl LEFT").withIcon(Icons.previousBar());
        define("nav.nextBar", "Compás siguiente", editor::moveToNextMeasure)
                .withAccelerator("ctrl RIGHT").withIcon(Icons.nextBar());
        define("nav.nextNote", "Nota siguiente", editor::moveRight).withAccelerator("ENTER");
        define("nav.lastBar", "Último compás", editor::moveToLastMeasure)
                .withAccelerator("ctrl END").withIcon(Icons.lastBar());
    }

    // ---- vista ------------------------------------------------------------

    private void defineViewCommands() {
        define("view.page", "Modo página", view::pageMode).withIcon(Icons.pageMode());
        define("view.parchment", "Modo pergamino", view::parchmentMode).withIcon(Icons.parchmentMode());
        define("view.verticalScreen", "Pantalla vertical", view::verticalScreenMode)
                .withIcon(Icons.verticalScreen());
        define("view.horizontalScreen", "Pantalla horizontal", view::horizontalScreenMode)
                .withIcon(Icons.horizontalScreen());
        define("view.zoomIn", "Acercar", view::zoomIn).withAccelerator("ctrl EQUALS").withIcon(Icons.zoomIn());
        define("view.zoomOut", "Alejar", view::zoomOut).withAccelerator("ctrl MINUS").withIcon(Icons.zoomOut());
        define("view.resetZoom", "Zoom al 100%", view::resetZoom).withAccelerator("ctrl 0").withIcon(Icons.zoomReset());
        define("view.multitrack", "Vista multipista", view::toggleMultitrack).withIcon(Icons.multitrack());
        define("view.grayInactiveVoice", "Atenuar la voz inactiva", view::toggleGrayInactiveVoice)
                .withAccelerator("ctrl G");
        define("view.hideStandardNotation", "Ocultar el pentagrama", view::toggleStandardNotation);
        define("view.hideTablature", "Ocultar la tablatura", view::toggleTablature);
        define("view.fretboard", "Diapasón", view::toggleFretboard).withAccelerator("ctrl 3")
                .withIcon(Icons.fretboard());
        define("view.keyboard", "Teclado", view::toggleKeyboard).withAccelerator("ctrl 4")
                .withIcon(Icons.keyboard());
        define("view.percussion", "Asistente de percusión", view::togglePercussionAssistant);
        define("view.mixTable", "Mesa de mezcla", view::toggleMixTable).withIcon(Icons.mixTable());
        define("view.toolBars", "Barras de herramientas", view::toggleToolBars);
        for (String theme : themes) {
            define("view.theme." + theme, theme, () -> view.useTheme(theme));
        }
        define("options.midiSetup", "Configuración MIDI…", dialogs::midiSetup);
        define("options.preferences", "Preferencias…", dialogs::preferences).withAccelerator("F12");
    }

    private void defineHelpCommands() {
        define("help.contents", "Ayuda de tabpro", dialogs::help).withAccelerator("F1");
        define("help.about", "Acerca de tabpro", dialogs::about);
    }

    private Command define(String name, String label, Runnable body) {
        Command command = Command.named(label, body);
        commands.put(name, command);
        return command;
    }
}

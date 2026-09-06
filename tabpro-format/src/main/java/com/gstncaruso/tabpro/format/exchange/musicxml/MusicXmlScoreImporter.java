package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Lee una partitura en MusicXML. Si el archivo trae la cuerda y el traste, se
 * respetan; si no, cada altura se ubica en el diapason de la afinacion elegida.
 */
public final class MusicXmlScoreImporter {

    private static final int DEFAULT_TEMPO = 120;

    public Score importScore(Path path) {
        try {
            return importScore(readDocument(path));
        } catch (IOException e) {
            throw new ScoreFileException("no se pudo leer " + path, e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ScoreFileException("el archivo " + path + " no es MusicXML válido", e);
        }
    }

    private static Document readDocument(Path path)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (var input = Files.newInputStream(path)) {
            return factory.newDocumentBuilder().parse(input);
        }
    }

    Score importScore(Document document) {
        List<Element> parts = elementsNamed(document.getDocumentElement(), "part");
        if (parts.isEmpty()) {
            throw new ScoreFileException("el archivo no tiene ninguna parte");
        }
        Map<String, String> partNames = partNames(document);
        List<Track> tracks = new ArrayList<>();
        for (Element part : parts) {
            tracks.add(trackOf(part, partNames.getOrDefault(part.getAttribute("id"), "Pista")));
        }
        return new Score(infoOf(document), DEFAULT_TEMPO, tracks,
                com.gstncaruso.tabpro.core.model.Lyrics.none());
    }

    private static ScoreInfo infoOf(Document document) {
        ScoreInfo info = ScoreInfo.titled(textOf(document.getDocumentElement(), "work-title").orElse(""));
        for (Element creator : elementsNamed(document.getDocumentElement(), "creator")) {
            String type = creator.getAttribute("type");
            String name = creator.getTextContent().strip();
            info = switch (type) {
                case "composer" -> info.withMusicAuthor(name);
                case "lyricist" -> info.withLyricsAuthor(name);
                default -> info.withArtist(name);
            };
        }
        return textOf(document.getDocumentElement(), "rights").map(info::withCopyright).orElse(info);
    }

    private static Map<String, String> partNames(Document document) {
        Map<String, String> names = new LinkedHashMap<>();
        for (Element part : elementsNamed(document.getDocumentElement(), "score-part")) {
            names.put(part.getAttribute("id"), textOf(part, "part-name").orElse("Pista"));
        }
        return names;
    }

    private static Track trackOf(Element part, String name) {
        Tuning tuning = tuningOf(part);
        List<Measure> measures = new ArrayList<>();
        TimeSignature timeSignature = TimeSignature.fourFour();
        KeySignature keySignature = KeySignature.cMajor();
        int divisions = Duration.TICKS_PER_QUARTER / 4;
        for (Element measure : elementsNamed(part, "measure")) {
            divisions = intOf(measure, "divisions").orElse(divisions);
            timeSignature = timeSignatureOf(measure).orElse(timeSignature);
            keySignature = keySignatureOf(measure).orElse(keySignature);
            measures.add(measureOf(measure, timeSignature, tuning, keySignature));
        }
        if (measures.isEmpty()) {
            measures.add(Measure.empty(TimeSignature.fourFour(), Duration.quarter()));
        }
        return new Track(name, tuning, Channel.playing(Track.GUITAR_PROGRAM), measures);
    }

    /** La afinacion sale del staff-details; si no esta, se asume una guitarra. */
    private static Tuning tuningOf(Element part) {
        List<Element> strings = elementsNamed(part, "staff-tuning");
        if (strings.isEmpty()) {
            return Tuning.standard();
        }
        // La linea mas alta de la tablatura es la cuerda 1, la mas aguda.
        List<Element> fromTheTopDown = new ArrayList<>(strings);
        fromTheTopDown.sort(java.util.Comparator.comparingInt(
                (Element string) -> asInteger(string.getAttribute("line")).orElse(0)).reversed());
        List<Pitch> pitches = new ArrayList<>();
        for (Element string : fromTheTopDown) {
            char step = textOf(string, "tuning-step").orElse("E").charAt(0);
            int alter = intOf(string, "tuning-alter").orElse(0);
            int octave = intOf(string, "tuning-octave").orElse(4);
            pitches.add(PitchSpelling.pitchOf(step, alter, octave));
        }
        return TuningLibrary.identify(pitches);
    }

    private static Optional<TimeSignature> timeSignatureOf(Element measure) {
        Optional<Integer> beats = intOf(measure, "beats");
        Optional<Integer> beatType = intOf(measure, "beat-type");
        return beats.isPresent() && beatType.isPresent()
                ? Optional.of(new TimeSignature(beats.get(), beatType.get()))
                : Optional.empty();
    }

    /** La armadura vale hasta el proximo &lt;key&gt;, tal como en el resto de MusicXML. */
    private static Optional<KeySignature> keySignatureOf(Element measure) {
        return intOf(measure, "fifths").map(fifths -> new KeySignature(fifths, modeOf(measure)));
    }

    private static Mode modeOf(Element measure) {
        return "minor".equals(textOf(measure, "mode").orElse("major")) ? Mode.MINOR : Mode.MAJOR;
    }

    private static Measure measureOf(
            Element measure, TimeSignature timeSignature, Tuning tuning, KeySignature keySignature) {
        List<Beat> beats = new ArrayList<>();
        for (Element note : elementsNamed(measure, "note")) {
            if (hasChild(note, "chord") && !beats.isEmpty()) {
                int last = beats.size() - 1;
                noteOf(note, tuning).ifPresent(played -> beats.set(last, beats.get(last).withNote(played)));
                continue;
            }
            Duration duration = durationOf(note);
            if (hasChild(note, "rest")) {
                beats.add(Beat.rest(duration));
                continue;
            }
            Beat beat = Beat.rest(duration);
            Optional<Note> played = noteOf(note, tuning);
            beats.add(played.map(beat::withNote).orElse(beat));
        }
        if (beats.isEmpty()) {
            beats.add(Beat.rest(Duration.quarter()));
        }
        return new Measure(timeSignature, beats).mappingAttributes(attrs -> attrs.withKeySignature(keySignature));
    }

    private static Duration durationOf(Element note) {
        var value = textOf(note, "type").map(NoteTypeNames::fromXml)
                .orElse(com.gstncaruso.tabpro.core.model.NoteValue.QUARTER);
        Tuplet tuplet = intOf(note, "actual-notes")
                .filter(Tuplet.AVAILABLE::contains)
                .map(Tuplet::of)
                .orElse(Tuplet.none());
        return new Duration(value, hasChild(note, "dot"), tuplet);
    }

    private static Optional<Note> noteOf(Element note, Tuning tuning) {
        Optional<Integer> string = intOf(note, "string");
        Optional<Integer> fret = intOf(note, "fret");
        boolean tied = elementsNamed(note, "tie").stream()
                .anyMatch(tie -> "stop".equals(tie.getAttribute("type")));
        if (string.isPresent() && fret.isPresent() && string.get() <= tuning.stringCount()) {
            return Optional.of(new Note(string.get(), Math.clamp(fret.get(), 0, Note.MAX_FRET))
                    .tied(tied));
        }
        return pitchOf(note)
                .flatMap(pitch -> tuning.bestNoteFor(pitch, Tuning.MAX_FRET))
                .map(placed -> placed.tied(tied));
    }

    private static Optional<Pitch> pitchOf(Element note) {
        Optional<String> step = textOf(note, "step");
        Optional<Integer> octave = intOf(note, "octave");
        if (step.isEmpty() || octave.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(PitchSpelling.pitchOf(
                step.get().charAt(0), intOf(note, "alter").orElse(0), octave.get()));
    }

    // ---- lo minimo de DOM que hace falta ----------------------------------

    private static List<Element> elementsNamed(Element parent, String name) {
        List<Element> found = new ArrayList<>();
        NodeList children = parent.getElementsByTagName(name);
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element) {
                found.add(element);
            }
        }
        return found;
    }

    private static boolean hasChild(Element parent, String name) {
        return !elementsNamed(parent, name).isEmpty();
    }

    private static Optional<String> textOf(Element parent, String name) {
        return elementsNamed(parent, name).stream().findFirst()
                .map(element -> element.getTextContent().strip())
                .filter(text -> !text.isEmpty());
    }

    private static Optional<Integer> intOf(Element parent, String name) {
        return textOf(parent, name).flatMap(MusicXmlScoreImporter::asInteger);
    }

    private static Optional<Integer> asInteger(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

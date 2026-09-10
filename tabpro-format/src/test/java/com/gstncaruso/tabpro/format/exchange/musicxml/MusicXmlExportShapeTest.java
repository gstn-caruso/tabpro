package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class MusicXmlExportShapeTest {

    private final MusicXmlScoreExporter exporter = new MusicXmlScoreExporter();

    @Test
    void theTieComesAfterTheDurationAndBeforeTheType() throws Exception {
        Beat attackedBeat = Beat.of(Duration.quarter(), new Note(3, 7));
        Beat tiedBeat = Beat.of(Duration.quarter(), new Note(3, 7).tied(true));
        Score score = scoreWith(attackedBeat, tiedBeat);

        Document document = parse(exporter.toXml(score));
        Element noteWithTie = notesWithChild(document, "tie").get(0);
        List<String> children = childNames(noteWithTie);

        assertTrue(children.indexOf("tie") > children.indexOf("duration"),
                "<tie> must come after <duration>: " + children);
        assertTrue(children.indexOf("tie") < children.indexOf("type"),
                "the content model of <note> in the MusicXML DTD places (tie, tie?) before type/dot/"
                        + "time-modification, not after: " + children);
    }

    @Test
    void theChildrenOfAttributesFollowTheSchemaOrder() throws Exception {
        Score score = scoreWith(Beat.rest(Duration.quarter()));

        Document document = parse(exporter.toXml(score));
        Element attributes = (Element) document.getElementsByTagName("attributes").item(0);

        assertEquals(List.of("divisions", "key", "time", "staves", "clef", "staff-details"),
                childNames(attributes).stream().distinct().toList(),
                "the DTD requires divisions?, key*, time*, staves?, ..., clef*, staff-details* in that order");
    }

    @Test
    void aBarThatChangesNothingWritesNoAttributes() throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC());

        Document document = parse(exporter.toXml(score));

        assertEquals(1, document.getElementsByTagName("attributes").getLength(),
                "the second measure changes neither the key signature nor the time signature: it must not write <attributes>");
    }

    @Test
    void aChangeOfOnlyTheTimeSignatureDoesNotRepeatTheKeySignature() throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC())
                .withTimeSignatureFrom(1, new TimeSignature(3, 4));

        Document document = parse(exporter.toXml(score));
        Element part = (Element) document.getElementsByTagName("part").item(0);
        Element secondBar = elementsNamed(part, "measure").get(1);
        Element attributes = firstChild(secondBar, "attributes").orElseThrow();

        assertEquals(List.of("time"), childNames(attributes),
                "only the time signature changed: <attributes> must not repeat <key> if the key signature stays the same");
    }

    @Test
    void theDurationOfEachFigureIsConsistentWithTheDeclaredDivisions() throws Exception {
        List<Duration> figures = List.of(
                Duration.of(NoteValue.WHOLE),
                Duration.of(NoteValue.HALF),
                Duration.quarter(),
                Duration.of(NoteValue.EIGHTH),
                new Duration(NoteValue.EIGHTH, true),
                Duration.of(NoteValue.EIGHTH).in(Tuplet.of(3)),
                Duration.of(NoteValue.SIXTEENTH));
        Beat[] beats = figures.stream().map(figure -> Beat.of(figure, new Note(1, 0))).toArray(Beat[]::new);
        Score score = scoreWith(beats);

        Document document = parse(exporter.toXml(score));
        int divisions = Integer.parseInt(document.getElementsByTagName("divisions").item(0).getTextContent().strip());
        List<Element> notes = elementsNamed(document.getDocumentElement(), "note");

        for (int i = 0; i < notes.size(); i++) {
            long declaredDuration = Long.parseLong(textOf(notes.get(i), "duration").orElseThrow());
            long expectedDuration = expectedDurationUnits(notes.get(i), divisions);
            assertEquals(expectedDuration, declaredDuration, "figure " + i + ": " + figures.get(i));
        }
    }

    private static Score scoreWith(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Test", 120, List.of(track));
    }

    private static Score scoreWithMeasures(Measure... measures) {
        Track track = new Track("Guitar", Tuning.standard(), Channel.playing(25), List.of(measures));
        return new Score("Test", 120, List.of(track));
    }

    private static Measure measureInC() {
        return new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static long expectedDurationUnits(Element note, int divisions) {
        int denominator = switch (textOf(note, "type").orElseThrow()) {
            case "whole" -> 1;
            case "half" -> 2;
            case "quarter" -> 4;
            case "eighth" -> 8;
            case "16th" -> 16;
            case "32nd" -> 32;
            case "64th" -> 64;
            default -> throw new IllegalStateException("figure not covered by this test: "
                    + textOf(note, "type").orElse("?"));
        };
        long numerator = 4L * divisions;
        long denom = denominator;
        if (hasChild(note, "dot")) {
            numerator *= 3;
            denom *= 2;
        }
        Optional<Element> timeModification = firstChild(note, "time-modification");
        if (timeModification.isPresent()) {
            int actual = Integer.parseInt(textOf(timeModification.get(), "actual-notes").orElseThrow());
            int normal = Integer.parseInt(textOf(timeModification.get(), "normal-notes").orElseThrow());
            numerator *= normal;
            denom *= actual;
        }
        assertEquals(0, numerator % denom,
                "with divisions=" + divisions + " this figure cannot be written without a remainder");
        return numerator / denom;
    }

    private static List<Element> notesWithChild(Document document, String childName) {
        return elementsNamed(document.getDocumentElement(), "note").stream()
                .filter(note -> hasChild(note, childName))
                .toList();
    }

    private static List<String> childNames(Element element) {
        List<String> names = new ArrayList<>();
        NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element childElement) {
                names.add(childElement.getTagName());
            }
        }
        return names;
    }

    private static List<Element> elementsNamed(Element parent, String name) {
        List<Element> found = new ArrayList<>();
        NodeList children = parent.getElementsByTagName(name);
        for (int index = 0; index < children.getLength(); index++) {
            found.add((Element) children.item(index));
        }
        return found;
    }

    private static Optional<Element> firstChild(Element parent, String name) {
        List<Element> found = elementsNamed(parent, name);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    private static boolean hasChild(Element parent, String name) {
        return !elementsNamed(parent, name).isEmpty();
    }

    private static Optional<String> textOf(Element parent, String name) {
        return elementsNamed(parent, name).stream().findFirst()
                .map(element -> element.getTextContent().strip())
                .filter(text -> !text.isEmpty());
    }
}

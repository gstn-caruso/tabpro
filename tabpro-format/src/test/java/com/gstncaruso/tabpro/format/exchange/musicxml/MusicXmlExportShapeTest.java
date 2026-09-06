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

/**
 * {@link MusicXmlRoundTripTest} solo prueba que lo que exportamos nos lo podamos leer a
 * nosotros mismos. Esta clase no usa el importador de tabpro para nada: verifica, leyendo el
 * XML con un parser DOM crudo, que lo que escribimos tiene la forma que el estandar exige
 * -sin la XSD a mano, pero con lo que el DTD de MusicXML fija de memoria: orden de hijos donde
 * el esquema lo pide, y que <divisions> sea coherente con las <duration> que se escriben.
 */
class MusicXmlExportShapeTest {

    private final MusicXmlScoreExporter exporter = new MusicXmlScoreExporter();

    @Test
    void elTieVaDespuesDeLaDuracionYAntesDelType() throws Exception {
        Beat atacada = Beat.of(Duration.quarter(), new Note(3, 7));
        Beat continuada = Beat.of(Duration.quarter(), new Note(3, 7).tied(true));
        Score score = scoreWith(atacada, continuada);

        Document document = parse(exporter.toXml(score));
        Element notaConTie = notesWithChild(document, "tie").get(0);
        List<String> hijos = childNames(notaConTie);

        assertTrue(hijos.indexOf("tie") > hijos.indexOf("duration"),
                "<tie> tiene que venir despues de <duration>: " + hijos);
        assertTrue(hijos.indexOf("tie") < hijos.indexOf("type"),
                "el content model de <note> en el DTD de MusicXML pone (tie, tie?) antes de type/dot/"
                        + "time-modification, no despues: " + hijos);
    }

    @Test
    void losHijosDeAttributesSiguenElOrdenDelEsquema() throws Exception {
        Score score = scoreWith(Beat.rest(Duration.quarter()));

        Document document = parse(exporter.toXml(score));
        Element attributes = (Element) document.getElementsByTagName("attributes").item(0);

        assertEquals(List.of("divisions", "key", "time", "staves", "clef", "staff-details"),
                childNames(attributes).stream().distinct().toList(),
                "el DTD exige divisions?, key*, time*, staves?, ..., clef*, staff-details* en ese orden");
    }

    @Test
    void unCompasQueNoCambiaNadaNoEscribeAttributes() throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC());

        Document document = parse(exporter.toXml(score));

        assertEquals(1, document.getElementsByTagName("attributes").getLength(),
                "el segundo compas no cambia ni la armadura ni el compas: no tiene que escribir <attributes>");
    }

    @Test
    void unCambioDeSoloElCompasNoRepiteLaArmadura() throws Exception {
        Score score = scoreWithMeasures(measureInC(), measureInC())
                .withTimeSignatureFrom(1, new TimeSignature(3, 4));

        Document document = parse(exporter.toXml(score));
        Element part = (Element) document.getElementsByTagName("part").item(0);
        Element segundoCompas = elementsNamed(part, "measure").get(1);
        Element attributes = firstChild(segundoCompas, "attributes").orElseThrow();

        assertEquals(List.of("time"), childNames(attributes),
                "solo cambio el compas: <attributes> no tiene que repetir <key> si la armadura sigue igual");
    }

    @Test
    void laDuracionDeCadaFiguraEsCoherenteConLasDivisionsDeclaradas() throws Exception {
        List<Duration> figuras = List.of(
                Duration.of(NoteValue.WHOLE),
                Duration.of(NoteValue.HALF),
                Duration.quarter(),
                Duration.of(NoteValue.EIGHTH),
                new Duration(NoteValue.EIGHTH, true),
                Duration.of(NoteValue.EIGHTH).in(Tuplet.of(3)),
                Duration.of(NoteValue.SIXTEENTH));
        Beat[] beats = figuras.stream().map(figura -> Beat.of(figura, new Note(1, 0))).toArray(Beat[]::new);
        Score score = scoreWith(beats);

        Document document = parse(exporter.toXml(score));
        int divisions = Integer.parseInt(document.getElementsByTagName("divisions").item(0).getTextContent().strip());
        List<Element> notas = elementsNamed(document.getDocumentElement(), "note");

        for (int i = 0; i < notas.size(); i++) {
            long declarado = Long.parseLong(textOf(notas.get(i), "duration").orElseThrow());
            long esperado = expectedDurationUnits(notas.get(i), divisions);
            assertEquals(esperado, declarado, "figura " + i + ": " + figuras.get(i));
        }
    }

    // ---- lo que necesita esta clase, sin depender del importador ---------

    private static Score scoreWith(Beat... beats) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beats));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        return new Score("Prueba", 120, List.of(track));
    }

    private static Score scoreWithMeasures(Measure... measures) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measures));
        return new Score("Prueba", 120, List.of(track));
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

    /** Ticks esperados en <duration>, calculados a partir de lo que dice el propio <type> del note,
     * no de la formula interna del exportador: divisions es "partes por negra", tal como lo define
     * el estandar. */
    private static long expectedDurationUnits(Element note, int divisions) {
        int denominator = switch (textOf(note, "type").orElseThrow()) {
            case "whole" -> 1;
            case "half" -> 2;
            case "quarter" -> 4;
            case "eighth" -> 8;
            case "16th" -> 16;
            case "32nd" -> 32;
            case "64th" -> 64;
            default -> throw new IllegalStateException("figura no contemplada en este test: "
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
                "con divisions=" + divisions + " esta figura no se puede escribir sin resto");
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

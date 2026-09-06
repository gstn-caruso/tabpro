package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Escribe la partitura en MusicXML: notacion estandar mas la tablatura, que
 * MusicXML representa con la cuerda y el traste dentro de {@code <technical>}.
 */
public final class MusicXmlScoreExporter {

    /** Las divisiones por negra con las que se escriben las duraciones. */
    private static final int DIVISIONS = Duration.TICKS_PER_QUARTER / 4;

    public void export(Score score, Path path) {
        try {
            Files.writeString(path, toXml(score), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new com.gstncaruso.tabpro.core.files.ScoreFileException("no se pudo escribir " + path, e);
        }
    }

    public String toXml(Score score) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE score-partwise PUBLIC \"-//Recordare//DTD MusicXML 3.1 Partwise//EN\"")
                .append(" \"http://www.musicxml.org/dtds/partwise.dtd\">\n");
        xml.append("<score-partwise version=\"3.1\">\n");
        appendWork(xml, score);
        appendPartList(xml, score);
        for (int index = 0; index < score.trackCount(); index++) {
            appendPart(xml, score.track(index), index);
        }
        xml.append("</score-partwise>\n");
        return xml.toString();
    }

    private static void appendWork(StringBuilder xml, Score score) {
        xml.append("  <work><work-title>").append(escape(score.info().title())).append("</work-title></work>\n");
        xml.append("  <identification>\n");
        appendCreator(xml, "composer", score.info().musicAuthor());
        appendCreator(xml, "lyricist", score.info().lyricsAuthor());
        if (!score.info().copyright().isBlank()) {
            xml.append("    <rights>").append(escape(score.info().copyright())).append("</rights>\n");
        }
        xml.append("    <encoding><software>tabpro</software></encoding>\n");
        xml.append("  </identification>\n");
    }

    private static void appendCreator(StringBuilder xml, String type, String name) {
        if (!name.isBlank()) {
            xml.append("    <creator type=\"").append(type).append("\">")
                    .append(escape(name)).append("</creator>\n");
        }
    }

    private static void appendPartList(StringBuilder xml, Score score) {
        xml.append("  <part-list>\n");
        for (int index = 0; index < score.trackCount(); index++) {
            Track track = score.track(index);
            xml.append("    <score-part id=\"P").append(index + 1).append("\">\n");
            xml.append("      <part-name>").append(escape(track.name())).append("</part-name>\n");
            xml.append("      <midi-instrument id=\"P").append(index + 1).append("\">\n");
            xml.append("        <midi-channel>").append(track.channel().number()).append("</midi-channel>\n");
            xml.append("        <midi-program>").append(track.channel().program() + 1).append("</midi-program>\n");
            xml.append("      </midi-instrument>\n");
            xml.append("    </score-part>\n");
        }
        xml.append("  </part-list>\n");
    }

    private static void appendPart(StringBuilder xml, Track track, int index) {
        xml.append("  <part id=\"P").append(index + 1).append("\">\n");
        for (int measureIndex = 0; measureIndex < track.measureCount(); measureIndex++) {
            appendMeasure(xml, track, measureIndex);
        }
        xml.append("  </part>\n");
    }

    private static void appendMeasure(StringBuilder xml, Track track, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        xml.append("    <measure number=\"").append(measureIndex + 1).append("\">\n");
        if (measureIndex == 0) {
            appendAttributes(xml, track, measure);
        }
        List<Beat> beats = measure.beats();
        Map<Integer, Set<Integer>> ties = TieStarts.of(beats);
        Map<Integer, TupletRuns.Mark> tuplets = TupletRuns.of(beats);
        for (int beatIndex = 0; beatIndex < beats.size(); beatIndex++) {
            appendBeat(xml, track, beats.get(beatIndex),
                    ties.getOrDefault(beatIndex, Set.of()), tuplets.get(beatIndex));
        }
        xml.append("    </measure>\n");
    }

    private static void appendAttributes(StringBuilder xml, Track track, Measure measure) {
        xml.append("      <attributes>\n");
        xml.append("        <divisions>").append(DIVISIONS).append("</divisions>\n");
        xml.append("        <key><fifths>")
                .append(measure.attributes().keySignature().accidentals()).append("</fifths></key>\n");
        xml.append("        <time><beats>").append(measure.timeSignature().beats())
                .append("</beats><beat-type>").append(measure.timeSignature().beatUnit())
                .append("</beat-type></time>\n");
        xml.append("        <staves>2</staves>\n");
        xml.append("        <clef number=\"1\"><sign>G</sign><line>2</line>")
                .append("<clef-octave-change>-1</clef-octave-change></clef>\n");
        xml.append("        <clef number=\"2\"><sign>TAB</sign><line>5</line></clef>\n");
        appendStaffDetails(xml, track);
        xml.append("      </attributes>\n");
    }

    /** La afinacion de la tablatura, cuerda por cuerda. */
    private static void appendStaffDetails(StringBuilder xml, Track track) {
        xml.append("        <staff-details number=\"2\">\n");
        xml.append("          <staff-lines>").append(track.stringCount()).append("</staff-lines>\n");
        for (int string = 1; string <= track.stringCount(); string++) {
            PitchSpelling.Spelling open = PitchSpelling.spell(track.tuning().pitchOfString(string), false);
            xml.append("          <staff-tuning line=\"").append(track.stringCount() - string + 1).append("\">")
                    .append("<tuning-step>").append(open.step()).append("</tuning-step>");
            if (open.alter() != 0) {
                xml.append("<tuning-alter>").append(open.alter()).append("</tuning-alter>");
            }
            xml.append("<tuning-octave>").append(open.octave()).append("</tuning-octave></staff-tuning>\n");
        }
        xml.append("        </staff-details>\n");
    }

    private static void appendBeat(
            StringBuilder xml, Track track, Beat beat, Set<Integer> tiedStrings, TupletRuns.Mark tuplet) {
        if (beat.isRest()) {
            appendRest(xml, beat);
            return;
        }
        boolean first = true;
        for (Note note : beat.notes()) {
            appendNote(xml, track, beat, note, first, tiedStrings.contains(note.string()), tuplet);
            first = false;
        }
    }

    private static void appendRest(StringBuilder xml, Beat beat) {
        xml.append("      <note>\n        <rest/>\n");
        appendDurationAndType(xml, beat.duration());
        xml.append("      </note>\n");
    }

    private static void appendNote(
            StringBuilder xml,
            Track track,
            Beat beat,
            Note note,
            boolean first,
            boolean tied,
            TupletRuns.Mark tuplet) {
        xml.append("      <note>\n");
        if (!first) {
            xml.append("        <chord/>\n");
        }
        PitchSpelling.Spelling spelling = PitchSpelling.spell(track.pitchOf(note), false);
        xml.append("        <pitch><step>").append(spelling.step()).append("</step>");
        if (spelling.alter() != 0) {
            xml.append("<alter>").append(spelling.alter()).append("</alter>");
        }
        xml.append("<octave>").append(spelling.octave()).append("</octave></pitch>\n");
        appendDuration(xml, beat.duration());
        if (tied || note.tied()) {
            xml.append("        <tie type=\"").append(note.tied() ? "stop" : "start").append("\"/>\n");
        }
        appendTypeAndModifiers(xml, beat.duration());
        appendNotations(xml, note, tied, tuplet);
        xml.append("      </note>\n");
    }

    /**
     * El content model de {@code <note>} del DTD de MusicXML pone {@code duration, (tie, tie?)?}
     * antes de {@code type, dot*, time-modification}: por eso {@code <tie>} no puede salir junto
     * con el resto de {@link #appendTypeAndModifiers}.
     */
    private static void appendDurationAndType(StringBuilder xml, Duration duration) {
        appendDuration(xml, duration);
        appendTypeAndModifiers(xml, duration);
    }

    private static void appendDuration(StringBuilder xml, Duration duration) {
        long divisions = duration.ticks() * DIVISIONS / Duration.TICKS_PER_QUARTER;
        xml.append("        <duration>").append(Math.max(1, divisions)).append("</duration>\n");
    }

    private static void appendTypeAndModifiers(StringBuilder xml, Duration duration) {
        xml.append("        <type>").append(NoteTypeNames.toXml(duration.value())).append("</type>\n");
        if (duration.dotted()) {
            xml.append("        <dot/>\n");
        }
        if (!duration.tuplet().isPlain()) {
            xml.append("        <time-modification><actual-notes>").append(duration.tuplet().enters())
                    .append("</actual-notes><normal-notes>").append(duration.tuplet().inTheTimeOf())
                    .append("</normal-notes></time-modification>\n");
        }
    }

    private static void appendNotations(StringBuilder xml, Note note, boolean tied, TupletRuns.Mark tuplet) {
        boolean hasTuplet = tuplet != null && (tuplet.start() || tuplet.stop());
        xml.append("        <notations>\n");
        if (tied || note.tied()) {
            xml.append("          <tied type=\"").append(note.tied() ? "stop" : "start").append("\"/>\n");
        }
        if (hasTuplet) {
            xml.append("          <tuplet type=\"").append(tuplet.start() ? "start" : "stop").append("\"/>\n");
        }
        xml.append("          <technical><string>").append(note.string()).append("</string>")
                .append("<fret>").append(note.fret()).append("</fret></technical>\n");
        xml.append("        </notations>\n");
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

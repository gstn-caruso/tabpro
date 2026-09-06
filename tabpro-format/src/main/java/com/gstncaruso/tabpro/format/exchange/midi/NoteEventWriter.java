package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Note;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Escribe los eventos de nota de una voz en una pista MIDI. Una nota atada (tied) no ataca de
 * nuevo: estira el apagado de la nota anterior de esa misma cuerda, tal como dice su javadoc
 * en el modelo. El "let ring" y el resto de los efectos de sonido se reflejan alargando o
 * acortando esa duracion real con {@link Note#soundLength()}.
 */
final class NoteEventWriter {

    private record RingingNote(int soundOrPitch, long endTick) {
    }

    private final Track midiTrack;
    private final int channel;
    private final Map<Integer, RingingNote> ringingByString = new HashMap<>();

    NoteEventWriter(Track midiTrack, int channel) {
        this.midiTrack = midiTrack;
        this.channel = channel;
    }

    /** Ataca (o extiende, si esta atada) la nota que suena durante ese beat. */
    void attack(long tick, long beatDurationTicks, Note note, int soundOrPitch) {
        long plannedEnd = tick + Math.max(1, Math.round(beatDurationTicks * note.soundLength()));
        RingingNote ringing = ringingByString.get(note.string());
        if (note.tied() && ringing != null) {
            ringingByString.put(note.string(), new RingingNote(ringing.soundOrPitch(), plannedEnd));
            return;
        }
        closeAt(note.string(), tick);
        add(ShortMessage.NOTE_ON, soundOrPitch, note.velocity().value(), tick);
        ringingByString.put(note.string(), new RingingNote(soundOrPitch, plannedEnd));
    }

    /** Corta todo lo que sigue sonando, como mucho hasta ese momento. */
    void closeEverythingAt(long tick) {
        for (Integer string : List.copyOf(ringingByString.keySet())) {
            closeAt(string, tick);
        }
    }

    private void closeAt(int string, long cutoffTick) {
        RingingNote ringing = ringingByString.remove(string);
        if (ringing == null) {
            return;
        }
        add(ShortMessage.NOTE_OFF, ringing.soundOrPitch(), 0, Math.min(ringing.endTick(), cutoffTick));
    }

    private void add(int command, int data1, int data2, long tick) {
        try {
            midiTrack.add(new MidiEvent(new ShortMessage(command, channel, data1, data2), tick));
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.gstncaruso.tabpro.format.guitarpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Tuplet;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.model.effects.StrokeDirection;
import com.gstncaruso.tabpro.core.model.effects.Wah;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuitarProBeatReaderTest {

    private static final int NO_FLAGS = 0x00;
    private static final int DOTTED = 0x01;
    private static final int HAS_EFFECTS = 0x08;
    private static final int WITH_TUPLET = 0x20;
    private static final int WITH_MIX_TABLE = 0x10;
    private static final int WITH_STATUS = 0x40;
    private static final int REST_STATUS = 0x02;

    private static final int QUARTER = 0;
    private static final int EIGHTH = 1;
    private static final int WHOLE = -2;

    /** La cuerda 1 ocupa el bit mas alto de la mascara. */
    private static final int ONLY_FIRST_STRING = 0x40;
    private static final int TWO_HIGHEST_STRINGS = 0x60;

    /** El unico dato que trae una nota normal es su tipo y su traste. */
    private static final int NOTE_WITH_FRET = 0x20;

    /** El bit del segundo byte de efectos que anuncia el sentido de la pua. */
    private static final int HAS_PICKSTROKE = 0x02;

    /** El bit del primer byte de efectos que marca el vibrato ancho del beat. */
    private static final int WIDE_VIBRATO = 0x02;

    /** El bit del beat que anuncia el rasgueo, y la velocidad que dice que no hay ninguno. */
    private static final int HAS_STROKE = 0x40;
    private static final int NO_STROKE = 0;

    /** El bit del beat que en GP3 comparten la palanca y el golpe, y los valores de ese byte. */
    private static final int TREMOLO_BAR_OR_SLAP = 0x20;
    private static final int NO_SLAP = 0;
    private static final int SLAPPING = 2;

    /** El bit del cambio de parametros que dice que usa el RSE, no que va a todas las pistas. */
    private static final int USES_RSE = 0x40;

    /** Guitar Pro escribe en -1 el parametro que el cambio no toca. */
    private static final int UNCHANGED = -1;
    private static final int NO_STRINGS = 0x00;
    private static final int NORMAL_NOTE = 1;
    private static final int TIED_NOTE = 2;
    private static final int DEAD_NOTE = 3;

    private final GuitarProBeatReader reader = new GuitarProBeatReader();

    @Test
    void aRestHasNoNotes() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_STATUS)
                .writeUnsignedByte(REST_STATUS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS));

        assertTrue(beat.isRest());
        assertEquals(NoteValue.QUARTER, beat.duration().value());
    }

    /**
     * El formato escribe la mascara de cuerdas en todo beat, tambien en el silencio.
     * Si el lector no la consume, se corre un byte y arruina todo lo que sigue.
     */
    @Test
    void unSilencioIgualTraeSuMascaraDeCuerdas() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_STATUS)
                .writeUnsignedByte(REST_STATUS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat silencio = reader.read(bytes, GuitarProVersion.GP4, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP4, 6);

        assertTrue(silencio.isRest());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    @Test
    void aDottedFigureIsMarkedInTheFlags() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(DOTTED)
                .writeSignedByte(EIGHTH)
                .writeUnsignedByte(0));

        assertEquals(NoteValue.EIGHTH, beat.duration().value());
        assertTrue(beat.duration().dotted());
    }

    @Test
    void aWholeNoteIsWrittenAsMinusTwo() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(WHOLE)
                .writeUnsignedByte(0));

        assertEquals(NoteValue.WHOLE, beat.duration().value());
    }

    @Test
    void aTripletComesAsItsOwnInteger() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_TUPLET)
                .writeSignedByte(EIGHTH)
                .writeInt(3)
                .writeUnsignedByte(0));

        assertEquals(Tuplet.of(3), beat.duration().tuplet());
    }

    @Test
    void anUnknownTupletFallsBackToAPlainFigure() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_TUPLET)
                .writeSignedByte(EIGHTH)
                .writeInt(4)
                .writeUnsignedByte(0));

        assertTrue(beat.duration().tuplet().isPlain());
    }

    @Test
    void theStringMaskSaysWhichStringsSound() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TWO_HIGHEST_STRINGS)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(5)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(7));

        assertEquals(2, beat.notes().size());
        assertEquals(5, beat.noteOn(1).orElseThrow().fret());
        assertEquals(7, beat.noteOn(2).orElseThrow().fret());
    }

    @Test
    void aTiedNoteIsNotPlayedAgain() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(TIED_NOTE).writeSignedByte(3));

        assertTrue(beat.noteOn(1).orElseThrow().tied());
    }

    @Test
    void aDeadNoteCarriesItsOrnament() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(DEAD_NOTE).writeSignedByte(0));

        assertTrue(beat.noteOn(1).orElseThrow()
                .has(com.gstncaruso.tabpro.core.model.effects.Ornament.DEAD));
        assertFalse(beat.isRest());
    }

    /**
     * El instrumento y el tempo van tal cual, pero el volumen es una perilla de la mesa:
     * viene en sus dieciseis pasos, igual que en la tabla de canales, y el modelo lo
     * maneja en los 0 a 127 de MIDI.
     */
    @Test
    void aMixTableChangeBecomesAParameterChange() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(30)
                .writeSignedByte(5)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(90)
                .writeSignedByte(2)
                .writeSignedByte(4)
                .writeUnsignedByte(NO_STRINGS));

        ParameterChange change = beat.effects().parameterChange();
        assertEquals(OptionalInt.of(30), change.valueOf(SoundParameter.PROGRAM));
        assertEquals(OptionalInt.of(40), change.valueOf(SoundParameter.VOLUME));
        assertEquals(OptionalInt.of(90), change.valueOf(SoundParameter.TEMPO));
        assertFalse(change.changes(SoundParameter.PAN), "lo que viene en -1 no cambia");
    }

    /** El paneo al centro, que en el archivo es el paso 8 de la perilla y en MIDI el 64. */
    @Test
    void aMixTableChangeCentersThePanOnTheEighthStep() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(8)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeSignedByte(0)
                .writeUnsignedByte(NO_STRINGS));

        assertEquals(OptionalInt.of(64),
                beat.effects().parameterChange().valueOf(SoundParameter.PAN));
    }

    @Test
    void theLongestOfTheTransitionsIsTheOneThatCounts() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(40)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(90)
                .writeSignedByte(2)
                .writeSignedByte(4)
                .writeUnsignedByte(NO_STRINGS));

        assertEquals(4, beat.effects().parameterChange().transitionBeats());
    }

    @Test
    void aBeatWithoutAMixTableChangeChangesNothing() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_STRINGS));

        assertTrue(beat.effects().parameterChange().isEmpty());
    }

    @Test
    void theMaskOfTracksDecidesIfTheChangeIsForEveryTrack() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(40)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeSignedByte(0)
                .writeUnsignedByte(0x01)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().parameterChange().everyTrack());
    }

    /**
     * De ese byte, "para todas las pistas" son solo los seis bits de las perillas. Los
     * dos de arriba son otra cosa -- que el cambio use el RSE y que el wah se muestre en
     * la partitura -- y estan puestos en casi todo .gp5.
     */
    @Test
    void theTopBitsOfTheMaskAreNotAboutEveryTrack() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(30)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeUnsignedByte(USES_RSE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertFalse(beat.effects().parameterChange().everyTrack());
    }

    // ---- el wah del cambio de parametros, que existe recien en GP5 -----------

    @Test
    void gp3AndGp4NeverBringAWah() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED).writeSignedByte(UNCHANGED)
                .writeInt(UNCHANGED)
                .writeUnsignedByte(0x00)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().wah().isEmpty());
    }

    @Test
    void minusTwoTurnsTheWahOff() {
        Beat beat = read(gp5Beat(-2, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OFF, beat.effects().wah().orElseThrow());
    }

    @Test
    void zeroIsAClosedWah() {
        Beat beat = read(gp5Beat(0, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.CLOSED, beat.effects().wah().orElseThrow());
    }

    @Test
    void oneHundredIsAnOpenWah() {
        Beat beat = read(gp5Beat(100, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OPEN, beat.effects().wah().orElseThrow());
    }

    /** El pedal a mitad de camino redondea al mas cercano de los dos extremos que tabpro distingue. */
    @Test
    void justBelowHalfwayIsStillClosed() {
        Beat beat = read(gp5Beat(49, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.CLOSED, beat.effects().wah().orElseThrow());
    }

    @Test
    void halfwayIsAlreadyOpen() {
        Beat beat = read(gp5Beat(50, true), GuitarProVersion.GP5_10);

        assertEquals(Wah.OPEN, beat.effects().wah().orElseThrow());
    }

    /** -1 es que este cambio de parametros no toca el wah, como el resto de los parametros. */
    @Test
    void unchangedWahIsNotBroughtAtAll() {
        Beat beat = read(gp5Beat(UNCHANGED, true), GuitarProVersion.GP5_10);

        assertTrue(beat.effects().wah().isEmpty());
    }

    /** En 5.00 el cambio de parametros no trae nombre ni categoria del efecto de RSE. */
    @Test
    void gp500HasNoRseInstrumentEffectAfterTheWah() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5Beat(100, false)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat wahBeat = reader.read(bytes, GuitarProVersion.GP5_00, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_00, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /** En 5.10 el cambio de parametros agrega nombre y categoria del efecto de RSE tras el wah. */
    @Test
    void gp510KeepsTheAlignmentAfterTheRseInstrumentEffect() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5Beat(100, true)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat wahBeat = reader.read(bytes, GuitarProVersion.GP5_10, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_10, 6);

        assertEquals(Wah.OPEN, wahBeat.effects().wah().orElseThrow());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    // ---- la pua y el vibrato ancho, que valen para todo el beat -------------

    /** El sentido de la pua es un numero: 1 hacia arriba y 2 hacia abajo, no un signo. */
    @ParameterizedTest
    @CsvSource({"1, UP", "2, DOWN"})
    void thePickstrokeSaysWhichWayThePickGoes(int written, PickstrokeDirection expected) {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_FLAGS).writeUnsignedByte(HAS_PICKSTROKE)
                .writeSignedByte(written)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(expected, beat.effects().pickstroke().orElseThrow());
    }

    @Test
    void aPickstrokeInZeroIsNoPickstrokeAtAll() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(NO_FLAGS).writeUnsignedByte(HAS_PICKSTROKE)
                .writeSignedByte(0)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().pickstroke().isEmpty());
    }

    /**
     * El vibrato ancho es del beat entero y viene en el mismo bit en las tres
     * generaciones, no solo en GP3.
     */
    @ParameterizedTest
    @CsvSource({"GP3", "GP4", "GP5_10"})
    void theWideVibratoIsTheSameBitInEveryGeneration(GuitarProVersion version) {
        GuitarProFileWriter written = new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(WIDE_VIBRATO);
        if (version != GuitarProVersion.GP3) {
            written.writeUnsignedByte(NO_FLAGS);
        }
        written.writeUnsignedByte(NO_STRINGS);
        if (version == GuitarProVersion.GP5_10) {
            written.writeShort(0);
        }

        assertTrue(read(written, version).effects().wideVibrato());
    }

    // ---- el rasgueo: su velocidad y, en GP5, su direccion -------------------

    /**
     * La velocidad del rasgueo se escribe como un numero de figura que empieza en la
     * semifusa doble: 1 es 1/128, 2 es 1/64, 3 es 1/32, y asi hasta 6, que es la negra.
     * Tabpro no llega a 1/128 y la aproxima con la semifusa, que es lo mas rapido que tiene.
     */
    @ParameterizedTest
    @CsvSource({"1, SIXTY_FOURTH", "2, SIXTY_FOURTH", "3, THIRTY_SECOND",
            "4, SIXTEENTH", "5, EIGHTH", "6, QUARTER"})
    void theStrokeSpeedIsAFigureNumber(int written, NoteValue expected) {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(written)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(expected, beat.effects().stroke().orElseThrow().speed());
    }

    @Test
    void gp4WritesFirstTheStrokeGoingDown() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertEquals(StrokeDirection.DOWN, beat.effects().stroke().orElseThrow().direction());
    }

    /** En GP5 el orden de las dos velocidades se invierte: primero la de arriba. */
    @Test
    void gp5WritesFirstTheStrokeGoingUp() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS)
                .writeShort(0), GuitarProVersion.GP5_10);

        assertEquals(StrokeDirection.UP, beat.effects().stroke().orElseThrow().direction());
    }

    @Test
    void gp5ReadsTheStrokeGoingDownFromTheSecondSpeed() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(3)
                .writeUnsignedByte(NO_STRINGS)
                .writeShort(0), GuitarProVersion.GP5_10);

        assertEquals(StrokeDirection.DOWN, beat.effects().stroke().orElseThrow().direction());
    }

    @Test
    void aBeatWithBothSpeedsInZeroHasNoStroke() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(HAS_STROKE).writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STROKE)
                .writeUnsignedByte(NO_STRINGS), GuitarProVersion.GP4);

        assertTrue(beat.effects().stroke().isEmpty());
    }

    // ---- la palanca y el golpe de GP3, que comparten un mismo bit -----------

    /**
     * En GP3 la palanca y el golpe entran por el mismo bit del beat: detras va un byte
     * que dice cual de los dos es -- 0 es la palanca -- y despues un entero con cuanto se
     * hunde la cuerda. No hay curva de puntos: esa forma llega recien con GP4.
     */
    @Test
    void gp3ReadsTheTremoloBarAsASingleDepth() {
        Beat beat = read(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_SLAP)
                .writeInt(100)
                .writeUnsignedByte(NO_STRINGS));

        Bend palanca = beat.effects().tremoloBar().orElseThrow();
        assertEquals(-4, palanca.points().get(1).quarterTones(), "un tono entero hacia abajo");
        assertEquals(0, palanca.points().getFirst().quarterTones());
        assertEquals(0, palanca.points().getLast().quarterTones());
    }

    @Test
    void gp3KeepsTheAlignmentAfterATremoloBar() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_SLAP)
                .writeInt(100)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        reader.read(bytes, GuitarProVersion.GP3, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP3, 6);

        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /** Con golpe en vez de palanca, esos cuatro bytes siguen ahi y hay que consumirlos. */
    @Test
    void gp3KeepsTheAlignmentAfterASlap() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(SLAPPING)
                .writeInt(0)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat golpe = reader.read(bytes, GuitarProVersion.GP3, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP3, 6);

        assertTrue(golpe.effects().slapping());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /** De GP4 en adelante ese bit es solo el golpe, sin el entero que traia GP3. */
    @Test
    void gp4KeepsTheAlignmentAfterASlap() {
        GuitarProByteReader bytes = new GuitarProByteReader(new GuitarProFileWriter()
                .writeUnsignedByte(HAS_EFFECTS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(TREMOLO_BAR_OR_SLAP)
                .writeUnsignedByte(NO_FLAGS)
                .writeUnsignedByte(SLAPPING)
                .writeUnsignedByte(NO_STRINGS)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .bytes());

        Beat golpe = reader.read(bytes, GuitarProVersion.GP4, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP4, 6);

        assertTrue(golpe.effects().slapping());
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    // ---- el tempo del cambio de parametros y su bandera de 5.10 -------------

    /**
     * Solo 5.10 escribe, detras de la transicion del tempo, si el cambio se muestra o no
     * en la partitura. Ese byte existe unicamente cuando el tempo cambia; si el lector no
     * lo consume, todo lo que sigue en el archivo queda corrido en uno.
     */
    @Test
    void gp510KeepsTheAlignmentAfterTheHiddenTempoFlag() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5BeatChangingTempo(140, GuitarProVersion.GP5_10)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat tempoBeat = reader.read(bytes, GuitarProVersion.GP5_10, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_10, 6);

        assertEquals(OptionalInt.of(140),
                tempoBeat.effects().parameterChange().valueOf(SoundParameter.TEMPO));
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /** En 5.00 ese byte no existe: leerlo tambien correria todo lo que sigue. */
    @Test
    void gp500HasNoHiddenTempoFlagAfterTheTempo() {
        GuitarProByteReader bytes = new GuitarProByteReader(gp5BeatChangingTempo(140, GuitarProVersion.GP5_00)
                .writeUnsignedByte(NO_FLAGS)
                .writeSignedByte(QUARTER)
                .writeUnsignedByte(ONLY_FIRST_STRING)
                .writeUnsignedByte(NOTE_WITH_FRET).writeUnsignedByte(NORMAL_NOTE).writeSignedByte(9)
                .writeUnsignedByte(NO_FLAGS)
                .writeShort(0)
                .bytes());

        Beat tempoBeat = reader.read(bytes, GuitarProVersion.GP5_00, 6);
        Beat siguiente = reader.read(bytes, GuitarProVersion.GP5_00, 6);

        assertEquals(OptionalInt.of(140),
                tempoBeat.effects().parameterChange().valueOf(SoundParameter.TEMPO));
        assertEquals(9, siguiente.noteOn(1).orElseThrow().fret());
    }

    /**
     * Un beat completo de GP5, sin notas, con un cambio de parametros que no toca nada salvo
     * el wah pedido: instrumento, volumen, pan, chorus, reverb, phaser, tremolo y tempo en -1
     * (sin bytes de transicion), la mascara de "para todas las pistas" en cero. Si el archivo
     * es 5.10, agrega nombre y categoria de efecto de RSE vacios, que solo esa version trae,
     * y siempre cierra con la mascara de cuerdas (sin notas) y el final de beat que GP5 agrega
     * a todos (la segunda voz).
     */
    private static GuitarProFileWriter gp5Beat(int wah, boolean withRseInstrumentEffect) {
        GuitarProFileWriter writer = new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED);
        for (int i = 0; i < 16; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeLengthPrefixedString("")
                .writeInt(UNCHANGED)
                .writeUnsignedByte(0x00)
                .writeSignedByte(wah);
        if (withRseInstrumentEffect) {
            writer.writeLengthPrefixedString("").writeLengthPrefixedString("");
        }
        return writer.writeUnsignedByte(NO_STRINGS).writeShort(0);
    }

    /**
     * El mismo beat de GP5 pero con el tempo cambiado, que es lo que le agrega bytes al
     * cambio de parametros: el de la transicion y, desde 5.10, el de "no muestres el
     * cambio de tempo en la partitura".
     */
    private static GuitarProFileWriter gp5BeatChangingTempo(int tempo, GuitarProVersion version) {
        boolean isGp510 = version == GuitarProVersion.GP5_10;
        GuitarProFileWriter writer = new GuitarProFileWriter()
                .writeUnsignedByte(WITH_MIX_TABLE)
                .writeSignedByte(QUARTER)
                .writeSignedByte(UNCHANGED);
        for (int i = 0; i < 16; i++) {
            writer.writeUnsignedByte(0);
        }
        writer.writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeSignedByte(UNCHANGED)
                .writeLengthPrefixedString("")
                .writeInt(tempo)
                .writeSignedByte(0);
        if (isGp510) {
            writer.writeBoolean(false);
        }
        writer.writeUnsignedByte(0x00).writeSignedByte(UNCHANGED);
        if (isGp510) {
            writer.writeLengthPrefixedString("").writeLengthPrefixedString("");
        }
        return writer.writeUnsignedByte(NO_STRINGS).writeShort(0);
    }

    private Beat read(GuitarProFileWriter written) {
        return read(written, GuitarProVersion.GP3);
    }

    private Beat read(GuitarProFileWriter written, GuitarProVersion version) {
        return reader.read(new GuitarProByteReader(written.bytes()), version, 6);
    }
}

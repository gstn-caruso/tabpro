package com.gstncaruso.tabpro.core.model.bars;

import java.util.Optional;

/** Los catorce saltos que puede llevar el final de un compas. */
public enum DirectionJump {
    DA_CAPO("Da Capo", null, null),
    DA_CAPO_AL_CODA("Da Capo al Coda", null, DirectionSymbol.CODA),
    DA_CAPO_AL_DOUBLE_CODA("Da Capo al Doble Coda", null, DirectionSymbol.DOUBLE_CODA),
    DA_CAPO_AL_FINE("Da Capo al Fine", null, DirectionSymbol.FINE),
    DA_SEGNO("Da Segno", DirectionSymbol.SEGNO, null),
    DA_SEGNO_AL_CODA("Da Segno al Coda", DirectionSymbol.SEGNO, DirectionSymbol.CODA),
    DA_SEGNO_AL_DOUBLE_CODA("Da Segno al Doble Coda", DirectionSymbol.SEGNO, DirectionSymbol.DOUBLE_CODA),
    DA_SEGNO_AL_FINE("Da Segno al Fine", DirectionSymbol.SEGNO, DirectionSymbol.FINE),
    DA_SEGNO_SEGNO("Da Segno Segno", DirectionSymbol.SEGNO_SEGNO, null),
    DA_SEGNO_SEGNO_AL_CODA("Da Segno Segno al Coda", DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.CODA),
    DA_SEGNO_SEGNO_AL_DOUBLE_CODA("Da Segno Segno al Doble Coda", DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.DOUBLE_CODA),
    DA_SEGNO_SEGNO_AL_FINE("Da Segno Segno al Fine", DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.FINE),
    DA_CODA("Da Coda", DirectionSymbol.CODA, null),
    DA_DOUBLE_CODA("Da Doble Coda", DirectionSymbol.DOUBLE_CODA, null);

    private final String label;
    private final DirectionSymbol jumpsTo;
    private final DirectionSymbol stopsAt;

    DirectionJump(String label, DirectionSymbol jumpsTo, DirectionSymbol stopsAt) {
        this.label = label;
        this.jumpsTo = jumpsTo;
        this.stopsAt = stopsAt;
    }

    public String label() {
        return label;
    }

    /** El simbolo al que hay que ir; vacio significa volver al principio de la partitura. */
    public Optional<DirectionSymbol> jumpsTo() {
        return Optional.ofNullable(jumpsTo);
    }

    /** El simbolo donde termina el recorrido despues del salto, si lo hay. */
    public Optional<DirectionSymbol> stopsAt() {
        return Optional.ofNullable(stopsAt);
    }
}

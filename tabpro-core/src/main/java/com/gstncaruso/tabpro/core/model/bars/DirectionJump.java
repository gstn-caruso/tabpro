package com.gstncaruso.tabpro.core.model.bars;

import java.util.Optional;

public enum DirectionJump {
    DA_CAPO(null, null),
    DA_CAPO_AL_CODA(null, DirectionSymbol.CODA),
    DA_CAPO_AL_DOUBLE_CODA(null, DirectionSymbol.DOUBLE_CODA),
    DA_CAPO_AL_FINE(null, DirectionSymbol.FINE),
    DA_SEGNO(DirectionSymbol.SEGNO, null),
    DA_SEGNO_AL_CODA(DirectionSymbol.SEGNO, DirectionSymbol.CODA),
    DA_SEGNO_AL_DOUBLE_CODA(DirectionSymbol.SEGNO, DirectionSymbol.DOUBLE_CODA),
    DA_SEGNO_AL_FINE(DirectionSymbol.SEGNO, DirectionSymbol.FINE),
    DA_SEGNO_SEGNO(DirectionSymbol.SEGNO_SEGNO, null),
    DA_SEGNO_SEGNO_AL_CODA(DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.CODA),
    DA_SEGNO_SEGNO_AL_DOUBLE_CODA(DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.DOUBLE_CODA),
    DA_SEGNO_SEGNO_AL_FINE(DirectionSymbol.SEGNO_SEGNO, DirectionSymbol.FINE),
    DA_CODA(DirectionSymbol.CODA, null),
    DA_DOUBLE_CODA(DirectionSymbol.DOUBLE_CODA, null);

    private final DirectionSymbol jumpsTo;
    private final DirectionSymbol stopsAt;

    DirectionJump(DirectionSymbol jumpsTo, DirectionSymbol stopsAt) {
        this.jumpsTo = jumpsTo;
        this.stopsAt = stopsAt;
    }

    public Optional<DirectionSymbol> jumpsTo() {
        return Optional.ofNullable(jumpsTo);
    }

    public Optional<DirectionSymbol> stopsAt() {
        return Optional.ofNullable(stopsAt);
    }
}

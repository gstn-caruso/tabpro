package com.gstncaruso.tabpro.ui.percussion;

import java.util.Optional;

/**
 * Las seis lineas de la notacion de percusion (una por sonido simultaneo, como
 * documenta {@code PercussionKit.LINE_COUNT}), con el sonido que le corresponde a
 * cada una y su posicion en el pentagrama que dibuja el asistente.
 */
public enum PercussionLine {
    CRASH(1, 5, 49, Optional.empty()),
    HI_HAT(2, 4, 42, Optional.empty()),
    HIGH_TOM(3, 3, 47, Optional.empty()),
    LOW_TOM(4, 2, 41, Optional.empty()),
    SNARE(5, 1, 38, Optional.of(40)),
    KICK(6, 0, 35, Optional.of(36));

    private final int number;
    private final int staffSlot;
    private final int acousticSound;
    private final Optional<Integer> electricSound;

    PercussionLine(int number, int staffSlot, int acousticSound, Optional<Integer> electricSound) {
        this.number = number;
        this.staffSlot = staffSlot;
        this.acousticSound = acousticSound;
        this.electricSound = electricSound;
    }

    /** La linea de la tablatura de percusion, de 1 a {@code PercussionKit.LINE_COUNT}. */
    public int number() {
        return number;
    }

    /** Cuantos pasos arriba de la linea de abajo del pentagrama se dibuja. */
    public int staffSlot() {
        return staffSlot;
    }

    /** El sonido que corresponde a esta linea, el electrico si se pide y esta disponible. */
    public int soundToUse(boolean preferElectric) {
        return preferElectric ? electricSound.orElse(acousticSound) : acousticSound;
    }

    public boolean hasElectricAlternative() {
        return electricSound.isPresent();
    }
}

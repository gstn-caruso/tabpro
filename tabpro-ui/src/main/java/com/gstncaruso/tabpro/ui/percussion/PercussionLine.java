package com.gstncaruso.tabpro.ui.percussion;

import java.util.Optional;

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

    public int number() {
        return number;
    }

    public int staffSlot() {
        return staffSlot;
    }

    public int soundToUse(boolean preferElectric) {
        return preferElectric ? electricSound.orElse(acousticSound) : acousticSound;
    }

    public boolean hasElectricAlternative() {
        return electricSound.isPresent();
    }
}

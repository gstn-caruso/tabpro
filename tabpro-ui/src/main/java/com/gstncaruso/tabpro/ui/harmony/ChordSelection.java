package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;

/**
 * Lo que arma la zona A: fundamental, tipo, bajo (para las inversiones) y el filtro de
 * complejidad con que se buscan los diagramas. Es inmutable, como el resto del motor.
 */
public record ChordSelection(PitchClass root, ChordType type, PitchClass bass, ChordComplexity complexity) {

    public static ChordSelection initial() {
        return new ChordSelection(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Do"), ChordComplexity.COMPLEX);
    }

    public Chord chord() {
        return Chord.inverted(root, type, bass);
    }

    /**
     * Al cambiar la fundamental, el bajo la sigue -salvo que el usuario ya hubiera elegido
     * una inversion, que ahi queda como estaba.
     */
    public ChordSelection withRoot(PitchClass root) {
        PitchClass nuevoBajo = bass.equals(this.root) ? root : bass;
        return new ChordSelection(root, type, nuevoBajo, complexity);
    }

    public ChordSelection withType(ChordType type) {
        return new ChordSelection(root, type, bass, complexity);
    }

    public ChordSelection withBass(PitchClass bass) {
        return new ChordSelection(root, type, bass, complexity);
    }

    public ChordSelection withComplexity(ChordComplexity complexity) {
        return new ChordSelection(root, type, bass, complexity);
    }
}

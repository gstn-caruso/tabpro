package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.model.NoteValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * El unico punto que traduce un tipo del dominio a su texto en castellano para
 * mostrarlo en un combo o una lista: ningun combo debe apoyarse en toString().
 */
class LabelsTest {

    @Test
    void traduceLaFiguraDeNota() {
        assertEquals("Negra", Labels.of(NoteValue.QUARTER));
    }

    @ParameterizedTest
    @EnumSource(NoteValue.class)
    void todaFiguraDeNotaTieneUnaEtiquetaPropia(NoteValue value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }

    @Test
    void traduceElTipoDeAcordeMayorAlSufijoQueUsaElManual() {
        assertEquals("M", Labels.of(ChordType.MAJOR));
    }

    @Test
    void traduceElTipoDeAcordeASuSufijoMusical() {
        assertEquals("m7", Labels.of(ChordType.MINOR_SEVENTH));
    }

    @ParameterizedTest
    @EnumSource(ChordType.class)
    void todoTipoDeAcordeTieneUnaEtiquetaPropia(ChordType value) {
        String etiqueta = Labels.of(value);

        assertFalse(etiqueta.isBlank());
        assertNotEquals(value.name(), etiqueta);
    }
}

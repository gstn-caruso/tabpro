package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MnemonicAssignerTest {

    private final MnemonicAssigner assigner = new MnemonicAssigner();

    @Test
    void unTextoSinLetrasNoTieneIndiceLibre() {
        assertEquals(-1, assigner.chooseIndex("123…"));
    }

    @Test
    void unTextoDeUnaSolaPalabraEligeSuPrimeraLetra() {
        assertEquals(0, assigner.chooseIndex("Guardar"));
    }

    @Test
    void dosTextosDeUnaPalabraConLaMismaInicialElSegundoCaeEnOtraLetraDelTexto() {
        assertEquals(0, assigner.chooseIndex("Sonido"));

        assertEquals(1, assigner.chooseIndex("Salir"));
    }
}

package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MnemonicAssignerTest {

    private final MnemonicAssigner assigner = new MnemonicAssigner();

    @Test
    void unTextoSinLetrasNiDigitosNoTieneIndiceLibre() {
        assertEquals(-1, assigner.chooseIndex("…"));
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

    @Test
    void unaLetraReservadaNoSeLeAsignaAOtroTexto() {
        assigner.reserve('S');

        assertEquals(1, assigner.chooseIndex("Salir"));
    }

    @Test
    void agotadasLasLetrasCaeEnUnDigitoDelTexto() {
        for (char letra : "grupode".toCharArray()) {
            assigner.reserve(letra);
        }

        assertEquals(9, assigner.chooseIndex("Grupo de 12"));
    }
}

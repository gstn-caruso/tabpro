package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
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

    @Test
    void agotadasLasLetrasYSinDigitosNoHayIndiceLibre() {
        for (char letra : "salir".toCharArray()) {
            assigner.reserve(letra);
        }

        assertEquals(-1, assigner.chooseIndex("Salir"));
    }

    @Test
    void unaLetraTomadaEnMinusculaBloqueaLaMismaLetraAcentuadaYEnMayuscula() {
        assigner.reserve('a');

        assertEquals(1, assigner.chooseIndex("Álbum"));
    }

    @Test
    void aplicarleElMnemonicoAUnBotonLeFijaLaTeclaYElIndiceSubrayado() {
        JMenuItem item = new JMenuItem("Guardar");

        assigner.applyTo(item);

        assertEquals(KeyEvent.VK_G, item.getMnemonic());
        assertEquals(0, item.getDisplayedMnemonicIndex());
    }
}

package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Test;

class MnemonicAssignerTest {

    private final MnemonicAssigner assigner = new MnemonicAssigner();

    @Test
    void aTextWithNoLettersOrDigitsHasNoFreeIndex() {
        assertEquals(-1, assigner.chooseIndex("…"));
    }

    @Test
    void aSingleWordTextPicksItsFirstLetter() {
        assertEquals(0, assigner.chooseIndex("Guardar"));
    }

    @Test
    void twoSingleWordTextsWithTheSameInitialTheSecondFallsOnAnotherLetterOfTheText() {
        assertEquals(0, assigner.chooseIndex("Sonido"));

        assertEquals(1, assigner.chooseIndex("Salir"));
    }

    @Test
    void aReservedLetterIsNotAssignedToAnotherText() {
        assigner.reserve('S');

        assertEquals(1, assigner.chooseIndex("Salir"));
    }

    @Test
    void withLettersExhaustedItFallsOnADigitOfTheText() {
        for (char letter : "grupode".toCharArray()) {
            assigner.reserve(letter);
        }

        assertEquals(9, assigner.chooseIndex("Grupo de 12"));
    }

    @Test
    void withLettersExhaustedAndNoDigitsThereIsNoFreeIndex() {
        for (char letter : "salir".toCharArray()) {
            assigner.reserve(letter);
        }

        assertEquals(-1, assigner.chooseIndex("Salir"));
    }

    @Test
    void aLowercaseLetterTakenBlocksTheSameAccentedAndUppercaseLetter() {
        assigner.reserve('a');

        assertEquals(1, assigner.chooseIndex("Álbum"));
    }

    @Test
    void applyingTheMnemonicToAButtonSetsItsKeyAndUnderlinedIndex() {
        JMenuItem item = new JMenuItem("Guardar");

        assigner.applyTo(item);

        assertEquals(KeyEvent.VK_G, item.getMnemonic());
        assertEquals(0, item.getDisplayedMnemonicIndex());
    }

    @Test
    void applyingTheMnemonicToALabelSetsItsKeyAndUnderlinedIndex() {
        JLabel label = new JLabel("Título");

        assigner.applyTo(label);

        assertEquals(KeyEvent.VK_T, label.getDisplayedMnemonic());
        assertEquals(0, label.getDisplayedMnemonicIndex());
    }
}

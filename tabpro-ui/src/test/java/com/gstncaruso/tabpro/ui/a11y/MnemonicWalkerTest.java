package com.gstncaruso.tabpro.ui.a11y;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class MnemonicWalkerTest {

    private final MnemonicWalker walker = new MnemonicWalker();

    @Test
    void aBarWithNoMenusHasNoFindings() {
        assertTrue(walker.walkMenuBar(new JMenuBar()).isEmpty());
    }

    @Test
    void aMenuWithNoMnemonicIsAFinding() {
        JMenuBar bar = new JMenuBar();
        bar.add(new JMenu("Archivo"));

        assertTrue(walker.walkMenuBar(bar).stream().anyMatch(v -> v.reason().equals("missing mnemonic")));
    }

    @Test
    void twoMenusWithDifferentMnemonicsHaveNoFindings() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Archivo");
        file.setMnemonic('A');
        JMenu edit = new JMenu("Editar");
        edit.setMnemonic('E');
        bar.add(file);
        bar.add(edit);

        assertTrue(walker.walkMenuBar(bar).isEmpty());
    }

    @Test
    void twoMenusWithTheSameMnemonicClashWithEachOther() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Archivo");
        file.setMnemonic('A');
        JMenu help = new JMenu("Ayuda");
        help.setMnemonic('A');
        bar.add(file);
        bar.add(help);

        List<Violation> violations = walker.walkMenuBar(bar);

        assertEquals(2, violations.stream().filter(v -> v.reason().equals("duplicate mnemonic")).count());
    }

    @Test
    void aMenuItemWithNoMnemonicIsAFinding() {
        JMenu menu = new JMenu("Archivo");
        menu.add(new JMenuItem("Nuevo"));

        assertTrue(walker.walkMenu(menu).stream().anyMatch(v -> v.reason().equals("missing mnemonic")));
    }

    @Test
    void aFormLabelWithNoMnemonicIsAFinding() {
        JPanel form = new JPanel();
        JTextField field = new JTextField();
        JLabel label = new JLabel("Título");
        label.setLabelFor(field);
        form.add(label);
        form.add(field);

        assertTrue(walker.walkForm(form).stream().anyMatch(v -> v.reason().equals("missing mnemonic")));
    }

    @Test
    void aLabelWithoutSetLabelForIsNotReported() {
        JPanel form = new JPanel();
        form.add(new JLabel("Sólo un título de sección"));

        assertTrue(walker.walkForm(form).isEmpty());
    }

    @Test
    void aLabelWithAMnemonicInANestedFormHasNoFindings() {
        JPanel outer = new JPanel();
        JPanel inner = new JPanel();
        JTextField field = new JTextField();
        JLabel label = new JLabel("Nombre");
        label.setLabelFor(field);
        label.setDisplayedMnemonic('N');
        inner.add(label);
        inner.add(field);
        outer.add(inner);

        assertTrue(walker.walkForm(outer).isEmpty());
    }

    private static final class Form extends JPanel implements MnemonicScope {
    }

    @Test
    void twoNestedFormsCanShareALetterWithoutItBeingAClash() {
        Form outerForm = new Form();
        JTextField artistField = new JTextField();
        JLabel artistLabel = new JLabel("Artista");
        artistLabel.setLabelFor(artistField);
        artistLabel.setDisplayedMnemonic('A');
        outerForm.add(artistLabel);
        outerForm.add(artistField);

        Form innerForm = new Form();
        JTextField keySignatureField = new JTextField();
        JLabel keySignatureLabel = new JLabel("Armadura");
        keySignatureLabel.setLabelFor(keySignatureField);
        keySignatureLabel.setDisplayedMnemonic('A');
        innerForm.add(keySignatureLabel);
        innerForm.add(keySignatureField);
        outerForm.add(innerForm);

        assertTrue(walker.walkForm(outerForm).isEmpty());
    }
}

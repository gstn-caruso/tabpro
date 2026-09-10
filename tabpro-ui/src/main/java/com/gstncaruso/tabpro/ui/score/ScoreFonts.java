package com.gstncaruso.tabpro.ui.score;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Set;

/** Las fuentes unicas de la partitura, una por rol, para que ningun painter arme la suya. */
final class ScoreFonts {

    private static final List<String> FAMILY_PREFERENCE =
            List.of("Times New Roman", "Liberation Serif", "Tinos", "FreeSerif");

    /**
     * La serif de la partitura, como la de Guitar Pro 5: la primera de la preferencia que este
     * instalada en esta maquina, o la serif logica de Java si ninguna lo esta.
     */
    static final String FAMILY = resolveFamily();

    /** El numero de traste que se escribe sobre cada cuerda de la tablatura. */
    static final Font FRET_FONT = new Font(FAMILY, Font.BOLD, 16);
    static final Font TUNING_LEGEND_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font MEASURE_NUMBER_FONT = new Font(FAMILY, Font.PLAIN, 10);

    /** Las siglas cortas de un efecto sobre la tablatura, como "PM" o "R". */
    static final Font EFFECT_SYMBOL_FONT = new Font(FAMILY, Font.BOLD, 9);
    /** El texto libre de un efecto, mas largo que una sigla: "let ring", "P.M.", "wah wah". */
    static final Font EFFECT_TEXT_FONT = new Font(FAMILY, Font.ITALIC, 10);

    static final Font FINGER_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font BEND_FONT = new Font(FAMILY, Font.ITALIC, 9);
    static final Font GRACE_FONT = new Font(FAMILY, Font.PLAIN, 8);

    /** El nombre de un marcador ("Bridge", "Outro") y los carteles de direccion y salto. */
    static final Font SECTION_MARK_FONT = new Font(FAMILY, Font.BOLD, 15);
    /** El cartelito de pases de un final alternativo, por ejemplo "1, 2.". */
    static final Font ALTERNATE_ENDING_FONT = new Font(FAMILY, Font.PLAIN, 9);
    static final Font REPEAT_COUNT_FONT = new Font(FAMILY, Font.BOLD, 10);

    static final Font TEMPO_FONT = new Font(FAMILY, Font.BOLD, 10);

    static final Font CHORD_NAME_FONT = new Font(FAMILY, Font.BOLD, 10);
    /** El "Xfr" que marca desde que traste arranca la grilla del diagrama de acorde. */
    static final Font CHORD_FRET_FONT = new Font(FAMILY, Font.PLAIN, 8);

    static final Font LYRICS_FONT = new Font(FAMILY, Font.PLAIN, 10);
    static final Font TRACK_LABEL_FONT = new Font(FAMILY, Font.BOLD, 11);
    /** El numero pequeno que cuenta cuantas notas entran en un grupo de valoracion especial. */
    static final Font TUPLET_FONT = new Font(FAMILY, Font.ITALIC, 10);

    static final Font PAGE_TITLE_FONT = new Font(FAMILY, Font.BOLD, 20);
    static final Font PAGE_SUBTITLE_FONT = new Font(FAMILY, Font.PLAIN, 13);
    static final Font PAGE_CREDIT_FONT = new Font(FAMILY, Font.PLAIN, 10);
    static final Font PAGE_FOOTER_FONT = new Font(FAMILY, Font.PLAIN, 9);

    private ScoreFonts() {
    }

    /** La marca "TAB" que abre cada sistema, con la letra tan alta como deje la tablatura. */
    static Font tabMarkFont(int letterHeight) {
        return new Font(FAMILY, Font.BOLD, letterHeight);
    }

    /** El "8" u "15" arriba o abajo del pentagrama, proporcional a su interlinea. */
    static Font octaveMarkFont(double staffLineSpacing) {
        return new Font(FAMILY, Font.ITALIC, (int) Math.round(staffLineSpacing * 1.7));
    }

    /** Los dos numeros de la armadura de tiempo, proporcionales a la interlinea del pentagrama. */
    static Font timeSignatureFont(double staffLineSpacing) {
        return new Font(FAMILY, Font.BOLD, (int) Math.round(staffLineSpacing * 2.1));
    }

    private static String resolveFamily() {
        Set<String> installed =
                Set.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String candidate : FAMILY_PREFERENCE) {
            if (installed.contains(candidate)) {
                return candidate;
            }
        }
        return Font.SERIF;
    }
}

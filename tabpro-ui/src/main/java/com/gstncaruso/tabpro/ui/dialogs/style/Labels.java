package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.instruments.FretboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.FretboardType;
import com.gstncaruso.tabpro.ui.instruments.KeyboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.NoteNameMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleLabelMode;
import com.gstncaruso.tabpro.ui.instruments.ScaleType;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.page.Orientation;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import java.util.List;

public final class Labels {

    private Labels() {
    }

    public static String of(Object value) {
        return switch (value) {
            case Chord chord -> chord.name();
            case ChordType chordType -> chordTypeLabel(chordType);
            case PitchClass pitchClass ->
                    Texts.get("domain.PitchClass.format", pitchClass.name(), pitchClass.solfegeName());
            case Scale scale -> scale.name();
            case Tuning tuning -> tuning.name() + " (" + stringLetters(tuning) + ")";
            case Dynamic dynamic -> dynamic.symbol();
            case ScaleType scaleType -> scaleType.label();
            case FretboardDisplayMode fretboardDisplayMode -> fretboardDisplayMode.label();
            case NoteNameMode noteNameMode -> noteNameMode.label();
            case ScaleLabelMode scaleLabelMode -> scaleLabelMode.label();
            case FretboardType fretboardType -> fretboardType.label();
            case KeyboardDisplayMode keyboardDisplayMode -> keyboardDisplayMode.label();
            case Orientation orientation -> orientation.label();
            case PaperFormat paperFormat -> paperFormatLabelWithDimensions(paperFormat);
            case Enum<?> constant -> domainLabel(constant);
            default -> throw new IllegalArgumentException("No label for " + value);
        };
    }

    private static String domainLabel(Enum<?> constant) {
        return Texts.get("domain." + constant.getDeclaringClass().getSimpleName() + "." + constant.name());
    }

    private static String paperFormatLabelWithDimensions(PaperFormat format) {
        return format.label() + " (" + Math.round(format.widthMillimetres()) + " x "
                + Math.round(format.heightMillimetres()) + " mm)";
    }

    private static String stringLetters(Tuning tuning) {
        StringBuilder letters = new StringBuilder();
        List<Pitch> strings = tuning.strings();
        for (int string = strings.size() - 1; string >= 0; string--) {
            letters.append(PitchClass.fromSemitone(strings.get(string).midiNumber()).name());
        }
        return letters.toString();
    }

    private static String chordTypeLabel(ChordType value) {
        return value == ChordType.MAJOR ? "M" : value.suffix();
    }

}

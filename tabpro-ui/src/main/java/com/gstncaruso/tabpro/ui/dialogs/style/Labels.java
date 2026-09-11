package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.ScoreInfo;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningName;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.page.PaperFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Labels {

    private Labels() {
    }

    public static String of(Object value) {
        return switch (value) {
            case Chord chord -> chord.name();
            case ChordType chordType -> chordTypeLabel(chordType);
            case PitchClass pitchClass ->
                    Texts.get("domain.PitchClass.format", pitchClass.name(), pitchClass.solfegeName());
            case Scale scale -> Texts.get("library.scale." + scale.id());
            case Tuning tuning -> tuningNameLabel(tuning.name()) + " (" + stringLetters(tuning) + ")";
            case Dynamic dynamic -> dynamic.symbol();
            case PaperFormat paperFormat -> paperFormatLabelWithDimensions(paperFormat);
            case Enum<?> constant -> domainLabel(constant);
            default -> throw new IllegalArgumentException("No label for " + value);
        };
    }

    public static Optional<String> percussionSoundName(int sound) {
        return PercussionKit.isPlayable(sound)
                ? Optional.of(Texts.get("library.percussion." + sound))
                : Optional.empty();
    }

    public static String headingOf(ScoreInfo info) {
        if (info.title().isBlank()) {
            return info.artist().isBlank() ? Texts.get("library.score.untitled") : info.artist();
        }
        return info.artist().isBlank() ? info.title() : Texts.get("library.score.heading", info.title(), info.artist());
    }

    public static String creditsOf(ScoreInfo info) {
        if (info.musicAuthor().equals(info.lyricsAuthor())) {
            return info.musicAuthor().isBlank() ? "" : Texts.get("library.score.wordsAndMusic", info.musicAuthor());
        }
        List<String> credits = new ArrayList<>();
        if (!info.musicAuthor().isBlank()) {
            credits.add(Texts.get("library.score.music", info.musicAuthor()));
        }
        if (!info.lyricsAuthor().isBlank()) {
            credits.add(Texts.get("library.score.words", info.lyricsAuthor()));
        }
        return String.join("\n", credits);
    }

    private static String domainLabel(Enum<?> constant) {
        return Texts.get("domain." + constant.getDeclaringClass().getSimpleName() + "." + constant.name());
    }

    private static String paperFormatLabelWithDimensions(PaperFormat format) {
        return domainLabel(format) + " (" + Math.round(format.widthMillimetres()) + " x "
                + Math.round(format.heightMillimetres()) + " mm)";
    }

    private static String tuningNameLabel(TuningName name) {
        return switch (name) {
            case TuningName.Library(String id) -> Texts.get("library.tuning." + id);
            case TuningName.UserNamed(String typed) -> typed;
            case TuningName.Custom() -> Texts.get("library.tuning.custom");
        };
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

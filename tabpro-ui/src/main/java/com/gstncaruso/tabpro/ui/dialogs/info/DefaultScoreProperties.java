package com.gstncaruso.tabpro.ui.dialogs.info;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import java.util.prefs.Preferences;

/**
 * Los valores por defecto de la proxima partitura nueva, guardados entre sesiones en
 * java.util.prefs, igual que DefaultPageSetup.
 */
public final class DefaultScoreProperties {

    private static final String TEMPO = "tempo";
    private static final String TIME_SIGNATURE_BEATS = "timeSignatureBeats";
    private static final String TIME_SIGNATURE_BEAT_UNIT = "timeSignatureBeatUnit";
    private static final String KEY_SIGNATURE_ACCIDENTALS = "keySignatureAccidentals";
    private static final String KEY_SIGNATURE_MODE = "keySignatureMode";
    private static final String TITLE = "title";
    private static final String ARTIST = "artist";

    private final Preferences store;

    public DefaultScoreProperties(Preferences store) {
        this.store = store;
    }

    public static DefaultScoreProperties userProperties() {
        return new DefaultScoreProperties(
                Preferences.userNodeForPackage(DefaultScoreProperties.class).node("newScoreDefaults"));
    }

    public NewScoreDefaults get() {
        NewScoreDefaults fallback = NewScoreDefaults.blank();
        return new NewScoreDefaults(
                store.getInt(TEMPO, fallback.tempo()),
                timeSignatureOrFallback(fallback.timeSignature()),
                keySignatureOrFallback(fallback.keySignature()),
                store.get(TITLE, fallback.title()),
                store.get(ARTIST, fallback.artist()));
    }

    public void save(NewScoreDefaults defaults) {
        store.putInt(TEMPO, defaults.tempo());
        store.putInt(TIME_SIGNATURE_BEATS, defaults.timeSignature().beats());
        store.putInt(TIME_SIGNATURE_BEAT_UNIT, defaults.timeSignature().beatUnit());
        store.putInt(KEY_SIGNATURE_ACCIDENTALS, defaults.keySignature().accidentals());
        store.put(KEY_SIGNATURE_MODE, defaults.keySignature().mode().name());
        store.put(TITLE, defaults.title());
        store.put(ARTIST, defaults.artist());
    }

    private TimeSignature timeSignatureOrFallback(TimeSignature fallback) {
        try {
            return new TimeSignature(
                    store.getInt(TIME_SIGNATURE_BEATS, fallback.beats()),
                    store.getInt(TIME_SIGNATURE_BEAT_UNIT, fallback.beatUnit()));
        } catch (IllegalArgumentException noSeEntiende) {
            return fallback;
        }
    }

    private KeySignature keySignatureOrFallback(KeySignature fallback) {
        try {
            Mode mode = Mode.valueOf(store.get(KEY_SIGNATURE_MODE, fallback.mode().name()));
            return new KeySignature(store.getInt(KEY_SIGNATURE_ACCIDENTALS, fallback.accidentals()), mode);
        } catch (IllegalArgumentException noSeEntiende) {
            return fallback;
        }
    }
}

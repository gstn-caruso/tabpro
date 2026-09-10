package com.gstncaruso.tabpro.ui;

/**
 * java.util.prefs.Preferences guarda cada nodo en el filesystem, y PreferencesTest,
 * ScoreDocumentTest y AccessibilitySettingsTest crean y borran nodos hermanos bajo el mismo padre
 * "com/gstncaruso/tabpro/test" (uno por metodo, para no compartir claves entre tests). Sin este
 * lock, esas altas y bajas concurrentes bajo el mismo padre -la suite corre en paralelo, ver
 * pom.xml raiz- corrompen la lectura de ese padre para quien lo toque al mismo tiempo. Cada una
 * de esas clases se anota con {@code @ResourceLock(RealPreferencesTests.LOCK)} para que JUnit las
 * serialice entre si, aunque sigan en paralelo con el resto de la suite -que no toca ese nodo-.
 */
final class RealPreferencesTests {

    static final String LOCK = "tabpro-real-preferences";

    private RealPreferencesTests() {
    }
}

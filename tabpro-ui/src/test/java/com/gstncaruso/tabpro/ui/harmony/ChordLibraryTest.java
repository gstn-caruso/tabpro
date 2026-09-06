package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ChordLibraryTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName());
    private final ChordLibrary library = new ChordLibrary(scratch);

    @AfterEach
    void limpiarElNodoDePrueba() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void empiezaVacia() {
        assertTrue(library.all().isEmpty());
    }

    @Test
    void agregaUnAcordeYLoConserva() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));

        library.add(am);

        assertEquals(List.of(am), library.all());
    }

    @Test
    void conservaLaDigitacion() {
        ChordDiagram conDedos = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1))
                .withFingering(java.util.Arrays.asList(null, Finger.INDEX, Finger.MIDDLE, Finger.RING, null, null));

        library.add(conDedos);

        assertEquals(conDedos.fingering(), library.all().get(0).fingering());
    }

    @Test
    void agregaVariosEnElOrdenEnQueSeAgregaron() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        ChordDiagram do_ = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));

        library.add(am);
        library.add(do_);

        assertEquals(List.of(am, do_), library.all());
    }

    @Test
    void borraElQueEligieron() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        ChordDiagram do_ = ChordDiagram.named("C", List.of(0, 1, 0, 2, 3, -1));
        library.add(am);
        library.add(do_);

        library.remove(0);

        assertEquals(List.of(do_), library.all());
    }

    @Test
    void actualizaElQueEligieronConElDiagramaNuevo() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(am);
        ChordDiagram amCejilla = ChordDiagram.named("Am", List.of(5, 5, 5, 7, 7, 5));

        library.update(0, amCejilla);

        assertEquals(List.of(amCejilla), library.all());
    }

    @Test
    void ordenaAlfabeticamentePorNombre() {
        ChordDiagram sol = ChordDiagram.named("G", List.of(3, 0, 0, 0, 2, 3));
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(sol);
        library.add(am);

        library.sortByName();

        assertEquals(List.of("Am", "G"), library.all().stream().map(ChordDiagram::name).toList());
    }

    @Test
    void loQueQuedaGuardadoSobreviveAUnaBibliotecaNueva() {
        ChordDiagram am = ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1));
        library.add(am);

        ChordLibrary otraInstancia = new ChordLibrary(scratch);

        assertEquals(List.of(am), otraInstancia.all());
    }
}

package com.gstncaruso.tabpro.core.harmony;

/**
 * Una candidata del buscador de escalas: una tonalidad y una escala, con cuantas notas
 * de lo que se toco no le pertenecen (menos incidencias, mejor candidata).
 */
public record ScaleMatch(PitchClass tonic, Scale scale, int incidentNotes) {
}

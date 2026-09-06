package com.gstncaruso.tabpro.format.powertab;

import com.gstncaruso.tabpro.core.model.Beat;

/**
 * Una posicion leida de PowerTab: en que indice del sistema esta anclada, y el
 * beat que le corresponde. Si trae un silencio de varios compases comprimido,
 * measureCount dice cuantos (0 si no es un silencio de ese tipo); ese caso
 * todavia no se arma en compases y lo reporta quien ensambla el compas.
 */
record PowerTabPosition(int index, Beat beat, int multibarRestMeasureCount) {

    boolean hasMultibarRest() {
        return multibarRestMeasureCount > 0;
    }
}

package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;

/** Los atributos de un compas, iguales para todas las pistas como en Guitar Pro. */
record GuitarProMasterBar(TimeSignature timeSignature, MeasureAttributes attributes) {
}

package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;

/** A measure's attributes, shared by every track just as Guitar Pro stores them. */
record GuitarProMasterBar(TimeSignature timeSignature, MeasureAttributes attributes) {
}

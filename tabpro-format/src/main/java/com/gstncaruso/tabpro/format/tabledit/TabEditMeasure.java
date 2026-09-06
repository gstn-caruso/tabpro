package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;

/** La medida y la armadura de un compas, tal como las trae TablEdit. */
record TabEditMeasure(TimeSignature timeSignature, KeySignature keySignature) {
}

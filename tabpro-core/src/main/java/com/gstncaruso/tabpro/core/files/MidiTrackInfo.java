package com.gstncaruso.tabpro.core.files;

/**
 * Lo que hace falta para que la ventana de import de MIDI muestre una pista del archivo elegido
 * y el usuario decida si la importa: su indice (para pedirla despues), su nombre, si es de
 * percusion, su instrumento de General MIDI, el canal que usa y cuantas notas tiene.
 */
public record MidiTrackInfo(int index, String name, boolean percussion, int program, int channelNumber, int noteCount) {
}
